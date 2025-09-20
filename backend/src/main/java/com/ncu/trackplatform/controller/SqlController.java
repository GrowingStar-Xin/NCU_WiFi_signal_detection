package com.ncu.trackplatform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sql")
public class SqlController {
    
    @Autowired
    private DataSource dataSource;
    
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeSql() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 先创建表
            String createTableSql = "CREATE TABLE IF NOT EXISTS TRACK_POINTS (" +
                "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "ACCOUNT_ID VARCHAR(255) NOT NULL, " +
                "TRACK_ID VARCHAR(255), " +
                "LATITUDE DOUBLE NOT NULL, " +
                "LONGITUDE DOUBLE NOT NULL, " +
                "TIMESTAMP TIMESTAMP NOT NULL, " +
                "ACCURACY DOUBLE, " +
                "SPEED DOUBLE" +
                ")";
            statement.executeUpdate(createTableSql);
            
            // 插入测试数据
            String[] insertStatements = {
                "INSERT INTO TRACK_POINTS (ACCOUNT_ID, TRACK_ID, LATITUDE, LONGITUDE, TIMESTAMP) VALUES ('user001', 'track_001', 28.6639, 115.9932, '2025-09-20 13:13:00')",
                "INSERT INTO TRACK_POINTS (ACCOUNT_ID, TRACK_ID, LATITUDE, LONGITUDE, TIMESTAMP) VALUES ('user001', 'track_001', 28.6642, 115.9935, '2025-09-20 13:18:00')",
                "INSERT INTO TRACK_POINTS (ACCOUNT_ID, TRACK_ID, LATITUDE, LONGITUDE, TIMESTAMP) VALUES ('user001', 'track_001', 28.6645, 115.9938, '2025-09-20 13:23:00')",
                "INSERT INTO TRACK_POINTS (ACCOUNT_ID, TRACK_ID, LATITUDE, LONGITUDE, TIMESTAMP) VALUES ('user001', 'track_001', 28.6648, 115.9941, '2025-09-20 13:28:00')",
                "INSERT INTO TRACK_POINTS (ACCOUNT_ID, TRACK_ID, LATITUDE, LONGITUDE, TIMESTAMP) VALUES ('user002', 'track_002', 28.6650, 115.9920, '2025-09-20 13:33:00')",
                "INSERT INTO TRACK_POINTS (ACCOUNT_ID, TRACK_ID, LATITUDE, LONGITUDE, TIMESTAMP) VALUES ('user002', 'track_002', 28.6653, 115.9923, '2025-09-20 13:38:00')",
                "INSERT INTO TRACK_POINTS (ACCOUNT_ID, TRACK_ID, LATITUDE, LONGITUDE, TIMESTAMP) VALUES ('user002', 'track_002', 28.6656, 115.9926, '2025-09-20 13:43:00')",
                "INSERT INTO TRACK_POINTS (ACCOUNT_ID, TRACK_ID, LATITUDE, LONGITUDE, TIMESTAMP) VALUES ('user002', 'track_002', 28.6659, 115.9929, '2025-09-20 13:48:00')",
                "INSERT INTO TRACK_POINTS (ACCOUNT_ID, TRACK_ID, LATITUDE, LONGITUDE, TIMESTAMP) VALUES ('user003', 'track_003', 28.6630, 115.9910, '2025-09-20 13:53:00')",
                "INSERT INTO TRACK_POINTS (ACCOUNT_ID, TRACK_ID, LATITUDE, LONGITUDE, TIMESTAMP) VALUES ('user003', 'track_003', 28.6633, 115.9913, '2025-09-20 13:58:00')",
                "INSERT INTO TRACK_POINTS (ACCOUNT_ID, TRACK_ID, LATITUDE, LONGITUDE, TIMESTAMP) VALUES ('user003', 'track_003', 28.6636, 115.9916, '2025-09-20 14:03:00')",
                "INSERT INTO TRACK_POINTS (ACCOUNT_ID, TRACK_ID, LATITUDE, LONGITUDE, TIMESTAMP) VALUES ('user003', 'track_003', 28.6639, 115.9919, '2025-09-20 14:08:00')"
            };
            
            int totalInserted = 0;
            for (String sql : insertStatements) {
                int result = statement.executeUpdate(sql);
                totalInserted += result;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "SQL执行成功");
            response.put("data", Map.of("insertedRows", totalInserted));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "SQL执行失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCount() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            var resultSet = statement.executeQuery("SELECT COUNT(*) as count FROM TRACK_POINTS");
            int count = 0;
            if (resultSet.next()) {
                count = resultSet.getInt("count");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", Map.of("count", count));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}