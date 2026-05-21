package com.rwms.audit.command;

import com.rwms.user.entity.User;

public class TaskSubmittedCommand extends BaseAuditCommand {
    public TaskSubmittedCommand(User performedBy, String userEmail, String details) {
        super(performedBy, userEmail, details);
    }

    @Override
    protected String getActionName() {
        return "TASK_SUBMITTED";
    }
}
