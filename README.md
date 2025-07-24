# HHPlus TDD E-Commerce API

본 프로젝트는 TDD(Test-Driven Development) 방식과 클린 아키텍처 설계 원칙을 기반으로 구현된 전자상거래 백엔드 API입니다.  
포인트 시스템, 쿠폰, 상품, 주문/결제 도메인을 중심으로 기능을 분리하고, 책임을 명확히 나눈 구조로 설계되었습니다.

---

## 🚀 주요 기능

- 사용자 포인트 충전 및 사용
- 상품 전체 조회 및 상세 조회
- 쿠폰 발급 및 사용자 쿠폰 조회
- 주문 및 결제 처리 (포인트 차감 + 쿠폰 할인 포함)
- 주문 이력 / 상품 이력 저장
- Swagger 기반 Mock API 설계 및 명세화

---

## 🧱 아키텍처 구조

본 프로젝트는 클린 아키텍처 철학을 따르되, 단계적으로 전환 중입니다.  
현재 구조는 Layered + Interface 기반으로 설계되어 있으며, 다음의 계층으로 나뉘어 있습니다:

```
Controller (Presentation)
    ↓
UseCase (Application Layer)
    ↓
Domain (Model, Domain Service)
    ↓
Port (Interface)
    ↓
Adapter (JPA / Memory)
```

### 📁 패키지 구조

```
kr.hhplus.be.server
├── point         # 포인트 충전/사용/조회
├── coupon        # 쿠폰 발급/검증/조회
├── product       # 상품 조회/인기상품 통계
├── order         # 주문/결제/이력 저장
├── database      # Mock Table or Memory DB
├── mock          # Swagger Mock API
└── Diagram       # 시퀀스 다이어그램 / ERD
```

---

## 🧪 테스트 전략

- TDD 기반: 기능 구현 전 테스트부터 작성
- `JUnit5` + `Mockito` 기반 단위 테스트 수행
- 모든 도메인 유스케이스에 대해 경계값/예외 케이스 검증
- Repository는 메모리 기반(`InMemoryRepository`)으로 대체하여 단위 테스트 우선 적용
- 통합 테스트는 이후 단계에서 Docker 환경으로 확장 예정

---

## 📌 API 명세 요약

| 기능 | Method | Endpoint | 설명 |
|------|--------|----------|------|
| 포인트 충전 | PATCH | `/point/charge/{id}` | 사용자 포인트를 충전 |
| 포인트 조회 | GET | `/point/{id}` | 현재 포인트 잔액 조회 |
| 상품 전체 조회 | GET | `/products` | 등록된 모든 상품 조회 |
| 상품 상세 조회 | GET | `/products/{id}` | 개별 상품 상세 조회 |
| 주문/결제 요청 | POST | `/orders` | 포인트 차감 및 결제 처리 |

---

## 🔁 주문 처리 흐름 요약 (시퀀스)

```
Client → OrderController → OrderUseCase
  → ProductCheckService : 재고 확인
  → CouponCheckService : 쿠폰 유효성 검사
  → CouponDiscountService : 할인율 계산
  → PointUseService : 포인트 차감
  → OrderRepository : 주문 저장
  → OrderItemRepository : 주문상품 저장
  → OrderHistoryService : 주문 이력 저장
  → ProductHistoryService : 상품 이력 저장
```

---

## 🛠 실행 방법

```bash
# 1. 프로젝트 클론
git clone https://github.com/ho-jaeee/hhplus-tdd-ecommerce.git
cd hhplus-tdd-ecommerce

# 2. Gradle 빌드
./gradlew clean build

# 3. 서버 실행
./gradlew bootRun

# 4. Swagger UI 확인 (Mock API 포함)
http://localhost:8080/swagger-ui/index.html
```

---

## 📌 기술 스택

- Java 17
- Spring Boot 3.1+
- JPA / In-Memory DB
- Gradle (Kotlin DSL)
- JUnit 5, Mockito
- Swagger (Springdoc OpenAPI)
- GitHub Branch 기반 설계/커밋 관리

---

## 📈 향후 개선 및 TODO

- 도메인 모델 ↔ JPA 완전 분리 (Entity → Domain Mapper)
- Port/Adapter 구조 본격 적용
- 동시성 이슈 처리 (쿠폰 발급, 포인트 차감 등)
- 분산 환경 대응을 위한 Event 기반 구조 설계
- Redis 기반 캐시 / 선착순 처리 개선
- Docker-Compose 기반 통합 개발환경 구성

---

## 🙋‍♂️ 프로젝트 기획자 및 개발자

이재호 (Jaeho Lee)  
- 기획자인 제가 개발자 입장이 되어 실무에서 도입 가능한 아키텍처 설계 및 테스트 우선 개발을 학습 중입니다.  
- 이 프로젝트는 도메인 기반 설계, 클린 아키텍처 이행, 테스트 주도 개발을 연습하는 실전 프로젝트입니다.

---