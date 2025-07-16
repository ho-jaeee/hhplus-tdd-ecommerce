```mermaid
erDiagram

    PRODUCT ||--o{ PRODUCT_HISTORY : has_history

    %% 상품 기본 정보 테이블
    PRODUCT {
        long product_id "제품ID" PK
        string name "제품이름"
        long price "가격"
        int quantity "수량"
        datetime created_at "제품 최초 등록일"
        datetime updated_at "제품 변경일"
    }

    %% 상품 재고 변경 이력 테이블
    PRODUCT_HISTORY {
        long id PK "이력 ID"
        long product_id "제품ID(논리적 FK)"
        long order_id "주문ID"
        enum change_type "이력 타입(sale, restock, return, manual)"
        int quantity "판매수량"
        string product_name "판매 시 이름" 
        long price_per_unit "판매 단가"
        datetime created_at "이력 생성일"
    }
```
