package com.ncu.trackplatform.service;

import com.ncu.trackplatform.entity.TrackPoint;
import com.ncu.trackplatform.repository.TrackPointRepository;
import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class DataInitService implements CommandLineRunner {
    
    @Autowired
    private TrackPointRepository trackPointRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // 检查数据库是否已有数据
        if (trackPointRepository.count() > 0) {
            System.out.println("数据库已有数据，跳过初始化");
            return;
        }
        
        // 加载CSV数据
        loadCSVData();
    }
    
    private void loadCSVData() {
        // 尝试多个可能的数据目录路径
        String[] possiblePaths = {
            "../data",
            "./data", 
            "data",
            "/Users/wulinxin/Desktop/其他项目/wifi异常检测/data"
        };
        
        Path dataDirPath = null;
        for (String pathStr : possiblePaths) {
            Path testPath = Paths.get(pathStr);
            if (Files.exists(testPath)) {
                dataDirPath = testPath;
                System.out.println("找到数据目录: " + testPath.toAbsolutePath());
                break;
            }
        }
        
        if (dataDirPath == null) {
            System.out.println("数据目录不存在，尝试的路径: " + String.join(", ", possiblePaths));
            return;
        }
        
        try (Stream<Path> paths = Files.walk(dataDirPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".csv"))
                 .forEach(this::loadCSVFile);
        } catch (IOException e) {
            System.err.println("读取数据目录失败: " + e.getMessage());
        }
    }
    
    private void loadCSVFile(Path csvFile) {
        System.out.println("正在加载文件: " + csvFile.getFileName());
        
        try (CSVReader reader = new CSVReader(new FileReader(csvFile.toFile()))) {
            String[] headers = reader.readNext();
            if (headers == null) {
                System.out.println("文件为空: " + csvFile.getFileName());
                return;
            }
            
            List<TrackPoint> trackPoints = new ArrayList<>();
            String[] line;
            int lineCount = 0;
            
            while ((line = reader.readNext()) != null) {
                lineCount++;
                TrackPoint point = parseCSVLine(line, headers, csvFile.getFileName().toString());
                if (point != null) {
                    trackPoints.add(point);
                }
                
                // 批量保存，避免内存溢出
                if (trackPoints.size() >= 1000) {
                    trackPointRepository.saveAll(trackPoints);
                    trackPoints.clear();
                }
            }
            
            // 保存剩余数据
            if (!trackPoints.isEmpty()) {
                trackPointRepository.saveAll(trackPoints);
            }
            
            System.out.println("文件 " + csvFile.getFileName() + " 加载完成，共处理 " + lineCount + " 行数据");
            
        } catch (Exception e) {
            System.err.println("加载文件失败 " + csvFile.getFileName() + ": " + e.getMessage());
        }
    }
    
    private TrackPoint parseCSVLine(String[] line, String[] headers, String fileName) {
        try {
            TrackPoint point = new TrackPoint();
            
            // 从文件名提取trackId
            String trackId = fileName.replace(".csv", "").replace("_decrypted", "");
            point.setTrackId(trackId);
            
            String timestamp = null;
            String apInfo = null;
            String userMac = null;
            
            for (int i = 0; i < headers.length && i < line.length; i++) {
                String header = headers[i].trim();
                String value = line[i].trim();
                
                if (value.isEmpty()) continue;
                
                switch (header) {
                    case "时间":
                        timestamp = value;
                        break;
                    case "轨迹详细信息":
                        apInfo = value;
                        break;
                    case "日志原文":
                        // 从日志中提取MAC地址
                        userMac = extractMacFromLog(value);
                        break;
                }
            }
            
            // 设置时间戳
            if (timestamp != null) {
                point.setTimestamp(parseTimestamp(timestamp));
            } else {
                point.setTimestamp(LocalDateTime.now());
            }
            
            // 设置用户ID
            if (userMac != null) {
                point.setAccountId(userMac);
            } else {
                point.setAccountId(trackId);
            }
            
            // 从AP信息中提取位置并设置坐标
            if (apInfo != null && !apInfo.isEmpty()) {
                double[] coordinates = getCoordinatesFromAP(apInfo);
                if (coordinates != null) {
                    point.setLatitude(coordinates[0]);
                    point.setLongitude(coordinates[1]);
                    return point;
                }
            }
            
        } catch (Exception e) {
            System.err.println("解析CSV行失败: " + String.join(",", line) + " - " + e.getMessage());
        }
        
        return null;
    }
    
    private String extractMacFromLog(String logText) {
        // 从日志中提取MAC地址
        if (logText.contains("MAC:")) {
            int macIndex = logText.indexOf("MAC:") + 4;
            int endIndex = logText.indexOf(";", macIndex);
            if (endIndex == -1) endIndex = logText.indexOf(" ", macIndex);
            if (endIndex == -1) endIndex = logText.length();
            return logText.substring(macIndex, endIndex).trim();
        }
        return null;
    }
    
    private double[] getCoordinatesFromAP(String apInfo) {
        // 南昌大学前湖校区的基准坐标
        double baseLat = 28.6329;
        double baseLng = 115.8342;
        
        // 根据AP名称分配相对坐标
        if (apInfo.contains("机电楼")) {
            return new double[]{baseLat + 0.002, baseLng + 0.001};
        } else if (apInfo.contains("慧源楼")) {
            return new double[]{baseLat - 0.001, baseLng + 0.002};
        } else if (apInfo.contains("艺术楼")) {
            return new double[]{baseLat + 0.001, baseLng - 0.001};
        } else if (apInfo.contains("医创中心")) {
            return new double[]{baseLat - 0.002, baseLng - 0.001};
        } else if (apInfo.contains("图书馆")) {
            return new double[]{baseLat, baseLng + 0.003};
        } else if (apInfo.contains("教学楼")) {
            return new double[]{baseLat + 0.003, baseLng};
        } else {
            // 默认位置，添加随机偏移
            double randomLat = baseLat + (Math.random() - 0.5) * 0.01;
            double randomLng = baseLng + (Math.random() - 0.5) * 0.01;
            return new double[]{randomLat, randomLng};
        }
    }
    
    private LocalDateTime parseTimestamp(String timestamp) {
        try {
            // 尝试多种时间格式
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            };
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDateTime.parse(timestamp, formatter);
                } catch (Exception ignored) {
                }
            }
            
            // 如果是时间戳（毫秒或秒）
            if (timestamp.matches("\\d+")) {
                long millis = Long.parseLong(timestamp);
                if (millis > 1000000000000L) { // 毫秒时间戳
                    return LocalDateTime.ofEpochSecond(millis / 1000, 0, java.time.ZoneOffset.UTC);
                } else { // 秒时间戳
                    return LocalDateTime.ofEpochSecond(millis, 0, java.time.ZoneOffset.UTC);
                }
            }
            
        } catch (Exception e) {
            System.err.println("时间戳解析失败: " + timestamp);
        }
        
        return LocalDateTime.now(); // 默认返回当前时间
    }
}