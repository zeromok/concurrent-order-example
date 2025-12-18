package example.V0.service;

import static example.V0.util.Sleep.*;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import example.V0.domain.OrderStatus;
import example.V0.domain.Orders;
import example.V0.domain.Product;
import example.V0.repository.OrderRepository;
import example.V0.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProcessorV0 {
	private final ProductRepository productRepository;
	private final OrderRepository orderRepository;
	private final FailedOrderRecorder failedOrderRecorder;

	/// ## 주문 처리 (단일 스레드, 다일 트랜잭션)
	/// ### 처리 순서
	/// 1. 주문 저장 (PENDING)
	/// 2. 주문 검증 (VALIDATED)
	/// 3. 재고확인 및 차감 (STOCK_CHECKED)
	/// 4. 결제 처리 (PAYMENT_COMPLETED)
	/// 5. 주문 완료 (COMPLETED)
	///
	/// ### 실패 처리
	/// - RuntimeException 발생 시 전체 롤백 (재고 복원)
	/// - 실패 주문 기록은 별도 트랜잭션으로 저장
	@Transactional
	public void processOrder(Orders orders) {
		try {
			// 주문 초기 저장
			orderRepository.save(orders);
			log.debug("주문 생성: {}", orders.getOrderId());

			// 주문 검증
			validatorOrder(orders);
			orders.setStatus(OrderStatus.VALIDATED);
			orderRepository.save(orders);

			// 재고 확인 및 차감
			checkAndDecreaseStock(orders);
			orders.setStatus(OrderStatus.STOCK_CHECKED);
			orderRepository.save(orders);

			// 결제 처리
			processPayment(orders);
			orders.setStatus(OrderStatus.PAYMENT_COMPLETED);
			orderRepository.save(orders);

			// 완료
			orders.setStatus(OrderStatus.COMPLETED);
			orderRepository.save(orders);
			log.info("주문 완료: {}", orders.getOrderId());

		} catch (Exception e) {
			log.error("주문 처리 실패 [{}]: {}", orders.getOrderId(), e.getMessage());

			// 이 시점에 예외를 던지면 롤백될 예정 (재고 복원)
			// 실패 기록만 별도 트랜잭션으로 저장
			failedOrderRecorder.recordFailedOrder(orders, e.getMessage());

			// 예외를 다시 던져서 메인 트랜잭션 롤백
			throw new RuntimeException("주문 처리 실패: " + e.getMessage(), e);
		}
	}



	/// 결제 처리
	private void processPayment(Orders orders) {
		sleep(200);

		// 10% 확률로 결제에 실패한다.
		if (Math.random() < 0.1) {
			throw new RuntimeException("결제 실패");
		}
	}

	/// 재고 확인 및 차감
	private void checkAndDecreaseStock(Orders orders) {
		sleep(50);

		Product product = productRepository.findById(orders.getProductId())
			.orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다." + orders.getProductId()));

		product.decreaseStock(orders.getQuantity());
		productRepository.save(product);
	}

	/// 주문 검증
	private void validatorOrder(Orders orders) {
		sleep(100);

		if (orders.getQuantity() <= 0) {
			throw new IllegalArgumentException("주문 수량은 0보다 커야 합니다.");
		}

		if (orders.getPrice() <= 0) {
			throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
		}
	}
}
