package com.ncu.trackplatform.controller;

import com.ncu.trackplatform.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "*")
public class DataImportController {

    @Autowired
    private DataService dataService;

    @Value("${data.import.directory:data}")
    private String dataDirectory;

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importData() {
        try {
            Map<String, Object> result = dataService.importDataFromDirectory(dataDirectory);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "导入失败: " + e.getMessage()));
        }
    }

    @GetMapping("/mac-addresses")
    public ResponseEntity<List<String>> getAvailableMacAddresses() {
        try {
            List<String> macAddresses = dataService.getAvailableMacAddresses();
            return ResponseEntity.ok(macAddresses);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/trajectory/{macAddress}")
    public ResponseEntity<List<Map<String, Object>>> getTrajectoryByMac(@PathVariable String macAddress) {
        try {
            List<Map<String, Object>> trajectory = dataService.getTrajectoryByMac(macAddress);
            return ResponseEntity.ok(trajectory);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        try {
            Map<String, Object> status = dataService.getDatabaseStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("connected", false, "message", "获取状态失败: " + e.getMessage()));
        }
    }
}