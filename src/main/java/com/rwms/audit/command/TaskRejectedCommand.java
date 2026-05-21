package com.rwms.audit.command;

import com.rwms.user.entity.User;

public class TaskRejectedCommand extends BaseAuditCommand {
    public TaskRejectedCommand(User performedBy, String userEmail, String details) {
        super(performedBy, userEmail, details);
    }

    @Override
    protected String getActionName() {
        return "TASK_REJECTED";
    }
}
