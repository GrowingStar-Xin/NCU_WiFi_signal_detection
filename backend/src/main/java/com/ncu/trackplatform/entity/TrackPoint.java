package com.ncu.trackplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "track_points")
public class TrackPoint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "account_id", nullable = false)
    private String accountId;
    
    @Column(name = "latitude", nullable = false)
    private Double latitude;
    
    @Column(name = "longitude", nullable = false)
    private Double longitude;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "accuracy")
    private Double accuracy;
    
    @Column(name = "speed")
    private Double speed;
    
    @Column(name = "track_id")
    private String trackId;
    
    // 默认构造函数
    public TrackPoint() {}
    
    // 构造函数
    public TrackPoint(String accountId, Double latitude, Double longitude, LocalDateTime timestamp) {
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