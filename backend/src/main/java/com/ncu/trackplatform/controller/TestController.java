package com.ncu.trackplatform.controller;

import com.ncu.trackplatform.repository.TrackPointRepository;
import com.ncu.trackplatform.service.DataProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private TrackPointRepository trackPointRepository;
    
    @Autowired
    private DataProcessingService dataProcessingService;
    
    @GetMapping("/count")
    public String getCount() {
        try {
            long count = trackPointRepository.count();
            return "数据库中有 " + count + " 条记录";
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }
    
    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }
    
    @GetMapping("/service-test")
    public String testDataProcessingService() {
        try {
            String result = dataProcessingService.getDataStatistics();
            return "DataProcessingService 正常工作: " + result;
        } catch (Exception e) {
            return "DataProcessingService 错误: " + e.getMessage();
        }
    }
    
    @GetMapping("/repository-test")
    public String testRepository() {
        try {
            long count = trackPointRepository.count();
            return "Repository 正常工作: 数据库中有 " + count + " 条记录";
        } catch (Exception e) {
            return "Repository 错误: " + e.getMessage();
        }
    }
    
    @GetMapping("/data-statistics")
    public String testDataStatistics() {
        try {
            System.out.println("TestController: 开始调用 dataProcessingService.getDataStatistics()");
            String result = dataProcessingService.getDataStatistics();
            System.out.println("TestController: 成功获取统计信息: " + result);
            return "通过TestController调用成功: " + result;
        } catch (Exception e) {
            System.out.println("TestController: 获取统计信息时发生错误: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return "通过TestController调用失败: " + e.getMessage();
        }
    }
}