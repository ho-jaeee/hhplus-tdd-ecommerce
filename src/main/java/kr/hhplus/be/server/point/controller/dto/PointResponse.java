package kr.hhplus.be.server.point.controller.dto;

import kr.hhplus.be.server.point.domain.model.UserPointJPA;

public record PointResponse(
        long userId,
        long point,
        long updateMillis
) {
    public static PointResponse from(UserPointJPA entity) {
        return new PointResponse(
                entity.getUserId(),
                entity.getPoint(),
                entity.getUpdateMillis()
        );
    }
}
