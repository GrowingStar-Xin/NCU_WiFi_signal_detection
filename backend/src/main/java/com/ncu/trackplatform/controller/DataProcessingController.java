package com.ncu.trackplatform.controller;

import com.ncu.trackplatform.service.DataProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "false")
public class DataProcessingController {
    
    @Autowired
    private DataProcessingService dataProcessingService;
    
    /**
     * 清空所有数据
     */
    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllData() {
        Map<String, Object> response = new HashMap<>();
        try {
            dataProcessingService.clearAllData();
            response.put("code", 200);
            response.put("message", "数据清空成功");
            response.put("data", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "数据清空失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 处理数据文件（暂时禁用）
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processDataFiles() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 501);
        response.put("message", "数据处理功能暂时不可用");
        response.put("data", null);
        return ResponseEntity.status(501).body(response);
    }
    
    /**
     * 获取数据统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getDataStatistics() {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("开始调用 dataProcessingService.getDataStatistics()");
            String statistics = dataProcessingService.getDataStatistics();
            System.out.println("成功获取统计信息: " + statistics);
            response.put("code", 200);
            response.put("message", "获取统计信息成功");
            response.put("data", statistics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("获取统计信息时发生错误: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
}