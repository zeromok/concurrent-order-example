package example.V0.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import example.V0.domain.Product;

public interface ProductRepository extends JpaRepository<Product, String> {

	@Query("SELECT COALESCE(SUM(p.stock), 0) FROM Product p")
	int getTotalStock();
}
