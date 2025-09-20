package com.ncu.trackplatform.service;

import com.ncu.trackplatform.entity.TrackPoint;
import com.ncu.trackplatform.repository.TrackPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataProcessingService {
    
    // @Autowired
    // private TrackPointRepository trackPointRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 清空轨迹点数据
     */
    @Transactional
    public void clearAllData() {
        // trackPointRepository.deleteAll();
        System.out.println("清空数据功能暂时不可用");
    }
    
    /**
     * 获取数据统计信息
     */
    public String getDataStatistics() {
        // long trackPointCount = trackPointRepository.count();
        // return String.format("数据统计: 轨迹点记录 %d 条", trackPointCount);
        return "数据统计功能暂时不可用";
    }
}