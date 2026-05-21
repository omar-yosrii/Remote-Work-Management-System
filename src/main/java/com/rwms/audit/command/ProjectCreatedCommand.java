package com.rwms.audit.command;

import com.rwms.user.entity.User;

public class ProjectCreatedCommand extends BaseAuditCommand {
    public ProjectCreatedCommand(User performedBy, String userEmail, String details) {
        super(performedBy, userEmail, details);
    }

    @Override
    protected String getActionName() {
        return "PROJECT_CREATED";
    }
}
