package kr.hhplus.be.server.coupon.domain.service.dto;

public record HoldResult(boolean hold, Long rank, String error) {

    public static HoldResult success(Long rank) {
        return new HoldResult(true, rank, null);
    }

    public static HoldResult fail(String err) {
        return new HoldResult(false, null, err);
    }

    public boolean isAlreadyIssued() {
        return "ALREADY_ISSUED".equals(error);
    }
    public boolean isOutOfCut() {
        return "OUT_OF_CUT".equals(error);
    }
}