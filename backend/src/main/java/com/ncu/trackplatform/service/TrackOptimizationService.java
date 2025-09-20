package com.ncu.trackplatform.service;

import com.ncu.trackplatform.entity.TrackPoint;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrackOptimizationService {
    
    // 最小距离阈值（米）- 只有移动距离超过此值才记录新点
    private static final double MIN_DISTANCE_THRESHOLD = 5.0;
    
    // 最小时间间隔（秒）- 只有时间间隔超过此值才可能记录新点
    private static final long MIN_TIME_INTERVAL_SECONDS = 30;
    
    // 最大时间间隔（秒）- 超过此时间必须记录新点，即使位置没变化
    private static final long MAX_TIME_INTERVAL_SECONDS = 300; // 5分钟
    
    /**
     * 优化轨迹点列表，去除冗余点
     * @param originalPoints 原始轨迹点列表
     * @return 优化后的轨迹点列表
     */
    public List<TrackPoint> optimizeTrackPoints(List<TrackPoint> originalPoints) {
        if (originalPoints == null || originalPoints.size() <= 2) {
            return originalPoints;
        }
        
        List<TrackPoint> optimizedPoints = new ArrayList<>();
        
        // 总是保留第一个点
        optimizedPoints.add(originalPoints.get(0));
        TrackPoint lastKeptPoint = originalPoints.get(0);
        
        for (int i = 1; i < originalPoints.size(); i++) {
            TrackPoint currentPoint = originalPoints.get(i);
            
            // 检查是否应该保留当前点
            if (shouldKeepPoint(lastKeptPoint, currentPoint)) {
                optimizedPoints.add(currentPoint);
                lastKeptPoint = currentPoint;
            }
        }
        
        // 总是保留最后一个点（如果它不是已经保留的最后一个点）
        TrackPoint lastOriginalPoint = originalPoints.get(originalPoints.size() - 1);
        if (!lastKeptPoint.equals(lastOriginalPoint)) {
            optimizedPoints.add(lastOriginalPoint);
        }
        
        return optimizedPoints;
    }
    
    /**
     * 判断是否应该保留当前轨迹点
     * @param lastPoint 上一个保留的点
     * @param currentPoint 当前点
     * @return 是否保留
     */
    private boolean shouldKeepPoint(TrackPoint lastPoint, TrackPoint currentPoint) {
        // 计算时间间隔
        Duration timeDiff = Duration.between(lastPoint.getTimestamp(), currentPoint.getTimestamp());
        long timeIntervalSeconds = timeDiff.getSeconds();
        
        // 如果时间间隔太短，直接跳过
        if (timeIntervalSeconds < MIN_TIME_INTERVAL_SECONDS) {
            return false;
        }
        
        // 如果时间间隔太长，必须保留
        if (timeIntervalSeconds > MAX_TIME_INTERVAL_SECONDS) {
            return true;
        }
        
        // 计算距离
        double distance = calculateDistance(lastPoint, currentPoint);
        
        // 如果距离变化足够大，保留点
        return distance >= MIN_DISTANCE_THRESHOLD;
    }
    
    /**
     * 计算两点之间的距离（米）
     * 使用Haversine公式计算地球表面两点间的距离
     */
    private double calculateDistance(TrackPoint point1, TrackPoint point2) {
        if (point1.getLatitude() == null || point1.getLongitude() == null ||
            point2.getLatitude() == null || point2.getLongitude() == null) {
            return 0.0;
        }
        
        double lat1 = Math.toRadians(point1.getLatitude());
        double lon1 = Math.toRadians(point1.getLongitude());
        double lat2 = Math.toRadians(point2.getLatitude());
        double lon2 = Math.toRadians(point2.getLongitude());
        
        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;
        
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dlon / 2) * Math.sin(dlon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // 地球半径（米）
        double earthRadius = 6371000;
        
        return earthRadius * c;
    }
    
    /**
     * 获取优化统计信息
     */
    public String getOptimizationStats(List<TrackPoint> original, List<TrackPoint> optimized) {
        if (original == null || optimized == null) {
            return "无法计算优化统计";
        }
        
        int originalSize = original.size();
        int optimizedSize = optimized.size();
        double reductionPercentage = originalSize > 0 ? 
            ((double)(originalSize - optimizedSize) / originalSize) * 100 : 0;
        
        return String.format("轨迹点优化: %d -> %d (减少 %.1f%%)", 
                           originalSize, optimizedSize, reductionPercentage);
    }
}