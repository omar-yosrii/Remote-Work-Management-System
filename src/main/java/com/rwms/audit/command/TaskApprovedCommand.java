package com.rwms.audit.command;

import com.rwms.user.entity.User;

public class TaskApprovedCommand extends BaseAuditCommand {
    public TaskApprovedCommand(User performedBy, String userEmail, String details) {
        super(performedBy, userEmail, details);
    }

    @Override
    protected String getActionName() {
        return "TASK_APPROVED";
    }
}
