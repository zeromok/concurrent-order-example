package example.V0;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import example.V0.domain.OrderStatus;
import example.V0.domain.Orders;
import example.V0.domain.Product;
import example.V0.repository.OrderRepository;
import example.V0.repository.ProductRepository;
import example.V0.service.OrderServiceV0;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("V0: 단일 스레드 주문 시스템")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrdersV0Test {

	@Autowired
	private OrderServiceV0 orderServiceV0;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private OrderRepository orderRepository;

	private Product testProduct;

	@BeforeEach
	void setUp() {
		// 테스트 전 데이터 정리
		orderRepository.deleteAll();
		productRepository.deleteAll();

		// 테스트용 상품 생성
		testProduct = new Product("TEST-001", "테스트 상품", 10000, 100);
		productRepository.save(testProduct);
	}

	@Test
	@Order(1)
	@DisplayName("단일 주문 정상 처리 - DB에 저장 확인")
	void 단일_주문_정상_처리() {
		// G: 주문 생성
		Orders orders = new Orders(testProduct.getProductId(), 5, testProduct.getPrice());

		// W: 주문 처리
		orderServiceV0.processOrder(orders);

		// T: 재고 100 -> 주문 5 -> 재고 95
		Orders savedOrders = orderRepository.findById(orders.getOrderId())
			.orElseThrow();

		Product updatedProduct = productRepository.findById(testProduct.getProductId())
			.orElseThrow();


		assertThat(savedOrders.getStatus()).isEqualTo(OrderStatus.COMPLETED); // 정상 주문 완료
		assertThat(updatedProduct.getStock()).isEqualTo(95); // 현재 재고
		assertThat(orderServiceV0.getCompletedOrderCount()).isEqualTo(1); // 생성된 주문 수
	}

	@Test
	@Order(2)
	@DisplayName("재고 부족 시 주문 실패 및 롤백")
	void 재고_부족_시_주문_실패() {
		// G: 주문 생성
		Orders orders = new Orders(testProduct.getProductId(), 150, testProduct.getPrice());

		// W
		assertThatThrownBy(() -> orderServiceV0.processOrder(orders))
			.isInstanceOf(RuntimeException.class);

		// T
		Product product = productRepository.findById(testProduct.getProductId())
			.orElseThrow();

		assertThat(product.getStock()).isEqualTo(100); // 실패 -> 롤백 -> 재고 변경 없음
		assertThat(orderServiceV0.getFailedOrderCount()).isEqualTo(1); // 실패 -> 실패 주문 쌓임
	}

	@Test
	@Order(3)
	@DisplayName("여러 주문 순차 처리 - 각각 별도 트랜잭션")
	void 여러_주문_순차_처리() {
		// G: 여러 주문 생성
		List<Orders> orders = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			orders.add(new Orders(testProduct.getProductId(), 1, testProduct.getPrice()));
		}

		// W
		orderServiceV0.processOrders(orders);

		// T
		Product product = productRepository.findById(testProduct.getProductId())
			.orElseThrow();

		long completedOrderCount = orderServiceV0.getCompletedOrderCount();
		assertThat(product.getStock()).isEqualTo(100 - completedOrderCount);
		assertThat(completedOrderCount).isGreaterThanOrEqualTo(8);
	}
}
