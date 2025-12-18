package example.V0.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "product")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

	@Id
	@Column(length = 36)
	private String productId;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false)
	private long price;

	@Column(nullable = false)
	private int stock;

	public Product(String productId, String name, long price, int stock) {
		this.productId = productId;
		this.name = name;
		this.price = price;
		this.stock = stock;
	}

	public void decreaseStock(int quantity) {
		if (stock < quantity) {
			throw new IllegalStateException(
				String.format("재고 부족: 현재 %d개, 요청 %d개", stock, quantity)
			);
		}
		this.stock -= quantity;
	}

	public void increaseStock(int quantity) {
		this.stock += quantity;
	}
}
