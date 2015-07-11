package com.iconectiv.common.controller;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by ramp on 7/10/2015.
 */
@Configuration
@ConfigurationProperties(prefix = "app")
public class Config {
    @NotBlank
    String version;
    @Valid
    List<String> queryFiles;

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setQueryFiles(List<String> queryFiles) {
        this.queryFiles = queryFiles;
    }

    public List<String> getQueryFiles() {
        return queryFiles;
    }
}
