# 📑 이커머스 랭킹 & 비동기 Outbox 시스템 설계/개발/회고 보고서

## 1. 시스템 개요

### 1.1 상품 인기 랭킹 시스템
- **목적**: 주문 발생 시 상품별 인기도(판매량, 주문 횟수 등)를 누적하고, 이를 기반으로 **일간/주간/전체기간 Top-N 랭킹 API**를 제공.
- **특징**
    - Redis ZSet을 활용한 **실시간 집계 & Top-N 조회 최적화**
    - KST 기준의 “오늘”, “최근 7일”, “전체기간” 단위로 API 제공
    - DB Outbox와 연계하여 **캐시/DB 최종 일치성 보장**

### 1.2 Outbox 기반 비동기 동기화 시스템
- **목적**: 주문 이벤트 발생 후 DB와 Redis 캐시 간의 불일치 문제를 방지.
- **특징**
    - Outbox 테이블에 이벤트 기록 후, Scheduler + SyncService가 비동기로 Redis/ZSet 반영
    - **트랜잭션 경계 안에서 Outbox 적재**, 이후 비동기 처리로 장애 복원력 강화
    - 재처리 가능한 **idempotent consumer** 구현

---

## 2. 아키텍처 설계

### 2.1 계층 구조

- **Controller**
    - REST API 제공 (`/products/rank/today`, `/weekly`, `/all-time`)
- **UseCase**
    - 랭킹 조회 요청을 받아 기간 계산 및 Service 호출
    - 파라미터 정규화 (Top-N 기본값, 상한값)
- **Service**
    - 주문 이벤트 처리 시 인기 상품 점수 갱신
    - Redis 캐시와 DB Outbox 병행 기록
    - Outbox 동기화/복구
- **Repository**
    - JPA 기반 RDB 영속화
- **Infrastructure**
    - Redis 접근 구현체, Spring Data JPA
- **Scheduler**
    - OutboxSyncScheduler가 주기적으로 미처리 Outbox 이벤트를 Redis에 반영

### 2.2 데이터 흐름

1. **주문 발생**
    - OrderPlacedEvent 발생
    - ProductPopularCacheService → Redis ZINCRBY로 스코어 누적
    - 동시에 ProductPopularOutboxSaveService → Outbox 테이블에 이벤트 저장

2. **비동기 동기화**
    - OutboxSyncScheduler → ProductPopularOutboxSyncService 실행
    - Outbox 상태 확인 후 Redis 반영
    - 성공 시 Outbox 상태 = PROCESSED

3. **조회 API**
    - Controller → UseCase → CacheService
    - Redis ZREVRANGE 기반 Top-N 조회
    - DTO 변환 후 응답

---

## 3. 주요 컴포넌트 설명

### 3.1 ProductPopularCacheService
- Redis ZSet 조작 (ZINCRBY, ZREVRANGE)
- 스코어 누적 및 조회 담당
- TTL 정책(일간/주간 키 만료) 적용 가능

### 3.2 ProductPopularOutboxSaveService
- 주문 이벤트 발생 시 Outbox 테이블에 기록
- RDB 트랜잭션 안에서 처리

### 3.3 ProductPopularOutboxSyncService
- Outbox → Redis 반영
- 중복 실행에도 문제없는 멱등 처리 보장

### 3.4 OutboxSyncScheduler
- 배치/스케줄러로 주기적 동기화 실행
- 장애 시 Outbox 적체를 재처리하여 정합성 확보

---

## 4. 개발 회고

### 4.1 잘된 점
- **관심사 분리**: Controller-UseCase-Service-Repository 계층이 명확 → 테스트 용이성 확보
- **성능 최적화**: Redis ZSet 사용으로 조회 성능 수 ms 단위 달성
- **장애 복원력**: Outbox + Scheduler 구조로 DB/Redis 일시적 장애에도 데이터 정합성 확보 가능

### 4.2 아쉬운 점
- Outbox 적체가 많아질 경우 배치 성능 저하 발생 가능 → **Kafka 등 MQ 기반 이벤트 스트리밍 고려 필요**
- 랭킹 스코어 정의(주문수 vs 매출 vs 최근성 감쇠)가 단순 → 향후 비즈니스 로직 반영 필요
- 캐시 TTL 및 만료 정책을 운영하면서 **데이터 보존/정책 조율 필요**

### 4.3 개선 계획
- Outbox → Kafka 기반 실시간 처리로 전환 검토
- 랭킹 가중치 모델 도입 (예: 주문량 + 매출 + 최신성)
- 모니터링 강화 (Outbox 적체량, Redis ZSet 크기, 랭킹 조회 QPS)

---

## 5. 결론

- **상품 인기 랭킹 시스템**은 Redis ZSet 기반으로 안정적이고 빠른 Top-N 조회를 제공.
- **비동기 Outbox 시스템**은 데이터 정합성을 지키면서도 장애 상황에 강인한 구조를 확보.
- 다만 향후 확장성과 대규모 트래픽 환경을 위해 **메시지큐 기반 이벤트 처리, 가중치 기반 랭킹 알고리즘, 모니터링/알림 강화**가 필요함.

---
