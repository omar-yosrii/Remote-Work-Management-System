package com.rwms.audit.command;

import com.rwms.audit.entity.AuditLog;
import com.rwms.audit.repository.AuditLogRepository;
import com.rwms.user.entity.User;

public abstract class BaseAuditCommand implements AuditCommand {
    protected final User performedBy;
    protected final String userEmail;
    protected final String details;

    public BaseAuditCommand(User performedBy, String userEmail, String details) {
        this.performedBy = performedBy;
        this.userEmail = userEmail;
        this.details = details;
    }

    protected abstract String getActionName();

    @Override
    public void execute(AuditLogRepository repository) {
        AuditLog log = AuditLog.builder()
                .actionName(getActionName())
                .userEmail(userEmail)
                .performedBy(performedBy)
                .details(details)
                .build();
        repository.save(log);
    }
}
