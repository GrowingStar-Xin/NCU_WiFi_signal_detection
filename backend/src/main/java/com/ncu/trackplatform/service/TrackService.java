package com.ncu.trackplatform.service;

import com.ncu.trackplatform.dto.TrackDataDto;
import com.ncu.trackplatform.dto.TrackPointDto;
import com.ncu.trackplatform.entity.TrackPoint;
import com.ncu.trackplatform.repository.TrackPointRepository;
import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrackService {
    
    @Autowired
    private TrackPointRepository trackPointRepository;
    
    @Autowired
    private TrackOptimizationService optimizationService;
    
    /**
     * 获取所有轨迹列表
     */
    public List<TrackDataDto> getAllTracks() {
        List<String> accountIds = trackPointRepository.findDistinctAccountIds();
        List<TrackDataDto> tracks = new ArrayList<>();
        
        for (String accountId : accountIds) {
            List<TrackPoint> points = trackPointRepository.findByAccountIdOrderByTimestampAsc(accountId);
            if (!points.isEmpty()) {
                TrackDataDto track = new TrackDataDto();
                track.setId(accountId);
                track.setName("账号 " + accountId + " 的轨迹");
                track.setAccountId(accountId);
                track.setStartTime(points.get(0).getTimestamp());
                track.setEndTime(points.get(points.size() - 1).getTimestamp());
                track.setTotalPoints(points.size());
                tracks.add(track);
            }
        }
        
        return tracks;
    }
    
    /**
     * 根据轨迹ID获取轨迹点（优化版本）
     */
    public List<TrackPointDto> getTrackPoints(String trackId) {
        List<TrackPoint> points = trackPointRepository.findByAccountIdOrderByTimestampAsc(trackId);
        
        // 应用轨迹优化
        List<TrackPoint> optimizedPoints = optimizationService.optimizeTrackPoints(points);
        
        // 打印优化统计信息
        System.out.println("轨迹 " + trackId + " - " + 
                          optimizationService.getOptimizationStats(points, optimizedPoints));
        
        return optimizedPoints.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * 获取所有轨迹点（优化版本）
     */
    public List<TrackPointDto> getAllTrackPoints() {
        List<TrackPoint> points = trackPointRepository.findAll();
        
        // 按账号分组并分别优化
        List<TrackPoint> allOptimizedPoints = new ArrayList<>();
        List<String> accountIds = trackPointRepository.findDistinctAccountIds();
        
        for (String accountId : accountIds) {
            List<TrackPoint> accountPoints = trackPointRepository.findByAccountIdOrderByTimestampAsc(accountId);
            List<TrackPoint> optimizedPoints = optimizationService.optimizeTrackPoints(accountPoints);
            allOptimizedPoints.addAll(optimizedPoints);
            
            // 打印优化统计信息
            System.out.println("账号 " + accountId + " - " + 
                              optimizationService.getOptimizationStats(accountPoints, optimizedPoints));
        }
        
        return allOptimizedPoints.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * 获取原始轨迹点（未优化）
     */
    public List<TrackPointDto> getRawTrackPoints(String trackId) {
        List<TrackPoint> points = trackPointRepository.findByAccountIdOrderByTimestampAsc(trackId);
        return points.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * 获取所有原始轨迹点（未优化）
     */
    public List<TrackPointDto> getAllRawTrackPoints() {
        List<TrackPoint> points = trackPointRepository.findAll();
        return points.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * 上传CSV文件并解析轨迹数据
     */
    public String uploadTrackData(MultipartFile file) {
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()));
            String[] headers = reader.readNext(); // 读取表头
            
            List<TrackPoint> trackPoints = new ArrayList<>();
            String[] line;
            
            while ((line = reader.readNext()) != null) {
                TrackPoint point = parseCSVLine(line, headers);
                if (point != null) {
                    trackPoints.add(point);
                }
            }
            
            trackPointRepository.saveAll(trackPoints);
            reader.close();
            
            return "成功导入 " + trackPoints.size() + " 个轨迹点";
            
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析CSV行数据
     */
    private TrackPoint parseCSVLine(String[] line, String[] headers) {
        try {
            TrackPoint point = new TrackPoint();
            
            for (int i = 0; i < headers.length && i < line.length; i++) {
                String header = headers[i].toLowerCase().trim();
                String value = line[i].trim();
                
                switch (header) {
                    case "account_id":
                    case "accountid":
                    case "user_id":
                    case "userid":
                        point.setAccountId(value);
                        break;
                    case "latitude":
                    case "lat":
                        point.setLatitude(Double.parseDouble(value));
                        break;
                    case "longitude":
                    case "lng":
                    case "lon":
                        point.setLongitude(Double.parseDouble(value));
                        break;
                    case "timestamp":
                    case "time":
                    case "datetime":
                        point.setTimestamp(parseTimestamp(value));
                        break;
                    case "accuracy":
                        point.setAccuracy(Double.parseDouble(value));
                        break;
                    case "speed":
                        point.setSpeed(Double.parseDouble(value));
                        break;
                    case "track_id":
                    case "trackid":
                        point.setTrackId(value);
                        break;
                }
            }
            
            // 验证必要字段
            if (point.getAccountId() != null && point.getLatitude() != null && 
                point.getLongitude() != null && point.getTimestamp() != null) {
                return point;
            }
            
        } catch (Exception e) {
            System.err.println("解析CSV行失败: " + String.join(",", line) + " - " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 解析时间戳
     */
    private LocalDateTime parseTimestamp(String timestamp) {
        try {
            // 尝试多种时间格式
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
            };
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDateTime.parse(timestamp, formatter);
                } catch (Exception ignored) {
                }
            }
            
            // 如果是时间戳（毫秒）
            if (timestamp.matches("\\d+")) {
                long millis = Long.parseLong(timestamp);
                if (millis > 1000000000000L) { // 毫秒时间戳
                    return LocalDateTime.ofEpochSecond(millis / 1000, 0, java.time.ZoneOffset.UTC);
                } else { // 秒时间戳
                    return LocalDateTime.ofEpochSecond(millis, 0, java.time.ZoneOffset.UTC);
                }
            }
            
        } catch (Exception e) {
            System.err.println("时间戳解析失败: " + timestamp);
        }
        
        return LocalDateTime.now(); // 默认返回当前时间
    }
    
    /**
     * 实体转DTO
     */
    private TrackPointDto convertToDto(TrackPoint point) {
        TrackPointDto dto = new TrackPointDto();
        dto.setId(point.getId());
        dto.setAccountId(point.getAccountId());
        dto.setLatitude(point.getLatitude());
        dto.setLongitude(point.getLongitude());
        dto.setTimestamp(point.getTimestamp());
        dto.setAccuracy(point.getAccuracy());
        dto.setSpeed(point.getSpeed());
        dto.setTrackId(point.getTrackId());
        return dto;
    }
}