package com.company.demo.config;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;

@Source(type = SourceType.APP)
public interface FacebookConfig extends Config {

    /**
     * @return a set of fields to fetch in user data
     */
    @Default("id,name,email")
    @Property("facebook.fields")
    String getFacebookFields();

    /**
     * @return app client id provided by service
     */
    @Property("facebook.appId")
    String getFacebookAppId();

    /**
     * @return app client secret provided by service
     */
    @Property("facebook.appSecret")
    String getFacebookAppSecret();
}