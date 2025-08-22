# 🎫 쿠폰 발급 시스템 설계/개발/회고 보고서

## 1. 시스템 개요
- **목적**
    - 트래픽 피크 상황에서도 안정적인 **선착순 쿠폰 발급** 제공
    - **중복 발급 방지** 및 **멱등성(reqId) 보장**
    - Outbox 없이도 **DB 확정 + Redis 상태**로 정합성 유지

- **특징**
    - **Redis + Lua** 기반의 원자적 경쟁 제어
    - **DB UNIQUE 제약**으로 정확히 한 번(Exactly-once) 발급 보장
    - HTTP 상태코드 매핑을 통한 명확한 응답 제공

---

## 2. 아키텍처 설계

### 2.1 패키지 구조
- **controller**
    - CouponController
    - dto: CouponRequest, CouponResponse
- **usecase**
    - CouponIssuedUseCase, CouponIssuedUseCaseImpl
    - dto: CouponIssueCommand, CouponIssueResult
- **domain**
    - model: CouponJPA, CouponUserJPA
    - service: CouponCheckService, CouponDiscountService, CouponIssuedService
    - service.dto: Coupon, CouponUser, HoldResult, FinishResult
    - repository: CouponRepository, CouponUserRepository, CouponRedisQueueRepository
- **infrastructure**
    - CouponRepositoryImpl, CouponUserRepositoryImpl, CouponRedisQueueRepositoryImpl
    - SpringDataCouponRepository, SpringDataCouponUserRepository
- **policy**
    - CouponValidator

### 2.2 발급 흐름
1. **사전 검증**
    - CouponValidator → 쿠폰 유효성, 사용자 자격 검사 -> 기존에 있었으나 삭제 함 
2. **Redis Lua 경쟁 제어**
    - `SISMEMBER issued` → 이미 발급된 사용자 차단
    - `INCR seq` → 순번 증가
    - `ZADD NX queue` → 큐 등록
    - `ZCARD > limit` → 컷오프 (ZREMRANGEBYRANK)
    - 승자만 DB 확정 단계로 진행
3. **DB 확정 (트랜잭션)**
    - CouponIssuedService → CouponUserRepository.insert()
    - `UNIQUE (coupon_id, user_id)` : 사용자 중복 방지
    - `UNIQUE (req_id)` : 멱등성 보장
4. **Redis 갱신**
    - 커밋 성공 후 `SADD issued` → 최종 발급자 등록
5. **멱등 재시도**
    - 동일 reqId 재호출 시 DB 조회 후 동일 응답 반환

---

## 3. API 명세

### Endpoint
`POST /coupons/issue`

### Request
```json
{
  "couponId": 1001,
  "userId": 98765,
  "reqId": "external-idempotency-key-123"
}
```

### Response (성공)
```json
{
  "userId": 98765,
  "couponId": 1001,
  "success": true,
  "message": "OK",
  "rank": 73
}
```

### 상태코드 매핑
| 상황 | message | HTTP |
|---|---|---|
| 파라미터 오류 | 잘못된 입력입니다. | 400 |
| 이미 발급된 사용자 | 이미 발급된 사용자입니다. | 409 |
| 선착순 마감 | 선착순 마감되었습니다. | 410 |
| 내부 오류 | UNKNOWN_ERROR | 502 |
| 성공 | OK | 201 |

---

## 4. 데이터 모델

### DB 스키마
```sql
CREATE TABLE coupon_issues (
  id BIGSERIAL PRIMARY KEY,
  coupon_id BIGINT NOT NULL,
  user_id   BIGINT NOT NULL,
  req_id    VARCHAR(64) NOT NULL,
  issued_at TIMESTAMP NOT NULL DEFAULT now(),
  UNIQUE (coupon_id, user_id),
  UNIQUE (req_id)
);
```

### Redis 키
- `coupon:{couponId}:issued` → Set(userId) : 최종 발급자 집합
- `coupon:{couponId}:queue` → ZSet(score=ts/seq, member=userId) : 대기열
- `coupon:{couponId}:seq`   → String counter : 발급 순번

---

## 5. 정책/검증
- **쿠폰 유효기간/재고 검사**: CouponValidator
- **사용자 중복 방지**: DB UNIQUE + Redis SISMEMBER
- **멱등성 보장**: reqId 기반 (없으면 서버에서 UUID 생성)
- **컷오프 정책**: ZCARD > limit 시 초과분 제거

---

## 6. 테스트 전략

### 단위 테스트
- Validator (만료, 재고=0, 자격 미달)
- 상태코드 매핑 검증 (400/409/410/502)
- 멱등성: 동일 reqId 2회 호출 → DB 행 1건
- Redis SISMEMBER → 이미 발급된 사용자 차단

### 통합/동시성 테스트
- N 스레드 동시 요청, limit=K → 성공 수 정확히 K
- 동일 사용자 다른 reqId 경합 → 1건만 확정
- DB 오류 후 동일 reqId 재시도 → 최종 1건 확정

---

## 7. 운영/모니터링
- **지표**: 발급 성공/실패 비율, p95/p99 레이턴시, Lua 실패율
- **메모리 관리**: 이벤트 종료 후 queue/seq/issued TTL 설정
- **레이트리밋**: 사용자/IP 단위 제한
- **장애 대응**
    - Redis 장애 시 발급 중단 후 재시도
    - DB 에러 시 동일 reqId 재시도 허용

---

## 8. 회고

### 잘된 점
- Outbox 없이도 **DB 제약 + Lua 원자성**으로 정합성 확보
- Controller–UseCase–Service–Repository 계층 분리 → 테스트 용이
- Redis 기반 구조로 **고경쟁 환경에서도 성능 확보**

### 아쉬운 점
- 극단적 트래픽 시 Redis 단일 샤드 병목 가능
- 재시도 증가 시 seq 값과 실제 발급 수 불일치 가능
- 운영 리포팅 데이터는 별도 배치/CDC 필요

### 개선 계획
- **Redis 샤딩** 도입 (couponId 기반 파티셔닝)
- **관찰성 강화**: 지표/로그 분리, 대시보드 고도화
- **정책 고도화**: 사용자 쿨다운, 디바이스/아이피 제한

---

## 9. 샘플 요청/응답

### 성공
```http
POST /coupons/issue
{"couponId":1001,"userId":98765,"reqId":"abc-123"}

HTTP/1.1 201 Created
{"userId":98765,"couponId":1001,"success":true,"message":"OK","rank":73}
```

### 이미 발급
```http
HTTP/1.1 409 Conflict
{"success":false,"message":"이미 발급된 사용자입니다."}
```

### 마감
```http
HTTP/1.1 410 Gone
{"success":false,"message":"선착순 마감되었습니다."}
```
