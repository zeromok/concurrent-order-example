package example.V0.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import example.V0.domain.Orders;
import example.V0.domain.OrderStatus;

public interface OrderRepository extends JpaRepository<Orders, String> {

	List<Orders> findByStatus(OrderStatus status);

	long countByStatus(OrderStatus status);
}
