package kr.hhplus.be.server.couponRedis.domain.service.dto;

public record FinishResult(boolean ok, boolean canceled, String error) {
    public static FinishResult ofOk(){ return new FinishResult(true, false, null); }
    public static FinishResult ofCanceled(){ return new FinishResult(false, true, null); }
    public static FinishResult ofError(String e){ return new FinishResult(false, false, e); }
}