# Kafka 기본 개념 학습 정리

## 1. Kafka란?
Apache Kafka는 분산 스트리밍 플랫폼으로, 대규모 데이터를 실시간으로 처리하고 전달하기 위한 메시징 시스템.  
이벤트 기반 아키텍처의 핵심 기술로 사용됨.

**특징**
- **고성능**: 초당 수백만 건 메시지 처리 가능
- **확장성**: 파티션 단위로 수평 확장
- **내결함성**: 복제 기반으로 장애 복구 가능
- **영속성**: 로그 파일 형태로 메시지를 디스크에 저장

---

## 2. Kafka의 주요 구성 요소
- **Producer**: 메시지를 토픽(Topic)에 발행(Publish).
- **Consumer**: 메시지를 토픽에서 구독(Subscribe) 후 처리.
- **Topic**: 메시지를 분류하는 단위.
- **Partition**: Topic을 나누는 단위, 병렬 처리 가능.
- **Broker**: Kafka 서버. 여러 개가 모여 클러스터를 구성.
- **Consumer Group**: 여러 Consumer가 협력해 메시지를 처리하는 단위.
- **Zookeeper / KRaft**:
    - Zookeeper → 기존의 메타데이터 관리 도구
    - KRaft → Kafka 내장 메타스토어(신규 아키텍처)

---

## 3. Kafka 동작 흐름
1. **Producer**가 특정 Topic에 메시지를 발행
2. 메시지는 **Partition**에 저장되고 **offset**(순서)이 부여됨
3. **Consumer Group**이 메시지를 가져가서 처리
4. Consumer는 **offset commit**으로 어디까지 처리했는지 기록

---

## 4. 개발 환경에서 Kafka 활용
- **Docker Compose**: Kafka + Zookeeper/KRaft 환경을 손쉽게 구성
- **Testcontainers**: 테스트 코드(JUnit)에서 Kafka 컨테이너를 띄워 통합 테스트 수행
- **Spring Kafka**:
    - `KafkaTemplate` → Producer 역할
    - `@KafkaListener` → Consumer 역할
    - `JsonSerializer`, `JsonDeserializer`를 통한 직렬화/역직렬화 지원

---

## 5. 통합 테스트 개념
- 단순히 메시지 발행 여부 확인은 의미가 적음
- 실제로 중요한 것:
    - 메시지 JSON 구조 검증
    - Consumer가 메시지를 정상적으로 처리하는지 검증
- 패턴: **send → consume → assert**

---

## 6. 학습 중 나온 주요 개념
- **Partition 개수**: 병렬 처리 단위, 불균형 시 **Hot Partition** 문제 발생
- **Idempotency Key**: 메시지 중복 처리 방지를 위한 고유 키
- **Exactly Once**: Kafka의 트랜잭션 기능으로 “정확히 한 번 처리” 보장 가능
- **Rebalancing**: Consumer Group 구성 변경 시 파티션 재할당 과정

---

## 7. 나의 학습 실습 경험
- **Docker Compose**로 Kafka+Zookeeper/KRaft 환경을 직접 구성
- **Testcontainers**를 활용하여 JUnit 기반 **통합 테스트 작성** → 발행/소비 end-to-end 검증
- **Spring Kafka**의 `@KafkaListener`와 `KafkaTemplate`을 활용한 실전 코드 작성
- 단순 이벤트 발행 여부가 아닌, **JSON 구조 및 Consumer 로직 처리까지 검증** → 현업 수준 테스트 경험

---

## 8. 학습 중 겪은 문제와 해결
- **Hot Partition 문제**: 특정 파티션에 트래픽이 집중되는 현상을 학습하면서, 키 설계 전략의 중요성을 이해함
- **Idempotency Key 필요성**: 중복 메시지 처리 가능성을 고려하며, 이벤트 고유 키 설계 필요성을 학습
- **Exactly Once 처리**: 단순히 "한 번만 처리된다"는 이론이 아니라, 실제 **트랜잭션 producer + consumer 구조**를 통해 가능하다는 점을 이해함

---

## 9. 학습 인사이트
- Kafka는 단순 메시지 큐가 아니라, **실시간 데이터 파이프라인**의 중심이 될 수 있는 플랫폼임을 체감
- Redis Queue, DB Outbox 등과 비교하며, Kafka가 갖는 **확장성과 신뢰성**의 차이를 명확히 알게 됨
- 이벤트 소싱, CQRS 같은 아키텍처 패턴과의 연결성을 이해하면서, **서비스 확장성과 이벤트 중심 아키텍처**의 중요성을 깨달음

---

## 10. 향후 학습 계획
- 운영 환경 수준의 **토픽 설계 및 오프셋 관리 전략** 학습
- **Kafka Streams**와 **Schema Registry**를 통한 실시간 데이터 처리 고도화
- **Dead Letter Queue(DLQ)**, **모니터링/알람 체계**까지 학습하여, 프로덕션 운영 관점에서 Kafka 이해 확장

---

## 결론
- Kafka는 **분산 메시징 플랫폼**이자 **실시간 스트리밍 처리의 핵심 기술**이다.
- 지금까지 학습한 범위는 **구성 요소, 동작 원리, 개발 환경 구성, 통합 테스트 개념**에 더해,  
  **직접 실습 경험, 문제 해결 경험, 개인적 인사이트, 향후 학습 계획**까지 포함한다.
- 이를 통해 단순한 개념 이해를 넘어, 실제 현업 적용 가능성을 고려한 학습을 진행했다.