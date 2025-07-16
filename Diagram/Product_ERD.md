```mermaid
erDiagram

    PRODUCT ||--o{ PRODUCT_HISTORY : has_history

    %% 상품 기본 정보 테이블
    PRODUCT {
        long product_id PK
        string name
        long price
        int quantity
        datetime created_at
        datetime updated_at
    }

    %% 상품 재고 변경 이력 테이블
    PRODUCT_HISTORY {
        long id PK
        long product_id
        long order_id
        enum change_type
        int quantity
        string product_name 
        long price_per_unit 
        datetime created_at
    }
```
