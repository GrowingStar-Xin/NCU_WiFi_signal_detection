package com.ncu.trackplatform.controller;

import com.ncu.trackplatform.dto.TrackDataDto;
import com.ncu.trackplatform.dto.TrackPointDto;
import com.ncu.trackplatform.service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tracks")
public class TrackController {
    
    @Autowired
    private TrackService trackService;
    
    /**
     * 获取所有轨迹列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTracks() {
        try {
            List<TrackDataDto> tracks = trackService.getAllTracks();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取轨迹列表成功");
            response.put("data", tracks);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取轨迹列表失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 根据轨迹ID获取轨迹点
     */
    @GetMapping("/{trackId}/points")
    public ResponseEntity<Map<String, Object>> getTrackPoints(@PathVariable String trackId) {
        try {
            List<TrackPointDto> points = trackService.getTrackPoints(trackId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取轨迹点成功");
            response.put("data", points);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取轨迹点失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取所有轨迹点
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllTrackPoints() {
        try {
            List<TrackPointDto> points = trackService.getAllTrackPoints();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取所有轨迹点成功");
            response.put("data", points);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取所有轨迹点失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 上传轨迹数据文件
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadTrackData(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "文件不能为空");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            
            String result = trackService.uploadTrackData(file);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", result);
            response.put("data", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "文件上传失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "服务正常运行");
        response.put("data", "OK");
        return ResponseEntity.ok(response);
    }
}