-- 创建数据库
CREATE DATABASE IF NOT EXISTS wifi_tracking;

-- 使用数据库
USE wifi_tracking;

-- 创建原始WiFi日志表
CREATE TABLE IF NOT EXISTS wifi_logs (
    id UInt64,
    timestamp DateTime64(3),
    terminal_behavior String,
    track_detail String,
    terminal_ip String,
    ssid String,
    frequency_band String,
    channel String,
    signal_strength String,
    log_content String,
    mac_address String,
    ap_name String,
    file_source String,
    created_at DateTime64(3) DEFAULT now64()
) ENGINE = MergeTree()
ORDER BY (timestamp, mac_address)
PARTITION BY toYYYYMM(timestamp)
SETTINGS index_granularity = 8192;

-- 创建轨迹点表（优化后的数据）
CREATE TABLE IF NOT EXISTS track_points (
    id UInt64,
    account_id String,
    track_id String,
    latitude Float64,
    longitude Float64,
    timestamp DateTime64(3),
    accuracy Float64 DEFAULT 0.0,
    speed Float64 DEFAULT 0.0,
    ap_name String DEFAULT '',
    created_at DateTime64(3) DEFAULT now64()
) ENGINE = MergeTree()
ORDER BY (account_id, timestamp)
PARTITION BY toYYYYMM(timestamp)
SETTINGS index_granularity = 8192;

-- 创建AP位置信息表
CREATE TABLE IF NOT EXISTS ap_locations (
    ap_name String,
    latitude Float64,
    longitude Float64,
    building String DEFAULT '',
    floor String DEFAULT '',
    description String DEFAULT '',
    created_at DateTime64(3) DEFAULT now64()
) ENGINE = ReplacingMergeTree(created_at)
ORDER BY ap_name
SETTINGS index_granularity = 8192;

-- 创建数据导入状态表
CREATE TABLE IF NOT EXISTS import_status (
    file_name String,
    file_path String,
    file_size UInt64,
    records_total UInt64,
    records_imported UInt64,
    records_failed UInt64,
    import_start_time DateTime64(3),
    import_end_time DateTime64(3),
    status Enum8('pending' = 1, 'processing' = 2, 'completed' = 3, 'failed' = 4),
    error_message String DEFAULT '',
    created_at DateTime64(3) DEFAULT now64()
) ENGINE = ReplacingMergeTree(created_at)
ORDER BY file_name
SETTINGS index_granularity = 8192;

-- 插入一些基础AP位置数据
INSERT INTO ap_locations (ap_name, latitude, longitude, building, description) VALUES
('前湖北-艺术楼-b-2f-02', 28.6329, 115.8372, '艺术楼', '前湖北校区艺术楼2楼'),
('前湖北-材料环境楼-C349B-1', 28.6349, 115.8352, '材料环境楼', '前湖北校区材料环境楼C349B'),
('前湖北-理科楼-A区-1F', 28.6339, 115.8362, '理科楼', '前湖北校区理科楼A区1楼'),
('前湖北-理科楼-B区-2F', 28.6341, 115.8364, '理科楼', '前湖北校区理科楼B区2楼'),
('前湖北-图书馆-1F', 28.6355, 115.8345, '图书馆', '前湖北校区图书馆1楼'),
('前湖北-学生活动中心', 28.6365, 115.8335, '学生活动中心', '前湖北校区学生活动中心'),
('前湖北-体育馆', 28.6375, 115.8325, '体育馆', '前湖北校区体育馆'),
('前湖北-食堂-1F', 28.6345, 115.8355, '食堂', '前湖北校区食堂1楼'),
('前湖北-宿舍区-A栋', 28.6325, 115.8375, '宿舍区', '前湖北校区宿舍A栋'),
('前湖北-宿舍区-B栋', 28.6327, 115.8377, '宿舍区', '前湖北校区宿舍B栋');

-- 创建视图：获取有效轨迹统计
CREATE VIEW IF NOT EXISTS track_summary AS
SELECT 
    account_id,
    track_id,
    count() as point_count,
    min(timestamp) as start_time,
    max(timestamp) as end_time,
    round(avg(latitude), 6) as avg_latitude,
    round(avg(longitude), 6) as avg_longitude
FROM track_points 
GROUP BY account_id, track_id
HAVING point_count > 0
ORDER BY start_time DESC;

-- 创建视图：MAC地址统计
CREATE VIEW IF NOT EXISTS mac_address_stats AS
SELECT 
    mac_address,
    count() as log_count,
    min(timestamp) as first_seen,
    max(timestamp) as last_seen,
    uniq(ap_name) as unique_aps,
    groupArray(DISTINCT ap_name) as visited_aps
FROM wifi_logs 
WHERE mac_address != ''
GROUP BY mac_address
ORDER BY log_count DESC;