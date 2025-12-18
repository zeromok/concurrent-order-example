package example.V0.domain;

public enum OrderStatus {
	PENDING,        // 대기 중
	VALIDATED,      // 검증 완료
	STOCK_CHECKED,  // 재고 확인 완료
	PAYMENT_COMPLETED, // 결제 완료
	COMPLETED,      // 주문 완료
	FAILED          // 실패
}
