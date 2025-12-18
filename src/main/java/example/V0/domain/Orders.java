package example.V0.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders {

	@Id
	@Column(length = 36)
	private String orderId;

	@Column(nullable = false, length = 36)
	private String productId;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false)
	private long price;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column
	private LocalDateTime updatedAt;

	public Orders(String productId, int quantity, long price) {
		this.orderId = UUID.randomUUID().toString();
		this.productId = productId;
		this.quantity = quantity;
		this.price = price;
		this.status = OrderStatus.PENDING;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public long getTotalPrice() {
		return price * quantity;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
		this.updatedAt = LocalDateTime.now();
	}
}
