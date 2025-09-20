package com.ncu.trackplatform.service;

import com.ncu.trackplatform.dto.TrackDataDto;
import com.ncu.trackplatform.dto.TrackPointDto;
import com.ncu.trackplatform.entity.TrackPoint;
import com.ncu.trackplatform.repository.TrackPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrackService {
    
    @Autowired
    private TrackOptimizationService optimizationService;
    
    @Autowired
    private TrackPointRepository trackPointRepository;
    
    /**
     * 获取所有轨迹列表
     */
    public List<TrackDataDto> getAllTracks() {
        try {
            List<String> trackIds = trackPointRepository.findDistinctTrackIds();
            List<String> accountIds = trackPointRepository.findDistinctAccountIds();
            
            List<TrackDataDto> tracks = new ArrayList<>();
            
            // 添加按轨迹ID分组的轨迹
            for (String trackId : trackIds) {
                Long pointCount = trackPointRepository.countByTrackId(trackId);
                TrackDataDto track = new TrackDataDto();
                track.setId(trackId);
                track.setName("轨迹 " + trackId);
                track.setTotalPoints(pointCount.intValue());
                tracks.add(track);
            }
            
            // 添加按账号ID分组的轨迹
            for (String accountId : accountIds) {
                Long pointCount = trackPointRepository.countByAccountId(accountId);
                TrackDataDto track = new TrackDataDto();
                track.setId("account_" + accountId);
                track.setName("用户 " + accountId);
                track.setTotalPoints(pointCount.intValue());
                tracks.add(track);
            }
            
            return tracks;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * 根据轨迹ID获取轨迹点（优化版本）
     */
    public List<TrackPointDto> getTrackPoints(String trackId) {
        try {
            List<TrackPoint> rawPoints;
            if (trackId.startsWith("account_")) {
                String accountId = trackId.substring(8);
                rawPoints = trackPointRepository.findByAccountIdOrderByTimestampAsc(accountId);
            } else {
                rawPoints = trackPointRepository.findByTrackIdOrderByTimestampAsc(trackId);
            }
            
            List<TrackPoint> optimizedPoints = optimizationService.optimizeTrackPoints(rawPoints);
            return convertToDto(optimizedPoints);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取所有轨迹点（优化版本）
     */
    public List<TrackPointDto> getAllTrackPoints() {
        try {
            List<TrackPoint> rawPoints = trackPointRepository.findAll();
            List<TrackPoint> optimizedPoints = optimizationService.optimizeTrackPoints(rawPoints);
            return convertToDto(optimizedPoints);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取原始轨迹点（未优化）
     */
    public List<TrackPointDto> getRawTrackPoints(String trackId) {
        try {
            List<TrackPoint> rawPoints;
            if (trackId.startsWith("account_")) {
                String accountId = trackId.substring(8);
                rawPoints = trackPointRepository.findByAccountIdOrderByTimestampAsc(accountId);
            } else {
                rawPoints = trackPointRepository.findByTrackIdOrderByTimestampAsc(trackId);
            }
            return convertToDto(rawPoints);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取所有原始轨迹点（未优化）
     */
    public List<TrackPointDto> getAllRawTrackPoints() {
        try {
            List<TrackPoint> rawPoints = trackPointRepository.findAll();
            return convertToDto(rawPoints);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * 将TrackPoint实体转换为DTO
     */
    private List<TrackPointDto> convertToDto(List<TrackPoint> trackPoints) {
        return trackPoints.stream().map(point -> {
            TrackPointDto dto = new TrackPointDto();
            dto.setAccountId(point.getAccountId());
            dto.setLatitude(point.getLatitude());
            dto.setLongitude(point.getLongitude());
            dto.setTimestamp(point.getTimestamp());
            dto.setTrackId(point.getTrackId());
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * 上传CSV文件并解析轨迹数据
     */
    public String uploadTrackData(MultipartFile file) {
        return "文件上传功能暂时不可用";
    }
}