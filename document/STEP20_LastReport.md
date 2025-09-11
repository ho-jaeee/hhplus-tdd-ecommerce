## 1. 목적 & 배경

- **왜 필요한가:**
    - 비즈니스 안정성 확보 (대규모 트래픽에서도 안정적 주문 처리)
    - 고객 경험 개선 (응답 지연·결제 실패 최소화)
    - 서비스 확장성 확보 (이벤트/세일 기간 스파이크 대응)

- **테스트 환경:**
    - Stage 환경 (MySQL + Redis + Kafka + Spring Boot 3.x)
    - k6 + Docker 기반 부하 테스트
    - Pinpoint APM, Grafana, Prometheus 모니터링 연동

---

## 2. 문제 정의

- **병목 가능성:** 주문/결제는 포인트·쿠폰·재고를 모두 연동하는 복합 트랜잭션 → DB/Redis/Kafka 동시 사용, 분산락 경합 가능성 높음
- **실제 발견된 문제:**
    - p95 = 59.99s (SLO 300ms 초과)
    - 에러율 = 7.9% (목표 <1%)
    - dropped_iterations 71k, 5xx 다수 → 풀 고갈 및 큐잉 발생

---

## 3. 테스트 설계

- **API 선정 근거:** 포인트 충전, 쿠폰 발급, 랭킹 조회, 주문/결제 → 핵심 사용자 플로우 전체 커버
- **시나리오 구성:** `ramping-arrival-rate` 기반
    - Warm-up → Steady → Ramp → Spike → Cool-down
    - Think time 최소화, 실제 사용자 패턴 유사하게 구성
- **vUser 관리:** ARR 기반 단계별 증가 → 스파이크 구간에서 최대 부하 확인

---

## 4. 결과 분석

- **핵심 지표:** p95/p99, TPS, dropped_iterations, 2xx/5xx 분포
- **리소스 메트릭:** DB 커넥션 풀 포화(active=20/20), Tomcat thread usage >90%, Redis ops/sec 정상, Kafka lag 정상
- **결론:** 주문/결제에서만 병목 발생 → **락 대기 + DB 풀 고갈**이 주요 원인

---

## 5. 후속 조치 & 장애 대응 전략

- **즉시 대응 (Short-term):**
    - Redisson waitMs 단축 (5000ms → 2000ms)
    - Thread pool 확장 (Tomcat maxThreads 300)
    - Auto-Scaling으로 인스턴스 증설
- **중기 대응 (Mid-term):**
    - 트랜잭션 분리(SAGA), 보상 트랜잭션 설계
    - retry/backoff 로직 적용
    - Pool/Thread 사용률 모니터링 강화
- **장기 대응 (Long-term):**
    - 결제/포인트/쿠폰 서비스 모듈 분리
    - CQRS & Event Sourcing 기반 재설계
    - 정기 Chaos 테스트로 장애 복원력 점검

> **지표 목표:**  
> MTTD < 1분, MTTR ≤ 10분, 비즈니스 임팩트 ≤ 2% 매출 손실

---

## 6. 인사이트 도출 & 액션 아이템

- **핵심 리스크:** 주문 처리 스파이크가 전체 시스템 안정성의 가장 큰 위협
- **안정 구간:** Redis 캐싱, 쿠폰 발급, 포인트 충전은 정상
- **Action Plan:**
    1. 락 파라미터 튜닝
    2. Pool 및 Thread 확장
    3. 트랜잭션 구조 개선
    4. 분산 아키텍처 전환 (CQRS/비동기화)
