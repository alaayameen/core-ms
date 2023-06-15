package com.social.core.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UsedLoginEnum {
    EMAIL("EMAIL"),

    MOBILE("MOBILE"),

    FACEBOOK("FACEBOOK"),

    GOOGLE("GOOGLE"),

    APPLE("APPLE"),

    APP_USERNAME("APP_USERNAME");

    private String value;

    UsedLoginEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static UsedLoginEnum fromValue(String text) {
      for (UsedLoginEnum b : UsedLoginEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }