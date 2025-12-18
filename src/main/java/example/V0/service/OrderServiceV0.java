package example.V0.service;

import java.util.List;

import org.springframework.stereotype.Service;

import example.V0.domain.OrderStatus;
import example.V0.domain.Orders;
import example.V0.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/// # V0: 단일 스레드 주문
/// ## 특징
/// - 모든 작업이 순차적으로 실행
/// - @Transactional로 트랜잭션 관리
/// - 각 주문마다 별도 트랜잭션 (rollback 독립적)
///
/// ## 문제점:
/// - CPU 활용률 낮음
/// - 대량 주문 처리 시 응답 시간 증가
/// - I/O 대기 시간 동안 CPU 유휴
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceV0 {
	private final OrderRepository orderRepository;
	private final OrderProcessorV0 orderProcessorV0;

	/// 주문 1건 처리 유스케이스
	/// - 트랜잭션 경계: 이 메서드 1회 호출 = 트랜잭션 1개
	/// - 실패 시 메인 트랜잭션 롤백(재고/주문상태 변경 롤백)
	/// - 실패 주문 기록은 FailedOrderRecorder(REQUIRES_NEW)로 별도 커밋
	public void processOrder(Orders orders) {
		orderProcessorV0.processOrder(orders);
	}


	/// 여러 주문 순차 처리:
	/// - 주문 1건 = 트랜잭션 1개(@Transactional)
	/// - self-invocation 문제를 피하려고 "다른 빈(OrderProcessorV0)"을 호출한다
	public void processOrders(List<Orders> orders) {
		for (Orders order : orders) {
			try {
				orderProcessorV0.processOrder(order); // 프록시 경유 -> @Transactional 적용
			} catch (Exception e) {
				log.warn("주문 실패: {}", order.getOrderId());
			}
		}
	}



	// 조회 메서드들
	public List<Orders> getCompletedOrders() {
		return orderRepository.findByStatus(OrderStatus.COMPLETED);
	}

	public List<Orders> getFailedOrders() {
		return orderRepository.findByStatus(OrderStatus.FAILED);
	}

	public long getCompletedOrderCount() {
		return orderRepository.countByStatus(OrderStatus.COMPLETED);
	}

	public long getFailedOrderCount() {
		return orderRepository.countByStatus(OrderStatus.FAILED);
	}

	public void clearOrders() {
		orderRepository.deleteAll();
	}

}
