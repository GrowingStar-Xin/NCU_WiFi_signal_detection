package com.ncu.trackplatform.controller;

import com.ncu.trackplatform.entity.TrackPoint;
import com.ncu.trackplatform.repository.TrackPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/jpa-data")
public class JpaDataController {
    
    @Autowired
    private TrackPointRepository trackPointRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @PostMapping("/init")
    @Transactional
    public ResponseEntity<Map<String, Object>> initData() {
        try {
            // 直接插入数据，不清空现有数据
            
            // 创建测试数据
            List<TrackPoint> testData = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            // 用户1的轨迹数据
            testData.add(createTrackPoint("user001", "track_001", 28.6639, 115.9932, "2025-09-20 13:13:00", formatter));
            testData.add(createTrackPoint("user001", "track_001", 28.6642, 115.9935, "2025-09-20 13:18:00", formatter));
            testData.add(createTrackPoint("user001", "track_001", 28.6645, 115.9938, "2025-09-20 13:23:00", formatter));
            testData.add(createTrackPoint("user001", "track_001", 28.6648, 115.9941, "2025-09-20 13:28:00", formatter));
            
            // 用户2的轨迹数据
            testData.add(createTrackPoint("user002", "track_002", 28.6650, 115.9920, "2025-09-20 13:33:00", formatter));
            testData.add(createTrackPoint("user002", "track_002", 28.6653, 115.9923, "2025-09-20 13:38:00", formatter));
            testData.add(createTrackPoint("user002", "track_002", 28.6656, 115.9926, "2025-09-20 13:43:00", formatter));
            testData.add(createTrackPoint("user002", "track_002", 28.6659, 115.9929, "2025-09-20 13:48:00", formatter));
            
            // 用户3的轨迹数据
            testData.add(createTrackPoint("user003", "track_003", 28.6630, 115.9910, "2025-09-20 13:53:00", formatter));
            testData.add(createTrackPoint("user003", "track_003", 28.6633, 115.9913, "2025-09-20 13:58:00", formatter));
            testData.add(createTrackPoint("user003", "track_003", 28.6636, 115.9916, "2025-09-20 14:03:00", formatter));
            testData.add(createTrackPoint("user003", "track_003", 28.6639, 115.9919, "2025-09-20 14:08:00", formatter));
            
            // 使用EntityManager逐个保存
            int savedCount = 0;
            for (TrackPoint point : testData) {
                entityManager.persist(point);
                savedCount++;
            }
            entityManager.flush();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "测试数据初始化成功");
            response.put("data", Map.of("insertedCount", savedCount));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "测试数据初始化失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCount() {
        try {
            long totalCount = trackPointRepository.count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", Map.of("totalPoints", totalCount));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    private TrackPoint createTrackPoint(String accountId, String trackId, double latitude, double longitude, String timestamp, DateTimeFormatter formatter) {
        TrackPoint point = new TrackPoint();
        point.setAccountId(accountId);
        point.setTrackId(trackId);
        point.setLatitude(latitude);
        point.setLongitude(longitude);
        point.setTimestamp(LocalDateTime.parse(timestamp, formatter));
        return point;
    }
}