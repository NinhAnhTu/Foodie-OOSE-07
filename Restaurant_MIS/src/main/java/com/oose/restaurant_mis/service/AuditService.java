package com.oose.restaurant_mis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oose.restaurant_mis.entity.AuditLog;
import com.oose.restaurant_mis.entity.User;
import com.oose.restaurant_mis.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class AuditService {
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private ObjectMapper objectMapper;

    // Hàm ghi log dùng chung
    public void log(User user, String action, String tableName, Integer recordId, Object oldValue, Object newValue) {
        try {
            AuditLog log = new AuditLog();
            log.setUser(user);
            log.setAction(action);
            log.setTableName(tableName);
            log.setRecordId(recordId);

            // Chuyển Object sang JSON String để lưu vào DB
            if (oldValue != null) log.setOldValue(objectMapper.writeValueAsString(oldValue));
            if (newValue != null) log.setNewValue(objectMapper.writeValueAsString(newValue));

            log.setCreatedAt(LocalDateTime.now());
            auditLogRepository.save(log);
        } catch (Exception e) {
            e.printStackTrace(); // Tránh làm treo hệ thống chính nếu ghi log lỗi
        }
    }

    public Page<AuditLog> getLogsPage(int page, int size) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }
    public Page<AuditLog> searchLogsByDate(String dateStr, int page, int size) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        // Nếu có truyền ngày vào thì cắt ra từ 00:00:00 đến 23:59:59
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            LocalDate date = LocalDate.parse(dateStr);
            startDate = date.atStartOfDay();
            endDate = date.atTime(LocalTime.MAX);
        }

        // Gọi hàm searchLogs đã có sẵn trong AuditLogRepository
        return auditLogRepository.searchLogs(
                null, null, null, startDate, endDate,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
    }
}