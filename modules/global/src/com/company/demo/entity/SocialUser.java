package com.company.demo.entity;

import com.haulmont.cuba.core.entity.annotation.Extends;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "demo_SocialUser")
@Extends(User.class)
public class SocialUser extends User {
    private static final long serialVersionUID = 28195380502367880L;

    @Column(name = "FACEBOOK_ID")
    private String facebookId;

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }
}