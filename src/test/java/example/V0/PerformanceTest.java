package example.V0;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import example.V0.domain.Orders;
import example.V0.domain.Product;
import example.V0.repository.OrderRepository;
import example.V0.repository.ProductRepository;
import example.V0.service.OrderServiceV0;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("V0: 성능 측정 테스트")
public class PerformanceTest {
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
		testProduct = new Product("TEST-001", "테스트 상품", 10_000, 10_000);
		productRepository.save(testProduct);
	}

	@Test
	@DisplayName("100개 주문 처리 시간 측정 - BASELINE")
	void 주문_100개_처리_시간_베이스라인() {
		// Given
		List<Orders> orders = createOrders(100);

		// When
		Instant start = Instant.now();
		orderServiceV0.processOrders(orders);
		Instant end = Instant.now();

		// Then
		Duration duration = Duration.between(start, end);
		long seconds = duration.getSeconds();

		System.out.println("=".repeat(60));
		System.out.println("V0 성능 측정 - 100개 주문");
		System.out.println("=".repeat(60));
		System.out.println("처리 시간: " + seconds + "초 " + duration.toMillisPart() + "ms");
		System.out.println("성공: " + orderServiceV0.getCompletedOrderCount());
		System.out.println("실패: " + orderServiceV0.getFailedOrderCount());
		System.out.println("예상 시간: 약 35초 (350ms * 100개)");
		System.out.println("평균 처리 속도: " + String.format("%.2f", 100.0 / seconds) + " 주문/초");
		System.out.println("=".repeat(60));
		System.out.println("문제점:");
		System.out.println("- 순차 처리로 인한 긴 대기 시간");
		System.out.println("- CPU 활용률 낮음");
		System.out.println("- DB 트랜잭션 순차 실행");
		System.out.println("=".repeat(60));

		assertThat(seconds).isBetween(30L, 45L);
	}

	private List<Orders> createOrders(int count) {
		List<Orders> orders = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			orders.add(new Orders(testProduct.getProductId(), 1, testProduct.getPrice()));
		}
		return orders;
	}
}
