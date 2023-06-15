package com.social.core.models;

import lombok.Data;

import java.util.Locale;

@Data
public class UserInfo {
    private String subject;
    private String id;
    private String email;
    private String mobileNumber;
    private UsedLoginEnum usedLoginEnum;
    private Locale locale;
    private RuleEnum rule;
    private Boolean isDeleted;
    private Boolean isActive;
    private String userName;
    private String appUserName;
    private Integer numericUserId;
}
