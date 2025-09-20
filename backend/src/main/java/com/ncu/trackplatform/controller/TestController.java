package com.ncu.trackplatform.controller;

import com.ncu.trackplatform.repository.TrackPointRepository;
import com.ncu.trackplatform.service.DataProcessingService;
import com.ncu.trackplatform.service.WiFiLogParsingService;
import com.ncu.trackplatform.entity.TrackPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    // @Autowired
    // private TrackPointRepository trackPointRepository;
    
    @Autowired
    private DataProcessingService dataProcessingService;
    
    @Autowired
    private WiFiLogParsingService wifiLogParsingService;
    
    @GetMapping("/count")
    public String getCount() {
        return "数据统计功能暂时不可用";
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
        return "Repository功能暂时不可用";
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
    
    /**
     * 测试地址解析功能
     */
    @GetMapping("/address-parsing")
    public Map<String, Object> testAddressParsing() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 测试不同格式的WiFi日志
            String[] testLogs = {
                "2024-01-15 09:00:00,上线,终端在AP 无线/青山湖北/生物楼 获取IP地址,user001",
                "2024-01-15 09:05:00,上线,终端在AP 无线/青山湖北/化学楼 获取IP地址,user002", 
                "2024-01-15 09:10:00,上线,终端在AP 无线/前湖北/理科楼 获取IP地址,user003",
                "2024-01-15 09:15:00,漫游,漫游到AP 无线/青山湖北/物理楼,user001",
                "2024-01-15 09:20:00,上线,终端在AP 前湖北-艺术楼-b-2f-02 获取IP地址,user004"
            };
            
            Map<String, Object> testResults = new HashMap<>();
            
            // 定义CSV头部
            String[] headers = {"时间", "终端行为", "轨迹详细信息", "账号ID"};
            
            for (int i = 0; i < testLogs.length; i++) {
                try {
                    // 将CSV行分割为数组
                    String[] lineArray = testLogs[i].split(",");
                    
                    // 使用反射调用私有方法parseWiFiLogLine
                    java.lang.reflect.Method method = wifiLogParsingService.getClass()
                        .getDeclaredMethod("parseWiFiLogLine", String[].class, String[].class);
                    method.setAccessible(true);
                    TrackPoint trackPoint = (TrackPoint) method.invoke(wifiLogParsingService, lineArray, headers);
                    
                    Map<String, Object> pointInfo = new HashMap<>();
                    if (trackPoint != null) {
                        pointInfo.put("accountId", trackPoint.getAccountId());
                        pointInfo.put("latitude", trackPoint.getLatitude());
                        pointInfo.put("longitude", trackPoint.getLongitude());
                        pointInfo.put("timestamp", trackPoint.getTimestamp());
                        pointInfo.put("success", true);
                    } else {
                        pointInfo.put("success", false);
                        pointInfo.put("error", "解析失败");
                    }
                    testResults.put("test" + (i + 1), pointInfo);
                } catch (Exception e) {
                    Map<String, Object> errorInfo = new HashMap<>();
                    errorInfo.put("success", false);
                    errorInfo.put("error", e.getMessage());
                    testResults.put("test" + (i + 1), errorInfo);
                }
            }
            
            result.put("code", 200);
            result.put("message", "地址解析测试完成");
            result.put("data", testResults);
            
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "测试失败: " + e.getMessage());
            result.put("data", null);
        }
        
        return result;
    }
}