package example.V0.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import example.V0.domain.Orders;
import example.V0.domain.OrderStatus;
import example.V0.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/// 실패한 주문을 독립적인 트랜잭션으로 기록하는 클래스
/// 별도 클래스로 분리한 이유:
/// - Spring AOP 프록시의 self-invocation 문제 회피
/// - @Transactional(REQUIRES_NEW)이 제대로 동작하려면 외부 호출 필요
@Slf4j
@Component
@RequiredArgsConstructor
public class FailedOrderRecorder {
	private final OrderRepository orderRepository;

	/// 실패한 주문을 독립 트랜잭션으로 기록한다.
	/// - propagation = REQUIRES_NEW:
	///   - 부모 트랜잭션이 롤백돼도 실패 기록은 커밋된다.
	/// - 목적:
	///   - 실패 원인 추적/감사 로그(운영 가시성) 확보
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void recordFailedOrder(Orders orders, String errorMessage) {
		try {
			orders.setStatus(OrderStatus.FAILED);
			orderRepository.save(orders);
			log.info("실패 주문 기록 저장: {} - {}", orders.getOrderId(), errorMessage);
		} catch (Exception e) {
			log.error("실패 주문 저장 중 오류: {}", e.getMessage());
		}
	}
}
