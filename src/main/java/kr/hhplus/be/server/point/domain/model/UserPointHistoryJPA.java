package kr.hhplus.be.server.point.domain.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor

public class UserPointHistoryJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long point;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private Long updateMillis;

    public static UserPointHistoryJPA create(Long userId, Long point, TransactionType type) {
        return new UserPointHistoryJPA(null, userId, point, type, System.currentTimeMillis());
    }

    public enum TransactionType {
        CHARGE, USE
    }
}
