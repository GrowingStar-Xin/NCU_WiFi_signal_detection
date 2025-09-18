package com.ncu.trackplatform.dto;

import java.time.LocalDateTime;

public class TrackPointDto {
    
    private Long id;
    private String accountId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private Double accuracy;
    private Double speed;
    private String trackId;
    
    // 默认构造函数
    public TrackPointDto() {}
    
    // 构造函数
    public TrackPointDto(String accountId, Double latitude, Double longitude, LocalDateTime timestamp) {
        this.accountId = accountId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getAccountId() {
        return accountId;
    }
    
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Double getAccuracy() {
        return accuracy;
    }
    
    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }
    
    public Double getSpeed() {
        return speed;
    }
    
    public void setSpeed(Double speed) {
        this.speed = speed;
    }
    
    public String getTrackId() {
        return trackId;
    }
    
    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }
}