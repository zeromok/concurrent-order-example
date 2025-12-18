package example.V1;

import static org.assertj.core.api.AssertionsForClassTypes.*;

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

/// ## 목표
/// - 멀티스레드를 도입하면 "재고 = 초기재고 - 성공주문수" 불변조건이 깨질 수 있음을 재현한다.
/// ## 주의
/// - 재현 단계라서 완전 결정적이지 않을 수 있다.
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("V1: 멀티스레드 도입 - Race Condition")
public class OrdersV1Test {
	@Autowired
	private OrderServiceV0 orderServiceV0;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private OrderRepository orderRepository;

	private static final String PRODUCT_ID = "TEST-001";

	@BeforeEach
	void setUp() {
		orderRepository.deleteAll();
		productRepository.deleteAll();
		productRepository.save(new Product(PRODUCT_ID, "테스트 상품", 10_000, 100));
	}

	@Test
	@DisplayName("동시에 주문을 넣으면 재고/성공 주문 수 불변 조건이 깨질 수 있다.")
	void 멀티스레드_주문_레이스_재현() throws Exception {
		int threadCount = 30;   // 동시에 30건 주문
		int attempts = 30;      // 최대 30번 시도해서 한번이라도 깨지면 성공(재현 목적)

		boolean violated = false;

		for (int attempt = 1; attempt <= attempts; attempt++) {
			// 매 시도마다 DB 초기화
			orderRepository.deleteAll();
			productRepository.deleteAll();
			productRepository.save(new Product(PRODUCT_ID, "테스트 상품", 10_000, 100));

			List<Thread> threads = new ArrayList<>();
			for (int i = 0; i < threadCount; i++) {
				Orders order = new Orders(PRODUCT_ID, 1, 10_000);
				Thread t = new Thread(() -> {
					try {
						orderServiceV0.processOrder(order);
					} catch (Exception ignored) {
						// 결제 실패 등은 V0 정책상 있을 수 있음 (성공/실패는 카운트로만 본다)
					}
				});
				threads.add(t);
			}

			// naive start: 동시에 시작하려고 노력은 하지만, 시작 타이밍은 OS 스케줄러에 맡김
			for (Thread t : threads) t.start();
			for (Thread t : threads) t.join();

			Product product = productRepository.findById(PRODUCT_ID).orElseThrow();
			long completed = orderServiceV0.getCompletedOrderCount();
			int expectedStock = 100 - (int) completed;

			if (product.getStock() != expectedStock) {
				violated = true;
				System.out.println("[V1 재현 성공] attempt=" + attempt);
				System.out.println("주문 성공=" + completed);
				System.out.println("실제 재고=" + product.getStock());
				System.out.println("예상 재고=" + expectedStock);
				break;
			}
		}

		// V1의 목적: "깨질 수 있음"을 보여주는 것
		assertThat(violated).isTrue();
	}
}
