-- Add indexes for better query performance

-- Users indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role_id ON users(role_id);

-- Cars indexes
CREATE INDEX idx_cars_brand_id ON cars(brand_id);
CREATE INDEX idx_cars_category_id ON cars(category_id);
CREATE INDEX idx_cars_status ON cars(status);
CREATE INDEX idx_cars_price ON cars(price);

-- Orders indexes
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_code ON orders(order_code);

-- Order details indexes
CREATE INDEX idx_order_details_order_id ON order_details(order_id);
CREATE INDEX idx_order_details_car_id ON order_details(car_id);

-- Reviews indexes
CREATE INDEX idx_reviews_car_id ON reviews(car_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);

-- Payments indexes
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
