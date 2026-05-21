package com.rwms.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogFilter {
    private String actionName;
    private String userEmail;
    private String performedByEmail;
    private LocalDateTime from;
    private LocalDateTime to;
}
