package kr.hhplus.be.server.point.controller.dto;

public record PointResponse(
        long userId,
        long point,
        long updateMillis
) {
    public static PointResponse of(long userId, long point) {
        return new PointResponse(userId, point, System.currentTimeMillis());
    }
}
