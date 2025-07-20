package kr.hhplus.be.server.point.domain.model;


public record UserPointHistory(
        long id,
        long userId,
        long point,
        TransactionType type,
        long updateMillis
){
    public enum TransactionType {
        CHARGE, USE
    }


    public static UserPointHistory pointHistoryCreat(long id, long userId, long amount, TransactionType type) {
        return new UserPointHistory(id, userId, amount, type, System.currentTimeMillis());
    }

}
