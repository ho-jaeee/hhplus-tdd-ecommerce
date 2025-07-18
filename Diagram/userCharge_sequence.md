```mermaid

sequenceDiagram




    participant Client as Client

    participant UI as Web UI

    participant API as UserController

    participant Service as UserService

    participant UserRepo as UserRepository

    participant HistoryRepo as UserHistoryRepository



    Client->>UI: 포인트 충전 요청 (userId, 100,000원)

    UI->>API: POST /users/{userId}/charge

    API->>API: 유효성 검사 (0 초과, 1000원 단위)

    alt 유효하지 않음

        API-->>UI: 실패응답

        UI-->>Client: 에러 메시지

    else 유효함

        API->>Service: 충전 요청(userId, amount)

        Service->>UserRepo: userId로 User 조회

        alt 사용자 없음

            Service->>UserRepo: 신규 User(userId, point = 0) 저장

            UserRepo-->>Service: 신규 User 반환
        end
        alt 사용자 존재

            UserRepo-->>Service: 기존 User 반환
        end



        Service->>UserRepo: User.point += amount → 저장

        UserRepo-->>Service: 저장 완료

        Service->>HistoryRepo: UserHistory 저장 (userId, amount, CHARGE)

        HistoryRepo-->>Service: 저장 완료

        Service-->>API: 업데이트된 User 반환

        API-->>UI: 충전 완료 응답

        UI-->>Client: 충전 완료 및 잔액 표시

    end

```