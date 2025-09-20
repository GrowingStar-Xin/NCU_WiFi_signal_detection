package com.ncu.trackplatform.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // @PostConstruct
    // public void init() {
    //     initializeTables();
    //     System.out.println("数据库表初始化完成");
    // }

    public void initializeTables() {
        // 创建WiFi日志表
        jdbcTemplate.execute("DROP TABLE IF EXISTS wifi_logs");
        jdbcTemplate.execute(
            "CREATE TABLE wifi_logs (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "timestamp VARCHAR(255)," +
                "terminal_behavior VARCHAR(255)," +
                "trajectory_details TEXT," +
                "terminal_ip VARCHAR(50)," +
                "ssid VARCHAR(100)," +
                "frequency_band VARCHAR(10)," +
                "channel_info VARCHAR(20)," +
                "signal_strength VARCHAR(20)," +
                "log_content TEXT," +
                "terminal_mac VARCHAR(50)," +
                "ap_name VARCHAR(255)," +
                "location VARCHAR(255)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );

        // 创建终端状态表
        jdbcTemplate.execute("DROP TABLE IF EXISTS terminal_status");
        jdbcTemplate.execute(
            "CREATE TABLE terminal_status (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "status VARCHAR(50)," +
                "terminal_mac VARCHAR(50)," +
                "terminal_ip VARCHAR(50)," +
                "ssid VARCHAR(100)," +
                "frequency_band VARCHAR(10)," +
                "channel_info VARCHAR(20)," +
                "micro_ap_name VARCHAR(255)," +
                "ap_name VARCHAR(255)," +
                "ac_name VARCHAR(255)," +
                "location VARCHAR(255)," +
                "ac_ip VARCHAR(50)," +
                "ap_ip VARCHAR(50)," +
                "account VARCHAR(255)," +
                "online_duration INTEGER," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );

        // 创建AP位置表
        jdbcTemplate.execute("DROP TABLE IF EXISTS ap_locations");
        jdbcTemplate.execute(
            "CREATE TABLE ap_locations (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "ap_name VARCHAR(255)," +
                "location VARCHAR(255)," +
                "x_coordinate DOUBLE," +
                "y_coordinate DOUBLE," +
                "building VARCHAR(255)," +
                "floor_info VARCHAR(50)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );
    }

    public Map<String, Object> importDataFromDirectory(String directoryPath) {
        Map<String, Object> result = new HashMap<>();
        File directory = new File(directoryPath);
        
        if (!directory.exists() || !directory.isDirectory()) {
            result.put("success", false);
            result.put("message", "目录不存在: " + directoryPath);
            return result;
        }

        initializeTables();
        
        File[] csvFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            result.put("success", false);
            result.put("message", "目录中没有找到CSV文件");
            return result;
        }

        int totalProcessed = 0;
        int totalErrors = 0;
        List<String> processedFiles = new ArrayList<>();

        for (File csvFile : csvFiles) {
            try {
                Map<String, Object> fileResult = importCsvFile(csvFile);
                totalProcessed += (Integer) fileResult.get("processed");
                totalErrors += (Integer) fileResult.get("errors");
                processedFiles.add(csvFile.getName());
            } catch (Exception e) {
                totalErrors++;
                System.err.println("处理文件失败: " + csvFile.getName() + ", 错误: " + e.getMessage());
            }
        }

        result.put("success", true);
        result.put("totalProcessed", totalProcessed);
        result.put("totalErrors", totalErrors);
        result.put("processedFiles", processedFiles);
        result.put("message", String.format("成功处理 %d 个文件，共 %d 条记录，%d 个错误", 
                                           processedFiles.size(), totalProcessed, totalErrors));
        
        return result;
    }

    private Map<String, Object> importCsvFile(File csvFile) throws IOException, CsvException {
        Map<String, Object> result = new HashMap<>();
        int processed = 0;
        int errors = 0;

        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            List<String[]> records = reader.readAll();
            
            if (records.isEmpty()) {
                result.put("processed", 0);
                result.put("errors", 0);
                return result;
            }

            String[] headers = records.get(0);
            
            // 根据文件名和表头判断文件类型
            if (csvFile.getName().startsWith("track_") || 
                Arrays.asList(headers).contains("终端行为") || 
                Arrays.asList(headers).contains("轨迹详细信息")) {
                
                // 处理轨迹数据
                for (int i = 1; i < records.size(); i++) {
                    try {
                        String[] record = records.get(i);
                        if (isValidTrackRecord(record, headers)) {
                            insertWifiLog(record, headers);
                            processed++;
                        }
                    } catch (Exception e) {
                        errors++;
                    }
                }
                
            } else if (csvFile.getName().startsWith("list_") || 
                       Arrays.asList(headers).contains("终端MAC地址") || 
                       Arrays.asList(headers).contains("状态")) {
                
                // 处理状态列表数据
                for (int i = 1; i < records.size(); i++) {
                    try {
                        String[] record = records.get(i);
                        if (isValidStatusRecord(record, headers)) {
                            insertTerminalStatus(record, headers);
                            processed++;
                        }
                    } catch (Exception e) {
                        errors++;
                    }
                }
            }
        }

        result.put("processed", processed);
        result.put("errors", errors);
        return result;
    }

    private boolean isValidTrackRecord(String[] record, String[] headers) {
        if (record.length < headers.length) return false;
        
        // 检查关键字段是否为空
        for (int i = 0; i < Math.min(record.length, headers.length); i++) {
            String header = headers[i];
            String value = record[i];
            
            if (("时间".equals(header) || "终端行为".equals(header)) && 
                (value == null || value.trim().isEmpty())) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidStatusRecord(String[] record, String[] headers) {
        if (record.length < headers.length) return false;
        
        // 检查MAC地址字段
        for (int i = 0; i < Math.min(record.length, headers.length); i++) {
            String header = headers[i];
            String value = record[i];
            
            if ("终端MAC地址".equals(header) && 
                (value == null || value.trim().isEmpty())) {
                return false;
            }
        }
        return true;
    }

    private void insertWifiLog(String[] record, String[] headers) {
        Map<String, String> data = new HashMap<>();
        for (int i = 0; i < Math.min(record.length, headers.length); i++) {
            data.put(headers[i], record[i]);
        }

        String sql = "INSERT INTO wifi_logs (timestamp, terminal_behavior, trajectory_details, " +
                     "terminal_ip, ssid, frequency_band, channel_info, " +
                     "signal_strength, log_content, terminal_mac, ap_name, location) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // 从轨迹详细信息中提取MAC地址和AP名称
        String trajectoryDetails = data.getOrDefault("轨迹详细信息", "");
        String terminalMac = extractMacFromTrajectory(trajectoryDetails);
        String apName = extractApNameFromTrajectory(trajectoryDetails);
        String location = extractLocationFromTrajectory(trajectoryDetails);

        jdbcTemplate.update(sql,
            data.getOrDefault("时间", ""),
            data.getOrDefault("终端行为", ""),
            trajectoryDetails,
            data.getOrDefault("终端IP地址", ""),
            data.getOrDefault("关联SSID", ""),
            data.getOrDefault("关联频段", ""),
            data.getOrDefault("关联信道", ""),
            data.getOrDefault("信号强度", ""),
            data.getOrDefault("日志原文", ""),
            terminalMac,
            apName,
            location
        );
    }

    private void insertTerminalStatus(String[] record, String[] headers) {
        Map<String, String> data = new HashMap<>();
        for (int i = 0; i < Math.min(record.length, headers.length); i++) {
            data.put(headers[i], record[i]);
        }

        String sql = "INSERT INTO terminal_status (status, terminal_mac, terminal_ip, ssid, " +
                     "frequency_band, channel_info, micro_ap_name, " +
                     "ap_name, ac_name, location, ac_ip, ap_ip, " +
                     "account, online_duration) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String onlineDurationStr = data.getOrDefault("终端在线时长(s)", "0");
        int onlineDuration = 0;
        try {
            onlineDuration = Integer.parseInt(onlineDurationStr);
        } catch (NumberFormatException e) {
            // 忽略解析错误，使用默认值0
        }

        jdbcTemplate.update(sql,
            data.getOrDefault("状态", ""),
            data.getOrDefault("终端MAC地址", ""),
            data.getOrDefault("终端IP地址", ""),
            data.getOrDefault("关联SSID", ""),
            data.getOrDefault("关联频段", ""),
            data.getOrDefault("关联信道", ""),
            data.getOrDefault("关联微AP名称", ""),
            data.getOrDefault("关联AP名称", ""),
            data.getOrDefault("AC名称", ""),
            data.getOrDefault("位置", ""),
            data.getOrDefault("AC的IP地址", ""),
            data.getOrDefault("关联AP的IP地址", ""),
            data.getOrDefault("账号", ""),
            onlineDuration
        );
    }

    private String extractMacFromTrajectory(String trajectory) {
        // 从日志原文中提取MAC地址
        if (trajectory.contains("MAC:")) {
            String[] parts = trajectory.split("MAC:");
            if (parts.length > 1) {
                String macPart = parts[1].split(";")[0];
                return macPart.trim();
            }
        }
        return "";
    }

    private String extractApNameFromTrajectory(String trajectory) {
        // 从轨迹详细信息中提取AP名称
        if (trajectory.contains("AP ")) {
            String[] parts = trajectory.split("AP ");
            if (parts.length > 1) {
                String apPart = parts[1].split(" ")[0];
                return apPart.trim();
            }
        }
        return "";
    }

    private String extractLocationFromTrajectory(String trajectory) {
        // 从轨迹详细信息中提取位置信息
        if (trajectory.contains("(") && trajectory.contains(")")) {
            int start = trajectory.indexOf("(");
            int end = trajectory.indexOf(")", start);
            if (start != -1 && end != -1) {
                return trajectory.substring(start + 1, end).trim();
            }
        }
        return "";
    }

    public List<String> getAvailableMacAddresses() {
        String sql = """
            SELECT DISTINCT terminal_mac 
            FROM (
                SELECT terminal_mac FROM wifi_logs WHERE terminal_mac IS NOT NULL AND terminal_mac != ''
                UNION
                SELECT terminal_mac FROM terminal_status WHERE terminal_mac IS NOT NULL AND terminal_mac != ''
            ) 
            ORDER BY terminal_mac
        """;
        
        return jdbcTemplate.queryForList(sql, String.class)
                          .stream()
                          .filter(mac -> mac != null && !mac.trim().isEmpty())
                          .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getTrajectoryByMac(String macAddress) {
        String sql = """
            SELECT timestamp, terminal_behavior, trajectory_details, 
                   terminal_ip, ssid, frequency_band, ap_name, location,
                   signal_strength
            FROM wifi_logs 
            WHERE terminal_mac = ? 
            ORDER BY timestamp
        """;
        
        return jdbcTemplate.queryForList(sql, macAddress);
    }

    public Map<String, Object> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            int wifiLogsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM wifi_logs", Integer.class);
            int terminalStatusCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM terminal_status", Integer.class);
            int macAddressCount = getAvailableMacAddresses().size();
            
            status.put("connected", true);
            status.put("wifiLogsCount", wifiLogsCount);
            status.put("terminalStatusCount", terminalStatusCount);
            status.put("macAddressCount", macAddressCount);
            status.put("message", "数据库连接正常");
            
        } catch (Exception e) {
            status.put("connected", false);
            status.put("message", "数据库连接失败: " + e.getMessage());
        }
        
        return status;
    }
}