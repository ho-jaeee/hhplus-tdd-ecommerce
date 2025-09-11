package kr.hhplus.be.server.point.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserPointJPA {

    @Id
    private long userId;

    private long point;

    private long updateMillis;

    public static UserPointJPA empty(Long userId) {

        return new UserPointJPA(userId, 0L, System.currentTimeMillis());
    }

    public long charge(long amount) {

        this.point += amount;
        this.updateMillis = System.currentTimeMillis();
        return this.point;
    }

    public long use(long amount) {
        if (this.point < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다");
        }
        this.point -= amount;
        this.updateMillis = System.currentTimeMillis();
        return this.point;
    }
}
