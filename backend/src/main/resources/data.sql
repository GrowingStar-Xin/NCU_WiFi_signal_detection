-- 创建轨迹点表
CREATE TABLE IF NOT EXISTS track_points (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id VARCHAR(255) NOT NULL,
    track_id VARCHAR(255),
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    accuracy DOUBLE,
    speed DOUBLE
);

-- 插入测试数据
INSERT INTO track_points (account_id, track_id, latitude, longitude, timestamp) VALUES
-- 用户1的轨迹数据 - 校园内移动轨迹
('user001', 'track_001', 28.6639, 115.9932, '2025-09-20 13:13:00'),
('user001', 'track_001', 28.6642, 115.9935, '2025-09-20 13:18:00'),
('user001', 'track_001', 28.6645, 115.9938, '2025-09-20 13:23:00'),
('user001', 'track_001', 28.6648, 115.9941, '2025-09-20 13:28:00'),

-- 用户2的轨迹数据 - 宿舍到食堂
('user002', 'track_002', 28.6650, 115.9920, '2025-09-20 13:33:00'),
('user002', 'track_002', 28.6653, 115.9923, '2025-09-20 13:38:00'),
('user002', 'track_002', 28.6656, 115.9926, '2025-09-20 13:43:00'),
('user002', 'track_002', 28.6659, 115.9929, '2025-09-20 13:48:00'),

-- 用户3的轨迹数据 - 体育馆周围
('user003', 'track_003', 28.6630, 115.9910, '2025-09-20 13:53:00'),
('user003', 'track_003', 28.6633, 115.9913, '2025-09-20 13:58:00'),
('user003', 'track_003', 28.6636, 115.9916, '2025-09-20 14:03:00'),
('user003', 'track_003', 28.6639, 115.9919, '2025-09-20 14:08:00');