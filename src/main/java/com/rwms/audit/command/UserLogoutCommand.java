package com.rwms.audit.command;

import com.rwms.user.entity.User;

public class UserLogoutCommand extends BaseAuditCommand {
    public UserLogoutCommand(User performedBy, String userEmail, String details) {
        super(performedBy, userEmail, details);
    }

    @Override
    protected String getActionName() {
        return "USER_LOGOUT";
    }
}
