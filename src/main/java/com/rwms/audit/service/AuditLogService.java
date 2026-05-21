package com.rwms.audit.service;

import com.rwms.audit.command.AuditCommand;
import com.rwms.audit.dto.AuditLogFilter;
import com.rwms.audit.dto.AuditLogResponse;
import com.rwms.audit.entity.AuditLog;
import com.rwms.audit.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(AuditCommand command) {
        command.execute(auditLogRepository);
    }

    public Page<AuditLogResponse> getLogs(AuditLogFilter filter, Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getActionName() != null && !filter.getActionName().isEmpty()) {
                predicates.add(cb.equal(root.get("actionName"), filter.getActionName()));
            }
            if (filter.getUserEmail() != null && !filter.getUserEmail().isEmpty()) {
                predicates.add(cb.equal(root.get("userEmail"), filter.getUserEmail()));
            }
            if (filter.getPerformedByEmail() != null && !filter.getPerformedByEmail().isEmpty()) {
                predicates.add(cb.equal(root.join("performedBy").get("email"), filter.getPerformedByEmail()));
            }
            if (filter.getFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), filter.getFrom()));
            }
            if (filter.getTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), filter.getTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return auditLogRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public List<AuditLogResponse> getAllLogs(AuditLogFilter filter) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getActionName() != null && !filter.getActionName().isEmpty()) {
                predicates.add(cb.equal(root.get("actionName"), filter.getActionName()));
            }
            if (filter.getUserEmail() != null && !filter.getUserEmail().isEmpty()) {
                predicates.add(cb.equal(root.get("userEmail"), filter.getUserEmail()));
            }
            if (filter.getPerformedByEmail() != null && !filter.getPerformedByEmail().isEmpty()) {
                predicates.add(cb.equal(root.join("performedBy").get("email"), filter.getPerformedByEmail()));
            }
            if (filter.getFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), filter.getFrom()));
            }
            if (filter.getTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), filter.getTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return auditLogRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .actionName(log.getActionName())
                .userEmail(log.getUserEmail())
                .performedByEmail(log.getPerformedBy() != null ? log.getPerformedBy().getEmail() : null)
                .details(log.getDetails())
                .timestamp(log.getTimestamp())
                .build();
    }
}
