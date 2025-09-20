package com.ncu.trackplatform.service;

import com.ncu.trackplatform.entity.TrackPoint;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WiFiLogParsingService {
    
    // 时间格式解析器
    private static final DateTimeFormatter[] TIME_FORMATTERS = {
        DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("MMM dd HH:mm:ss yyyy", java.util.Locale.ENGLISH)
    };
    
    // MAC地址提取正则 - 适配多种格式
    private static final Pattern MAC_PATTERN = Pattern.compile("MAC[=:]([a-fA-F0-9:-]{17}|[a-fA-F0-9.]{14})");
    private static final Pattern MAC_PATTERN2 = Pattern.compile("Client\\s+([a-fA-F0-9-]{12,17})");
    private static final Pattern MAC_PATTERN3 = Pattern.compile("sta_mac\":\\s*\"([a-fA-F0-9.]{14})\"");
    
    // AP位置信息提取正则 - 适配实际数据格式
    private static final Pattern AP_LOCATION_PATTERN = Pattern.compile("终端在AP\\s+([^\\s]+)\\s+");
    private static final Pattern AP_LOCATION_PATTERN2 = Pattern.compile("终端在AP\\s+([^\\s(]+)");
    private static final Pattern AP_ROAM_TO_PATTERN = Pattern.compile("漫游到AP\\s+([^\\s(]+)");
    
    // 地址格式解析正则 - 支持"无线/校区/建筑"格式
    private static final Pattern ADDRESS_FORMAT_PATTERN = Pattern.compile("无线/([^/]+)/([^/\\s)]+)");
    
    // 模拟的AP位置坐标映射（实际项目中应该从数据库或配置文件读取）
    private static final Map<String, double[]> AP_COORDINATES = new HashMap<>();
    
    static {
        // 前湖北校区
        AP_COORDINATES.put("前湖北-艺术楼-b-2f-02", new double[]{115.8372, 28.6329});
        AP_COORDINATES.put("前湖北-图书馆-A1901-C1", new double[]{115.8385, 28.6342});
        AP_COORDINATES.put("前湖北-理科楼-6f-a-05", new double[]{115.8390, 28.6335});
        AP_COORDINATES.put("前湖北-理科楼-7f-a-10", new double[]{115.8392, 28.6337});
        AP_COORDINATES.put("前湖北-慧源楼-519-C1", new double[]{115.8380, 28.6350});
        AP_COORDINATES.put("前湖北-慧源楼-5FBMZL-2", new double[]{115.8382, 28.6352});
        AP_COORDINATES.put("前湖北-慧源楼-315-C1", new double[]{115.8378, 28.6348});
        AP_COORDINATES.put("前湖北-材料环境楼-C349B-1", new double[]{115.8395, 28.6340});
        AP_COORDINATES.put("前湖北-文法楼-2F-A243", new double[]{115.8365, 28.6325});
        AP_COORDINATES.put("前湖北-文法楼-3F-A320", new double[]{115.8367, 28.6327});
        AP_COORDINATES.put("前湖北-前湖图书馆-3f-a0305", new double[]{115.8388, 28.6345});
        AP_COORDINATES.put("前湖北-智华科技楼-ap720-l-A-05F-7", new double[]{115.8400, 28.6355});
        
        // 前湖南校区
        AP_COORDINATES.put("前湖南-第一教学大楼-101-G1", new double[]{115.8360, 28.6310});
        AP_COORDINATES.put("前湖南-第一教学大楼-403-G1", new double[]{115.8362, 28.6312});
        
        // 青山湖北校区
        AP_COORDINATES.put("青山湖北-软件楼-518-G4", new double[]{115.9200, 28.6800});
        AP_COORDINATES.put("青山湖北-软件楼-120-G4", new double[]{115.9195, 28.6795});
        AP_COORDINATES.put("青山湖北-分析测试中心-6f-02", new double[]{115.9210, 28.6810});
        AP_COORDINATES.put("青山湖北-分析测试中心-6f-03", new double[]{115.9212, 28.6812});
        AP_COORDINATES.put("青山湖北-生物楼", new double[]{115.9180, 28.6785});
        AP_COORDINATES.put("青山湖北-化学楼", new double[]{115.9190, 28.6790});
        AP_COORDINATES.put("青山湖北-物理楼", new double[]{115.9205, 28.6805});
        
        // 东湖南校区
        AP_COORDINATES.put("东湖南-基础楼4F-06", new double[]{115.8800, 28.7200});
        AP_COORDINATES.put("东湖南-继教楼-201-C1", new double[]{115.8805, 28.7205});
        AP_COORDINATES.put("东湖南-图书馆-103-C2", new double[]{115.8810, 28.7210});
        
        // 校区级别的默认坐标（当无法找到具体建筑时使用）
        AP_COORDINATES.put("前湖北", new double[]{115.8380, 28.6340});
        AP_COORDINATES.put("前湖南", new double[]{115.8360, 28.6310});
        AP_COORDINATES.put("青山湖北", new double[]{115.9200, 28.6800});
        AP_COORDINATES.put("东湖南", new double[]{115.8800, 28.7200});
    }
    
    /**
     * 解析WiFi日志CSV文件
     */
    public List<TrackPoint> parseWiFiLogCSV(MultipartFile file) {
        List<TrackPoint> trackPoints = new ArrayList<>();
        
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()));
            String[] headers = reader.readNext(); // 读取表头
            
            String[] line;
            while ((line = reader.readNext()) != null) {
                TrackPoint point = parseWiFiLogLine(line, headers);
                if (point != null) {
                    trackPoints.add(point);
                }
            }
            
            reader.close();
            
        } catch (Exception e) {
            throw new RuntimeException("WiFi日志解析失败: " + e.getMessage());
        }
        
        return trackPoints;
    }
    
    /**
     * 解析单行WiFi日志数据
     */
    private TrackPoint parseWiFiLogLine(String[] line, String[] headers) {
        try {
            if (line.length < headers.length) {
                return null;
            }
            
            String time = null;
            String behavior = null;
            String details = null;
            String ipAddress = null;
            String logContent = null;
            
            // 解析各个字段
            for (int i = 0; i < headers.length && i < line.length; i++) {
                String header = headers[i].trim();
                String value = line[i].trim();
                
                switch (header) {
                    case "时间":
                        time = value;
                        break;
                    case "终端行为":
                        behavior = value;
                        break;
                    case "轨迹详细信息":
                        details = value;
                        break;
                    case "终端IP地址":
                        ipAddress = value;
                        break;
                    case "日志原文":
                        logContent = value;
                        break;
                }
            }
            
            // 添加调试信息
            System.out.println("解析行为: " + behavior + ", 是否为上线事件: " + isOnlineEvent(behavior));
            try {
                java.io.FileWriter fw = new java.io.FileWriter("debug.log", true);
                fw.write("解析行为: " + behavior + ", 是否为上线事件: " + isOnlineEvent(behavior) + "\n");
                fw.close();
            } catch (Exception e) {}
            if (behavior == null || !isOnlineEvent(behavior)) {
                return null;
            }
            
            // 提取AP位置
            String apLocation = extractAPLocation(details);
            if (apLocation == null) {
                return null;
            }
            
            // 获取坐标
            double[] coordinates = getAPCoordinates(apLocation);
            if (coordinates == null) {
                return null;
            }
            
            // 提取MAC地址作为账号ID
            String accountId = extractMacAddress(logContent);
            if (accountId == null) {
                accountId = ipAddress; // 如果没有MAC地址，使用IP地址
            }
            
            // 创建轨迹点
            TrackPoint point = new TrackPoint();
            point.setAccountId(accountId);
            point.setLatitude(coordinates[1]); // 纬度
            point.setLongitude(coordinates[0]); // 经度
            point.setTimestamp(parseTimestamp(time));
            point.setTrackId(details); // 将详细信息存储在trackId字段中
            
            System.out.println("成功创建TrackPoint: " + accountId + " at " + coordinates[1] + "," + coordinates[0]);
            try {
                java.io.FileWriter fw = new java.io.FileWriter("debug.log", true);
                fw.write("成功创建TrackPoint: " + accountId + " at " + coordinates[1] + "," + coordinates[0] + "\n");
                fw.close();
            } catch (Exception e) {}
            
            return point;
            
        } catch (Exception e) {
            System.err.println("解析WiFi日志行失败: " + String.join(",", line) + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 判断是否为上线事件
     */
    private boolean isOnlineEvent(String behavior) {
        return behavior != null && 
               (behavior.contains("上线 (获取IP地址成功)") || 
                behavior.contains("上线 (关联成功)") || 
                behavior.contains("获取IP地址成功") || 
                behavior.contains("关联成功") || 
                behavior.contains("上线"));
    }
    
    /**
     * 解析地址格式 "无线/校区/建筑"
     */
    private String parseAddressFormat(String address) {
        try {
            Matcher matcher = ADDRESS_FORMAT_PATTERN.matcher(address);
            if (matcher.find()) {
                String campus = matcher.group(1);  // 校区
                String building = matcher.group(2); // 建筑
                
                // 构建标准化的地址格式
                String standardAddress = campus + "-" + building;
                System.out.println("解析地址格式: " + address + " -> " + standardAddress);
                return standardAddress;
            }
            return address; // 如果不匹配格式，返回原地址
        } catch (Exception e) {
            System.err.println("解析地址格式时发生错误: " + e.getMessage());
            return address;
        }
    }

    /**
     * 提取AP位置信息
     */
    private String extractAPLocation(String details) {
        if (details == null) {
            return null;
        }
        
        System.out.println("提取AP位置，详细信息: " + details);
        try {
            java.io.FileWriter fw = new java.io.FileWriter("debug.log", true);
            fw.write("提取AP位置，详细信息: " + details + "\n");
            fw.close();
        } catch (Exception e) {}
        
        // 尝试第一种模式：终端在AP xxx 获取IP地址成功
        Matcher matcher = AP_LOCATION_PATTERN.matcher(details);
        if (matcher.find()) {
            String result = matcher.group(1);
            System.out.println("第一种模式匹配成功: " + result);
            try {
                java.io.FileWriter fw = new java.io.FileWriter("debug.log", true);
                fw.write("第一种模式匹配成功: " + result + "\n");
                fw.close();
            } catch (Exception e) {}
            return parseAddressFormat(result);
        }
        
        // 尝试第二种模式：终端在AP xxx (无线/...)
        matcher = AP_LOCATION_PATTERN2.matcher(details);
        if (matcher.find()) {
            String result = matcher.group(1);
            System.out.println("第二种模式匹配成功: " + result);
            try {
                java.io.FileWriter fw = new java.io.FileWriter("debug.log", true);
                fw.write("第二种模式匹配成功: " + result + "\n");
                fw.close();
            } catch (Exception e) {}
            return parseAddressFormat(result);
        }
        
        // 尝试漫游模式：漫游到AP xxx
        matcher = AP_ROAM_TO_PATTERN.matcher(details);
        if (matcher.find()) {
            String result = matcher.group(1);
            System.out.println("漫游模式匹配成功: " + result);
            try {
                java.io.FileWriter fw = new java.io.FileWriter("debug.log", true);
                fw.write("漫游模式匹配成功: " + result + "\n");
                fw.close();
            } catch (Exception e) {}
            return parseAddressFormat(result);
        }
        
        System.out.println("所有模式都匹配失败");
        try {
            java.io.FileWriter fw = new java.io.FileWriter("debug.log", true);
            fw.write("所有模式都匹配失败\n");
            fw.close();
        } catch (Exception e) {}
        return null;
    }
    
    /**
     * 获取AP坐标
     */
    private double[] getAPCoordinates(String apLocation) {
        if (apLocation == null) {
            return new double[]{0.0, 0.0}; // 默认坐标
        }
        
        // 首先尝试精确匹配
        double[] coordinates = AP_COORDINATES.get(apLocation);
        if (coordinates != null) {
            System.out.println("找到精确坐标: " + apLocation + " -> [" + coordinates[0] + ", " + coordinates[1] + "]");
            return coordinates;
        }
        
        // 如果精确匹配失败，尝试提取校区信息作为回退
        if (apLocation.contains("-")) {
            String campus = apLocation.split("-")[0];
            coordinates = AP_COORDINATES.get(campus);
            if (coordinates != null) {
                System.out.println("使用校区默认坐标: " + apLocation + " -> " + campus + " -> [" + coordinates[0] + ", " + coordinates[1] + "]");
                return coordinates;
            }
        }
        
        System.out.println("未找到AP位置坐标: " + apLocation + ", 使用默认坐标");
        return new double[]{0.0, 0.0};
    }
    
    /**
     * 提取MAC地址
     */
    private String extractMacAddress(String logContent) {
        if (logContent == null) {
            return null;
        }
        
        // 尝试第一种模式：MAC=xxx 或 MAC:xxx
        Matcher matcher = MAC_PATTERN.matcher(logContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // 尝试第二种模式：Client xxx
        matcher = MAC_PATTERN2.matcher(logContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // 尝试第三种模式：JSON格式的sta_mac
        matcher = MAC_PATTERN3.matcher(logContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * 解析时间戳
     */
    private LocalDateTime parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return LocalDateTime.now();
        }
        
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(timestamp, formatter);
            } catch (Exception e) {
                // 继续尝试下一个格式
            }
        }
        
        // 如果所有格式都失败，返回当前时间
        System.err.println("无法解析时间戳: " + timestamp);
        return LocalDateTime.now();
    }
}