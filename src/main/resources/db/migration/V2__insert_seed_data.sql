-- Insert roles
INSERT INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_USER');

-- Insert admin user (password: admin123)
INSERT INTO users (username, email, password, full_name, role_id) 
VALUES ('admin', 'admin@carsales.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrator', 1);

-- Insert brands
INSERT INTO brands (name, description) VALUES 
('Toyota', 'Thương hiệu ô tô Nhật Bản'),
('Honda', 'Thương hiệu ô tô Nhật Bản'),
('Mercedes-Benz', 'Thương hiệu ô tô Đức'),
('BMW', 'Thương hiệu ô tô Đức'),
('Hyundai', 'Thương hiệu ô tô Hàn Quốc'),
('Kia', 'Thương hiệu ô tô Hàn Quốc'),
('Ford', 'Thương hiệu ô tô Mỹ'),
('Mazda', 'Thương hiệu ô tô Nhật Bản');

-- Insert categories
INSERT INTO categories (name, description) VALUES 
('Sedan', 'Xe sedan 4-5 chỗ'),
('SUV', 'Xe thể thao đa dụng'),
('Hatchback', 'Xe 5 cửa'),
('Pickup', 'Xe bán tải'),
('MPV', 'Xe đa dụng gia đình'),
('Coupe', 'Xe thể thao 2 cửa');
