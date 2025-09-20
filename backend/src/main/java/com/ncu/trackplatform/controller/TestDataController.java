package com.ncu.trackplatform.controller;

import com.ncu.trackplatform.entity.TrackPoint;
import com.ncu.trackplatform.repository.TrackPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test-data")
public class TestDataController {
    
    @Autowired
    private TrackPointRepository trackPointRepository;
    
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initTestData() {
        try {
            // 不清空数据，直接添加
            
            List<TrackPoint> testPoints = new ArrayList<>();
            LocalDateTime baseTime = LocalDateTime.now().minusHours(2);
            
            // 创建用户1的轨迹数据 - 校园内移动轨迹
            String user1 = "user001";
            String track1 = "track_001";
            
            // 模拟从图书馆到教学楼的轨迹
            testPoints.add(createTrackPoint(user1, track1, 28.6639, 115.9932, baseTime)); // 图书馆
            testPoints.add(createTrackPoint(user1, track1, 28.6642, 115.9935, baseTime.plusMinutes(5))); // 路径点1
            testPoints.add(createTrackPoint(user1, track1, 28.6645, 115.9938, baseTime.plusMinutes(10))); // 路径点2
            testPoints.add(createTrackPoint(user1, track1, 28.6648, 115.9941, baseTime.plusMinutes(15))); // 教学楼
            
            // 创建用户2的轨迹数据 - 宿舍到食堂
            String user2 = "user002";
            String track2 = "track_002";
            
            testPoints.add(createTrackPoint(user2, track2, 28.6650, 115.9920, baseTime.plusMinutes(20))); // 宿舍
            testPoints.add(createTrackPoint(user2, track2, 28.6653, 115.9923, baseTime.plusMinutes(25))); // 路径点1
            testPoints.add(createTrackPoint(user2, track2, 28.6656, 115.9926, baseTime.plusMinutes(30))); // 路径点2
            testPoints.add(createTrackPoint(user2, track2, 28.6659, 115.9929, baseTime.plusMinutes(35))); // 食堂
            
            // 创建用户3的轨迹数据 - 体育馆周围
            String user3 = "user003";
            String track3 = "track_003";
            
            testPoints.add(createTrackPoint(user3, track3, 28.6630, 115.9910, baseTime.plusMinutes(40))); // 体育馆入口
            testPoints.add(createTrackPoint(user3, track3, 28.6633, 115.9913, baseTime.plusMinutes(45))); // 体育馆内部
            testPoints.add(createTrackPoint(user3, track3, 28.6636, 115.9916, baseTime.plusMinutes(50))); // 运动场
            testPoints.add(createTrackPoint(user3, track3, 28.6639, 115.9919, baseTime.plusMinutes(55))); // 体育馆出口
            
            // 保存所有测试数据
            trackPointRepository.saveAll(testPoints);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "测试数据初始化成功");
            response.put("data", Map.of(
                "totalPoints", testPoints.size(),
                "users", 3,
                "tracks", 3
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "测试数据初始化失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getDataCount() {
        try {
            long count = trackPointRepository.count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取数据统计成功");
            response.put("data", Map.of(
                "totalPoints", count
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取数据统计失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    private TrackPoint createTrackPoint(String accountId, String trackId, double latitude, double longitude, LocalDateTime timestamp) {
        TrackPoint point = new TrackPoint();
        point.setAccountId(accountId);
        point.setTrackId(trackId);
        point.setLatitude(latitude);
        point.setLongitude(longitude);
        point.setTimestamp(timestamp);
        return point;
    }
}