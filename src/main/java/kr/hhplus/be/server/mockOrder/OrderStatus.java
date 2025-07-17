package kr.hhplus.be.server.mockOrder;

public enum OrderStatus {
    CREATED,   // 주문 생성됨
    PAID,      // 결제 완료
    FAILED,    // 결제 실패
    CANCELED   // 주문 취소
}
