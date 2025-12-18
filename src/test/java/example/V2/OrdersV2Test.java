package example.V2;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.concurrent.CountDownLatch;

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
@DisplayName("V2: 스레드 생명주기 제어(동시 시작/종료 대기)")
public class OrdersV2Test {
	@Autowired
	private OrderServiceV0 orderServiceV0;
	@Autowired private ProductRepository productRepository;
	@Autowired private OrderRepository orderRepository;

	private static final String PRODUCT_ID = "TEST-001";

	@BeforeEach
	void setUp() {
		orderRepository.deleteAll();
		productRepository.deleteAll();
		productRepository.save(new Product(PRODUCT_ID, "테스트 상품", 10_000, 100));
	}

	@Test
	@DisplayName("CountDownLatch로 동시 시작을 강제하면 재현이 더 안정적이다")
	void 동시_시작_강제() throws Exception {
		int threadCount = 30;

		CountDownLatch readyGate = new CountDownLatch(threadCount);
		CountDownLatch startGate = new CountDownLatch(1);
		CountDownLatch doneGate = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			Thread t = new Thread(() -> {
				readyGate.countDown();      // 나는 준비됐음
				try {
					startGate.await();        // 출발 신호 대기
					Orders order = new Orders(PRODUCT_ID, 1, 10_000);
					orderServiceV0.processOrder(order);
				} catch (Exception ignored) {
					// 결제 실패 등은 있을 수 있음
				} finally {
					doneGate.countDown();     // 작업 종료
				}
			});
			t.start();
		}

		// 모든 스레드가 출발선에 모일 때까지 기다림
		readyGate.await();

		// 동시에 출발!
		startGate.countDown();

		// 모두 끝날 때까지 대기(=join 효과)
		doneGate.await();

		Product product = productRepository.findById(PRODUCT_ID).orElseThrow();
		long completed = orderServiceV0.getCompletedOrderCount();
		int expectedStock = 100 - (int) completed;

		// V2의 포인트: 관측이 안정적이어야 함(출발/종료 제어)
		// 레이스가 실제로 깨지는지는 V1/V2에서 '관측'한다.
		System.out.println("completed=" + completed);
		System.out.println("stock(actual)=" + product.getStock());
		System.out.println("stock(expected)=" + expectedStock);


		assertThat(product.getStock()).isBetween(0, 100);
	}
}
