-- 创建示例表
CREATE TABLE IF NOT EXISTS customers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    customer_id INT REFERENCES customers(id),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS order_items (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders(id),
    product_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

-- 插入示例数据
INSERT INTO customers (name, email) VALUES
    ('张三', 'zhangsan@example.com'),
    ('李四', 'lisi@example.com'),
    ('王五', 'wangwu@example.com'),
    ('赵六', 'zhaoliu@example.com'),
    ('钱七', 'qianqi@example.com');

INSERT INTO orders (customer_id, total_amount, status) VALUES
    (1, 100.50, 'COMPLETED'),
    (2, 200.75, 'PENDING'),
    (3, 150.25, 'PROCESSING'),
    (4, 300.00, 'COMPLETED'),
    (5, 450.80, 'PENDING'),
    (1, 120.30, 'PROCESSING');

INSERT INTO order_items (order_id, product_name, quantity, price) VALUES
    (1, '手机', 1, 100.50),
    (2, '耳机', 2, 50.25),
    (2, '充电器', 1, 100.25),
    (3, '电脑', 1, 150.25),
    (4, '平板', 1, 300.00),
    (5, '键盘', 2, 125.40),
    (5, '鼠标', 2, 100.00),
    (5, '显示器', 1, 225.40),
    (6, '手表', 1, 120.30);

-- 创建更新触发器函数
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为customers表添加更新触发器
CREATE TRIGGER update_customers_modtime
BEFORE UPDATE ON customers
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- 配置PostgreSQL的逻辑复制
ALTER SYSTEM SET wal_level = 'logical';
ALTER SYSTEM SET max_replication_slots = 5;
ALTER SYSTEM SET max_wal_senders = 10;

-- 注意：上述ALTER SYSTEM命令需要重启PostgreSQL才能生效 