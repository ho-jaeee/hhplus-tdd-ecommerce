📘 HHPlus TDD E-Commerce API

본 프로젝트는 TDD(Test-Driven Development) 와 클린 아키텍처 설계 원칙을 기반으로 구현된 학습형 e-commerce 백엔드 API입니다.
포인트, 쿠폰, 상품, 주문/결제, 인기상품 랭킹 도메인을 중심으로 책임을 명확히 나눈 구조로 설계되었으며,
실무에 도입 가능한 아키텍처 설계 능력 + 테스트 우선 개발 경험을 학습하는 것을 목표로 합니다.

🚀 프로젝트 개요

목적: 테스트 우선 개발(TDD) + 클린 아키텍처 실습

특징: 포인트/쿠폰/상품/주문/랭킹 도메인 분리 및 확장 가능한 구조

목표: 분산 환경, 동시성 이슈 대응, 이벤트 기반 아키텍처로 확장 가능

활용: 포트폴리오, 실무 학습, 기술 면접 대비

🧱 아키텍처 구조

본 프로젝트는 클린 아키텍처 철학을 따르며 단계적으로 전환 중입니다.
현재 구조는 Layered + Interface 기반으로 설계되어 있으며, 계층은 다음과 같습니다:

Controller (Presentation)
↓
UseCase (Application Layer)
↓
Domain (Model, Domain Service)
↓
Port (Interface)
↓
Adapter (JPA / Redis / External API)

📁 패키지 구조
kr.hhplus.be.server
├── point         # 포인트 충전/사용/조회
├── coupon        # 쿠폰 발급/검증/조회
├── product       # 상품 조회/인기상품 랭킹 통계
├── order         # 주문/결제/이력 저장
├── database      # Mock Table or Memory DB
├── mock          # Swagger Mock API
└── Diagram       # 시퀀스 다이어그램 / ERD

🖼 아키텍처 다이어그램
[Client]
│
▼
[Controller] → [UseCase] → [Domain Service]→ [Port Interface] → [Adapter(JPA, Redis, External)]

🧪 테스트 전략

TDD 기반: 기능 구현 전 테스트부터 작성

단위 테스트: JUnit5, Mockito, InMemory Repository 활용

통합 테스트:

@SpringBootTest, @AutoConfigureMockMvc

Testcontainers 기반 MySQL 환경

실제 Controller → UseCase → Domain → Repository 흐름 검증

트랜잭션 처리: @Transactional로 테스트 후 자동 롤백

테스트 커버리지 목표:

전체 코드: 80% 이상

핵심 도메인 서비스: 100% (예외/경계값 포함)

📌 주요 기능 & API 명세
기능	Method	Endpoint	설명
포인트 충전	PATCH	/point/charge/{id}	사용자 포인트 충전
포인트 조회	GET	/point/{id}	포인트 잔액 조회
상품 전체 조회	GET	/products	등록된 모든 상품 조회
상품 상세 조회	GET	/products/{id}	단일 상품 상세 조회
쿠폰 발급	POST	/coupons/issue	신규 쿠폰 발급
쿠폰 조회	GET	/coupons/{userId}	사용자 쿠폰 목록 조회
주문/결제 요청	POST	/orders	포인트 차감 + 쿠폰 할인 포함 결제 처리
인기상품 일간 랭킹 조회	GET	/products/rank/daily	Redis 기반 일간 인기 상품 조회
인기상품 주간 랭킹 조회	GET	/products/rank/weekly	Redis 기반 주간 인기 상품 조회
인기상품 전체기간 랭킹 조회	GET	/products/rank/all-time	Redis 기반 전체기간 인기 상품 조회

🔁 주문 처리 흐름 요약 (시퀀스)
Client → OrderController → OrderUseCase
→ ProductCheckService : 재고 확인
→ CouponCheckService : 쿠폰 유효성 검사
→ CouponDiscountService : 할인율 계산
→ PointUseService : 포인트 차감
→ OrderRepository : 주문 저장
→ OrderItemRepository : 주문상품 저장
→ OrderHistoryService : 주문 이력 저장
→ ProductHistoryService : 상품 이력 저장
→ ProductPopularService : 상품 랭킹 스코어 반영 (Redis ZSet)
→ OutboxService : 이벤트 로그 저장 (DB 동기화)

🛠 실행 방법
# 1. 프로젝트 클론
git clone https://github.com/ho-jaeee/hhplus-tdd-ecommerce.git
cd hhplus-tdd-ecommerce

# 2. Gradle 빌드
./gradlew clean build

# 3. 서버 실행
./gradlew bootRun

# 4. Swagger UI 확인 (Mock API 포함)
http://localhost:8080/swagger-ui/index.html


📌 Docker 기반 실행

# MySQL 실행
docker-compose up -d

# Redis Cluster 실행
docker-compose -f docker-compose-redis.yml up -d

# (Windows용) 단일 Redis 실행
docker-compose -f docker-compose-redis-windows.yml up -d

📌 기술 스택

Java 17

Spring Boot 3.1+

JPA / In-Memory DB

Redis (ZSet 기반 랭킹)

Gradle (Kotlin DSL)

JUnit 5, Mockito

Swagger (Springdoc OpenAPI)

Docker, Testcontainers


📈 향후 개선 및 TODO (우선순위)

1순위 도메인 모델 ↔ JPA 완전 분리 (Entity → Domain Mapper)

1순위 Port/Adapter 구조 본격 적용

2순위 동시성 이슈 처리 (쿠폰 발급, 포인트 차감 등)

2순위 Redis 기반 캐시 / 선착순 처리 개선

3순위 Docker-Compose 기반 통합 개발환경 구성

3순위 이벤트 기반 구조 설계 (Outbox Pattern, Kafka 연동)

3순위 대규모 트래픽 상황에서 랭킹 성능 검증


🙋‍♂️ 프로젝트 기획자 및 개발자

이재호 (Jaeho Lee)

기획자인 제가 직접 개발자 입장에서 학습 및 구현

도메인 기반 설계(DDD), 클린 아키텍처, 테스트 주도 개발(TDD) 학습용 실전 프로젝트

✔ 이 README는 단순 코드 저장소 설명을 넘어, 포트폴리오 + 기술 학습 문서 역할을 목표로 정리되었습니다.