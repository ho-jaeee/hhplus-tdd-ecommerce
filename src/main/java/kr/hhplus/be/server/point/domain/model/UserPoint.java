package kr.hhplus.be.server.point.domain.model;

public record UserPoint(
        long userId,
        long point,
        long updateMillis
) {
    public static UserPoint empty(long userId) {
        return new UserPoint(userId, 0L, System.currentTimeMillis());
    }

    public UserPoint pointCharge(long amount) {
        return new UserPoint(userId, point + amount, System.currentTimeMillis());
    }

    public UserPoint pointUse(long amount) {
        return new UserPoint(userId, point - amount, System.currentTimeMillis());
    }
}
