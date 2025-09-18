package com.ncu.trackplatform.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TrackDataDto {
    
    private String id;
    private String name;
    private String accountId;
    private List<TrackPointDto> points;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalPoints;
    
    // 默认构造函数
    public TrackDataDto() {}
    
    // 构造函数
    public TrackDataDto(String id, String name, String accountId) {
        this.id = id;
        this.name = name;
        this.accountId = accountId;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAccountId() {
        return accountId;
    }
    
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    
    public List<TrackPointDto> getPoints() {
        return points;
    }
    
    public void setPoints(List<TrackPointDto> points) {
        this.points = points;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getTotalPoints() {
        return totalPoints;
    }
    
    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }
}