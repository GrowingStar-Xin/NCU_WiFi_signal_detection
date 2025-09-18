package com.ncu.trackplatform.repository;

import com.ncu.trackplatform.entity.TrackPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackPointRepository extends JpaRepository<TrackPoint, Long> {
    
    /**
     * 根据账号ID查找轨迹点
     */
    List<TrackPoint> findByAccountIdOrderByTimestampAsc(String accountId);
    
    /**
     * 根据轨迹ID查找轨迹点
     */
    List<TrackPoint> findByTrackIdOrderByTimestampAsc(String trackId);
    
    /**
     * 获取所有不同的账号ID
     */
    @Query("SELECT DISTINCT tp.accountId FROM TrackPoint tp")
    List<String> findDistinctAccountIds();
    
    /**
     * 获取所有不同的轨迹ID
     */
    @Query("SELECT DISTINCT tp.trackId FROM TrackPoint tp WHERE tp.trackId IS NOT NULL")
    List<String> findDistinctTrackIds();
    
    /**
     * 根据账号ID统计轨迹点数量
     */
    @Query("SELECT COUNT(tp) FROM TrackPoint tp WHERE tp.accountId = :accountId")
    Long countByAccountId(@Param("accountId") String accountId);
    
    /**
     * 根据轨迹ID统计轨迹点数量
     */
    @Query("SELECT COUNT(tp) FROM TrackPoint tp WHERE tp.trackId = :trackId")
    Long countByTrackId(@Param("trackId") String trackId);
}