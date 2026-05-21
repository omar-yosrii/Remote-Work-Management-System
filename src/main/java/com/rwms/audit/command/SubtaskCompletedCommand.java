package com.rwms.audit.command;

import com.rwms.user.entity.User;

public class SubtaskCompletedCommand extends BaseAuditCommand {
    public SubtaskCompletedCommand(User performedBy, String userEmail, String details) {
        super(performedBy, userEmail, details);
    }

    @Override
    protected String getActionName() {
        return "SUBTASK_COMPLETED";
    }
}
