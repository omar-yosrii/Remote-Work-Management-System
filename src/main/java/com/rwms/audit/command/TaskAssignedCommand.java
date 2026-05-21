package com.rwms.audit.command;

import com.rwms.user.entity.User;

public class TaskAssignedCommand extends BaseAuditCommand {
    public TaskAssignedCommand(User performedBy, String userEmail, String details) {
        super(performedBy, userEmail, details);
    }

    @Override
    protected String getActionName() {
        return "TASK_ASSIGNED";
    }
}
