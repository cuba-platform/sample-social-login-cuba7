package com.company.demo.service;

import com.company.demo.config.SocialRegistrationConfig;
import com.company.demo.entity.SocialUser;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.EntitySet;
import com.haulmont.cuba.security.entity.Group;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.entity.UserRole;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service(SocialRegistrationService.NAME)
public class SocialRegistrationServiceBean implements SocialRegistrationService {

    @Inject
    private DataManager dataManager;
    @Inject
    private Configuration configuration;

    @Override
    public User findOrRegisterUser(String facebookId, String email, String name) {
        User existingUser = dataManager.load(User.class)
                .query("select u from sec$User u where u.facebookId = :facebookId")
                .parameter("facebookId", facebookId)
                .optional()
                .orElse(null);
        if (existingUser != null) {
            return existingUser;
        }
        SocialUser user = dataManager.create(SocialUser.class);
        user.setLogin(email);
        user.setName(name);
        user.setGroup(getDefaultGroup());
        user.setActive(true);
        user.setEmail(email);
        user.setFacebookId(facebookId);
        UserRole fbUserRole = dataManager.create(UserRole.class);
        fbUserRole.setRoleName("facebook-access");
        fbUserRole.setUser(user);
        EntitySet eSet = dataManager.commit(user, fbUserRole);
        return eSet.get(user);
    }

    private Group getDefaultGroup() {
        SocialRegistrationConfig config = configuration.getConfig(SocialRegistrationConfig.class);

        return dataManager.load(Group.class)
                .query("select g from sec$Group g where g.id = :defaultGroupId")
                .parameter("defaultGroupId", config.getDefaultGroupId())
                .one();
    }
}