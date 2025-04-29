CREATE DATABASE scm_system;
USE scm_system;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE suppliers (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL,
    description TEXT,
    supplier_id INT,
    quantity INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 10,
    unit_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
);

CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    supplier_id INT,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    expected_delivery_date DATE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
);

CREATE TABLE order_details (
    order_detail_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT,
    product_id INT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES inventory(product_id)
);

INSERT INTO users (username, password, full_name, email, role)
VALUES
('admin', 'admin123', 'Yashraj Sharma', 'yashraj@gmail.com', 'ADMIN'),
('manager', 'manager123', 'Priya Sharma', 'priya@scm.in', 'MANAGER'),
('staff', 'staff123', 'Amit Patel', 'amit@scm.in', 'STAFF');

INSERT INTO suppliers (supplier_name, contact_person, email, phone, address)
VALUES
('Tech Bharat Pvt. Ltd.', 'Rajesh Gupta', 'rajesh@techbharat.in', '9876543210', '123 MG Road, Bangalore, Karnataka'),
('Office Solutions India', 'Sunita Desai', 'sunita@officesolutions.in', '9123456789', '456 Nehru Street, Mumbai, Maharashtra');

INSERT INTO inventory (product_name, description, supplier_id, quantity, reorder_level, unit_price)
VALUES
('Laptop', 'Business laptop with 16GB RAM', 1, 25, 5, 75000.00),
('Monitor', '24-inch LED monitor', 1, 40, 10, 12000.00),
('Desk Chair', 'Ergonomic office chair', 2, 15, 5, 8000.00),
('Printer', 'Color laser printer', 1, 10, 3, 25000.00);