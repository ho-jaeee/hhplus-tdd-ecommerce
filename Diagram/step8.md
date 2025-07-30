# 이커머스 스키마 최적화 및 반정규화 보고서

## 1. 현재 스키마 의존 방향 검토

* 모든 외래키(FK)가 부모→자식 방향으로 단방향 의존을 갖고 있어, 사이클 없이 깔끔하게 설계되어 있습니다.

    * `USER → USER_HISTORY`
    * `USER → USER_COUPON`
    * `COUPON → USER_COUPON`
    * `USER → ORDER`
    * `COUPON → ORDER`
    * `ORDER → ORDER_ITEM`
    * `ORDER → ORDER_HISTORY`
    * `PRODUCT → ORDER_ITEM`
    * `PRODUCT → PRODUCT_HISTORY`
    * `ORDER → PRODUCT_HISTORY`
* **장점**

    * 순환 의존 없음 → 도메인/서비스 레이어 매핑 단순화
    * 의존 방향이 명확해 코드 가독성 상승
* **추가 검토**

    * JPA에서 양방향 탐색이 필요한 경우에만 `mappedBy`를 활용해 양방향 매핑 고려
    * 무분별한 양방향 매핑은 페치 조인 시 N+1 문제를 유발할 수 있으니 주의

## 2. 조회 성능 저하 요인

1. **다중 조인 비용**
   사용자별 구매 제품 조회 시

   ```
   USER → ORDER → ORDER_ITEM → PRODUCT
   ```

   4개 테이블을 조인해야 하므로, 데이터 규모가 커질수록 응답 지연 가능성↑
2. **대량 데이터 스캔**

    * `ORDER`, `ORDER_ITEM` 테이블의 전체 스캔 비용
    * 히스토리 테이블(`ORDER_HISTORY`, `PRODUCT_HISTORY`)의 누적 데이터로 인한 스캔 부담
3. **불충분한 인덱스**

    * 외래키 컬럼만 단일로 인덱싱되어 있어, 날짜 범위 필터나 정렬 조건이 포함된 쿼리에서 추가 스캔 발생
4. **집계 연산 부하**

    * `GROUP BY product_id` 같은 집계 쿼리에 대한 CPU·I/O 부하

## 3. 최적화 방안

### 3.1 인덱스 설계 강화

```sql
-- 1) ORDER 테이블: 사용자별 · 기간 필터 최적화
CREATE INDEX idx_order_user_date ON `ORDER`(user_id, created_at);

-- 2) ORDER_ITEM 테이블: 제품별 집계 최적화
CREATE INDEX idx_order_item_product ON ORDER_ITEM(product_id);

-- 3) USER_HISTORY: 최근 내역 조회
CREATE INDEX idx_user_history_date ON USER_HISTORY(user_id, update_millis);

-- 4) COUPON: 활성 쿠폰 필터링
CREATE INDEX idx_coupon_active ON COUPON(start_at, end_at);

-- 5) USER_COUPON: 사용자별 발급 내역 조회
CREATE INDEX idx_user_coupon_user ON USER_COUPON(user_id);
```

### 3.2 파티셔닝 & 아카이빙

* **파티셔닝**: `ORDER`/`ORDER_HISTORY`를 월별 또는 분기별 파티셔닝
* **아카이빙**: 일정 기간 지난 히스토리 데이터를 별도 아카이브 테이블로 이전

### 3.3 쿼리 리팩토링

* **커버링 인덱스** 활용: 자주 쓰는 컬럼을 인덱스에 포함시켜 디스크 I/O 절감
* **Keyset Pagination** 적용: `OFFSET` 대신 `WHERE (user_id, order_id) > (:lastUser, :lastOrder)` 방식
* **예제 쿼리**:

```sql
SELECT oi.product_id, p.name, SUM(oi.quantity) AS total_qty
FROM `ORDER` o
JOIN ORDER_ITEM oi ON o.order_id = oi.order_id
JOIN PRODUCT p ON oi.product_id = p.product_id
WHERE o.user_id = :userId
  AND o.created_at BETWEEN :startDate AND :endDate
GROUP BY oi.product_id;
```

## 4. 반정규화 테이블 설계

### 4.1 목적

* **풀 조인 없이** 단일 테이블 조회만으로 사용자별 구매 제품 통계 제공 → 응답 속도 대폭 개선

### 4.2 테이블 설계: `USER_PRODUCT_SUMMARY`

```sql
CREATE TABLE USER_PRODUCT_SUMMARY (
    user_id            BIGINT       NOT NULL,
    product_id         BIGINT       NOT NULL,
    product_name       VARCHAR(255) NOT NULL,
    total_quantity     INT          NOT NULL DEFAULT 0,
    total_spent        BIGINT       NOT NULL DEFAULT 0,
    first_purchase_at  DATETIME     NULL,
    last_purchase_at   DATETIME     NULL,
    purchase_count     INT          NOT NULL DEFAULT 0,
    PRIMARY KEY        (user_id, product_id),
    INDEX idx_ups_user (user_id),
    INDEX idx_ups_last (user_id, last_purchase_at)
);
```

* **컬럼 설명**

    * `total_quantity`: 누적 구매 수량
    * `total_spent`: 누적 구매 금액
    * `first_purchase_at` / `last_purchase_at`: 최초·최종 구매 시점
    * `purchase_count`: 구매 횟수
* **인덱스**

    * `user_id` 기준 조회 → 사용자 전체 통계 빠르게 조회
    * `(user_id, last_purchase_at)` → 최근 구매 순 정렬

### 4.3 동기화 전략

* **DB 트리거**

    * `AFTER INSERT/UPDATE/DELETE` 트리거로 요약 테이블 즉시 갱신
* **배치 처리**

    * Spring Batch 등을 활용한 야간 일괄 재산출
* **이벤트 기반**

    * Kafka 등 메시지 큐로 주문 이벤트 수신 후 요약 테이블 업데이트
