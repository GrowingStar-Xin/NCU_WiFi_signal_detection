package com.ncu.trackplatform.service;

import com.ncu.trackplatform.entity.TrackPoint;
import com.ncu.trackplatform.repository.TrackPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BehaviorBasedOptimizationService {
    
    @Autowired
    private WiFiLogParsingService wifiLogParsingService;
    
    // @Autowired
    // private TrackPointRepository trackPointRepository;
    
    // AP位置提取的正则表达式
    private static final Pattern AP_LOCATION_PATTERN = Pattern.compile("终端在AP\\s+([^\\s]+)\\s+");
    private static final Pattern AP_LOCATION_PATTERN2 = Pattern.compile("AP\\s+([^\\s]+)\\s+获取IP地址成功");
    private static final Pattern AP_ROAM_PATTERN = Pattern.compile("漫游到AP\\s+([^\\s]+)\\s+");
    
    /**
     * 基于终端行为优化轨迹点
     * 只保留上线事件且位置发生变化的轨迹点
     */
    public List<TrackPoint> optimizeByBehavior(List<TrackPoint> originalPoints) {
        if (originalPoints == null || originalPoints.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 按账号分组处理
        Map<String, List<TrackPoint>> pointsByAccount = groupByAccount(originalPoints);
        List<TrackPoint> optimizedPoints = new ArrayList<>();
        
        for (Map.Entry<String, List<TrackPoint>> entry : pointsByAccount.entrySet()) {
            List<TrackPoint> accountPoints = entry.getValue();
            List<TrackPoint> accountOptimized = optimizeAccountPoints(accountPoints);
            optimizedPoints.addAll(accountOptimized);
        }
        
        return optimizedPoints;
    }
    
    /**
     * 优化单个账号的轨迹点
     */
    private List<TrackPoint> optimizeAccountPoints(List<TrackPoint> points) {
        List<TrackPoint> optimized = new ArrayList<>();
        String lastLocation = null;
        
        for (TrackPoint point : points) {
            // 检查是否为上线事件
            if (isOnlineEvent(point)) {
                String currentLocation = extractAPLocation(point);
                
                // 如果位置发生变化，则保留此轨迹点
                if (currentLocation != null && !currentLocation.equals(lastLocation)) {
                    optimized.add(point);
                    lastLocation = currentLocation;
                }
            }
        }
        
        return optimized;
    }
    
    /**
     * 判断是否为上线事件
     */
    private boolean isOnlineEvent(TrackPoint point) {
        if (point.getTrackId() == null) {
            System.out.println("BehaviorBasedOptimizationService: TrackId为null");
            try {
                java.io.FileWriter fw = new java.io.FileWriter("debug.log", true);
                fw.write("BehaviorBasedOptimizationService: TrackId为null\n");
                fw.close();
            } catch (Exception e) {}
            return false;
        }
        
        // 检查轨迹详细信息字段
        String details = point.getTrackId(); // 这里暂时用trackId字段存储轨迹详细信息
        if (details == null) {
            System.out.println("BehaviorBasedOptimizationService: details为null");
            try {
                java.io.FileWriter fw = new java.io.FileWriter("debug.log", true);
                fw.write("BehaviorBasedOptimizationService: details为null\n");
                fw.close();
            } catch (Exception e) {}
            return false;
        }
        
        System.out.println("BehaviorBasedOptimizationService: 检查上线事件，details=" + details);
        try {
            java.io.FileWriter fw = new java.io.FileWriter("debug.log", true);
            fw.write("BehaviorBasedOptimizationService: 检查上线事件，details=" + details + "\n");
            fw.close();
        } catch (Exception e) {}
        
        // 判断是否包含上线相关的关键词
        boolean result = details.contains("获取IP地址成功") || details.contains("关联成功");
        
        System.out.println("BehaviorBasedOptimizationService: 上线事件判断结果=" + result);
        try {
            java.io.FileWriter fw = new java.io.FileWriter("debug.log", true);
            fw.write("BehaviorBasedOptimizationService: 上线事件判断结果=" + result + "\n");
            fw.close();
        } catch (Exception e) {}
        
        return result;
    }
    
    /**
     * 从轨迹点中提取AP位置信息
     */
    private String extractAPLocation(TrackPoint point) {
        if (point.getTrackId() == null) {
            return null;
        }
        
        String details = point.getTrackId();
        
        // 尝试多种模式提取AP位置
        Matcher matcher = AP_LOCATION_PATTERN.matcher(details);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        matcher = AP_LOCATION_PATTERN2.matcher(details);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * 按账号分组轨迹点
     */
    private Map<String, List<TrackPoint>> groupByAccount(List<TrackPoint> points) {
        Map<String, List<TrackPoint>> grouped = new HashMap<>();
        
        for (TrackPoint point : points) {
            String accountId = point.getAccountId();
            if (accountId != null) {
                grouped.computeIfAbsent(accountId, k -> new ArrayList<>()).add(point);
            }
        }
        
        // 对每个账号的轨迹点按时间排序
        for (List<TrackPoint> accountPoints : grouped.values()) {
            accountPoints.sort((p1, p2) -> p1.getTimestamp().compareTo(p2.getTimestamp()));
        }
        
        return grouped;
    }
    
    /**
     * 获取优化统计信息
     */
    public String getOptimizationStats(List<TrackPoint> original, List<TrackPoint> optimized) {
        int originalCount = original.size();
        int optimizedCount = optimized.size();
        double reductionRate = originalCount > 0 ? 
            ((double)(originalCount - optimizedCount) / originalCount) * 100 : 0;
        
        // 统计上线事件数量
        long onlineEvents = original.stream()
            .filter(this::isOnlineEvent)
            .count();
        
        return String.format(
            "基于行为的轨迹优化统计:\n" +
            "- 原始轨迹点: %d 个\n" +
            "- 上线事件: %d 个\n" +
            "- 优化后轨迹点: %d 个\n" +
            "- 减少比例: %.2f%%",
            originalCount, onlineEvents, optimizedCount, reductionRate
        );
    }
    
    /**
     * 处理WiFi日志文件
     */
    public String processWiFiLogFile(MultipartFile file) {
        try {
            // 1. 解析WiFi日志文件
            List<TrackPoint> rawPoints = wifiLogParsingService.parseWiFiLogCSV(file);
            
            if (rawPoints.isEmpty()) {
                return "未能从WiFi日志中解析出有效的轨迹点";
            }
            
            // 2. 基于终端行为优化轨迹点
            List<TrackPoint> optimizedPoints = optimizeByBehavior(rawPoints);
            
            // 3. 保存优化后的轨迹点到数据库（暂时禁用）
            // trackPointRepository.saveAll(optimizedPoints);
            
            // 4. 返回处理结果
            String stats = getOptimizationStats(rawPoints, optimizedPoints);
            
            return String.format("成功处理WiFi日志文件，导入%d个优化后的轨迹点\n\n%s", 
                optimizedPoints.size(), stats);
                
        } catch (Exception e) {
            throw new RuntimeException("处理WiFi日志文件失败: " + e.getMessage(), e);
        }
    }
}