package com.social.core.models;

public enum RuleEnum {
    ADMIN("ADMIN"),

    SUPER_ADMIN("SUPER_ADMIN"),
    
    NORMAL("NORMAL"),

    ANONYMOUS("ANONYMOUS"),
    
    SYSTEM_CONTENT("SYSTEM_CONTENT"),
    
    TESTING("TESTING");


    private String value;

    RuleEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static RuleEnum fromValue(String text) {
      for (RuleEnum b : RuleEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }