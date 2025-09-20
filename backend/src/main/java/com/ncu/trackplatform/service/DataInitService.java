package com.ncu.trackplatform.service;

import com.ncu.trackplatform.entity.TrackPoint;
import com.ncu.trackplatform.repository.TrackPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 暂时禁用数据初始化
// @Service
public class DataInitService implements CommandLineRunner {
    
    // @Autowired
    // private TrackPointRepository trackPointRepository;
    
    public void run(String... args) throws Exception {
        System.out.println("数据初始化功能暂时禁用");
    }
    
    /*
    private void initializeTestData() {
        List<TrackPoint> testPoints = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusHours(2);
        
        // 创建用户1的轨迹数据 - 校园内移动轨迹
        String user1 = "user001";
        String track1 = "track_001";
        
        // 模拟从图书馆到教学楼的轨迹
        testPoints.add(new TrackPoint(user1, 28.6639, 115.9932, baseTime)); // 图书馆
        testPoints.add(new TrackPoint(user1, 28.6642, 115.9935, baseTime.plusMinutes(5))); // 路径点1
        testPoints.add(new TrackPoint(user1, 28.6645, 115.9938, baseTime.plusMinutes(10))); // 路径点2
        testPoints.add(new TrackPoint(user1, 28.6648, 115.9941, baseTime.plusMinutes(15))); // 教学楼
        
        // 设置轨迹ID
        testPoints.forEach(point -> point.setTrackId(track1));
        
        // 创建用户2的轨迹数据 - 宿舍到食堂
        String user2 = "user002";
        String track2 = "track_002";
        
        testPoints.add(new TrackPoint(user2, 28.6650, 115.9920, baseTime.plusMinutes(20))); // 宿舍
        testPoints.add(new TrackPoint(user2, 28.6653, 115.9923, baseTime.plusMinutes(25))); // 路径点1
        testPoints.add(new TrackPoint(user2, 28.6656, 115.9926, baseTime.plusMinutes(30))); // 路径点2
        testPoints.add(new TrackPoint(user2, 28.6659, 115.9929, baseTime.plusMinutes(35))); // 食堂
        
        testPoints.forEach(point -> {
            if (point.getAccountId().equals(user2)) {
                point.setTrackId(track2);
            }
        });
        
        // 创建用户3的轨迹数据 - 体育馆周围
        String user3 = "user003";
        String track3 = "track_003";
        
        testPoints.add(new TrackPoint(user3, 28.6630, 115.9910, baseTime.plusMinutes(40))); // 体育馆入口
        testPoints.add(new TrackPoint(user3, 28.6633, 115.9913, baseTime.plusMinutes(45))); // 体育馆内部
        testPoints.add(new TrackPoint(user3, 28.6636, 115.9916, baseTime.plusMinutes(50))); // 运动场
        testPoints.add(new TrackPoint(user3, 28.6639, 115.9919, baseTime.plusMinutes(55))); // 体育馆出口
        
        testPoints.forEach(point -> {
            if (point.getAccountId().equals(user3)) {
                point.setTrackId(track3);
            }
        });
        
        // 保存所有测试数据
        trackPointRepository.saveAll(testPoints);
        
        System.out.println("已创建 " + testPoints.size() + " 个轨迹点，涵盖 3 个用户的轨迹数据");
    }
    */
}