package com.rwms.audit.controller;

import com.rwms.audit.dto.AuditLogFilter;
import com.rwms.audit.dto.AuditLogResponse;
import com.rwms.audit.service.AuditLogService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogService auditLogService;

    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Page<AuditLogResponse>> getLogs(
            @RequestParam(required = false) String actionName,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String performedByEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        AuditLogFilter filter = AuditLogFilter.builder()
                .actionName(actionName)
                .userEmail(userEmail)
                .performedByEmail(performedByEmail)
                .from(from)
                .to(to)
                .build();
                
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return ResponseEntity.ok(auditLogService.getLogs(filter, pageable));
    }

    @GetMapping("/logs/export")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Resource> exportLogsAsCsv(
            @RequestParam(required = false) String actionName,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String performedByEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
            
        AuditLogFilter filter = AuditLogFilter.builder()
                .actionName(actionName)
                .userEmail(userEmail)
                .performedByEmail(performedByEmail)
                .from(from)
                .to(to)
                .build();
                
        List<AuditLogResponse> logs = auditLogService.getAllLogs(filter);
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Action Name,User Email,Performed By,Timestamp,Details\n");
        for (AuditLogResponse log : logs) {
            csv.append(log.getId()).append(",")
               .append(escapeCsv(log.getActionName())).append(",")
               .append(escapeCsv(log.getUserEmail())).append(",")
               .append(escapeCsv(log.getPerformedByEmail())).append(",")
               .append(log.getTimestamp() != null ? log.getTimestamp().toString() : "").append(",")
               .append(escapeCsv(log.getDetails())).append("\n");
        }
        
        ByteArrayResource resource = new ByteArrayResource(csv.toString().getBytes(StandardCharsets.UTF_8));
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit_logs.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
    
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
