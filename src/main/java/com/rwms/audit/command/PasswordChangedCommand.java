package com.rwms.audit.command;

import com.rwms.user.entity.User;

public class PasswordChangedCommand extends BaseAuditCommand {
    public PasswordChangedCommand(User performedBy, String userEmail, String details) {
        super(performedBy, userEmail, details);
    }

    @Override
    protected String getActionName() {
        return "PASSWORD_CHANGED";
    }
}
