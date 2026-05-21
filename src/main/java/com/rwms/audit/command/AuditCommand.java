package com.rwms.audit.command;

import com.rwms.audit.repository.AuditLogRepository;

public interface AuditCommand {
    void execute(AuditLogRepository repository);
}
