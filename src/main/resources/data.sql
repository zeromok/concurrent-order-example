-- 초기 상품 데이터 삽입
INSERT INTO product (product_id, name, price, stock) VALUES
                                                         ('PROD-001', 'MacBook Pro 16', 3290000, 50),
                                                         ('PROD-002', 'iPhone 15 Pro', 1550000, 100),
                                                         ('PROD-003', 'AirPods Pro', 359000, 200),
                                                         ('PROD-004', 'iPad Air', 929000, 80),
                                                         ('PROD-005', 'Apple Watch Ultra', 1149000, 60);

-- 테스트용 대량 재고 상품
INSERT INTO product (product_id, name, price, stock) VALUES
    ('PROD-999', 'Test Product (Large Stock)', 10000, 10000);