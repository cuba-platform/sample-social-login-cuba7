package com.company.demo.core.role;

import com.haulmont.cuba.security.app.role.AnnotatedRoleDefinition;
import com.haulmont.cuba.security.app.role.annotation.Role;
import com.haulmont.cuba.security.app.role.annotation.ScreenAccess;
import com.haulmont.cuba.security.role.ScreenPermissionsContainer;

@Role(name = "facebook-access")
public class FacebookAccessRole extends AnnotatedRoleDefinition {
    @ScreenAccess(screenIds = {
            "help",
            "aboutWindow",
            "settings",
    })
    @Override
    public ScreenPermissionsContainer screenPermissions() {
        return super.screenPermissions();
    }
}
