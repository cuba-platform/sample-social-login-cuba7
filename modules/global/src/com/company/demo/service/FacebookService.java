package com.company.demo.service;

import java.io.Serializable;

public interface FacebookService {
    String NAME = "demo_FacebookService";

    String getLoginUrl(String appUrl, OAuth2ResponseType responseType);

    FacebookUserData getUserData(String appUrl, String code);

    enum OAuth2ResponseType {
        CODE("code"),
        TOKEN("token"),
        CODE_TOKEN("code%20token");

        private final String id;

        OAuth2ResponseType(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    class FacebookUserData implements Serializable {

        private String id;
        private String email;
        private String name;

        public FacebookUserData(String id, String email, String name) {
            this.id = id;
            this.email = email;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "FacebookUserData{" +
                    "id='" + id + '\'' +
                    ", email='" + email + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}