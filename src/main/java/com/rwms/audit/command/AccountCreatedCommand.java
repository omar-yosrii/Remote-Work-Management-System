package com.rwms.audit.command;

import com.rwms.user.entity.User;

public class AccountCreatedCommand extends BaseAuditCommand {
    public AccountCreatedCommand(User performedBy, String userEmail, String details) {
        super(performedBy, userEmail, details);
    }

    @Override
    protected String getActionName() {
        return "ACCOUNT_CREATED";
    }
}
