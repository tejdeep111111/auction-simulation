package com.auction.util;

import com.auction.model.User;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Enums {
    public enum UserRole {
        USER("user"), ADMIN("admin");

        private final String value;
        private static final Map<String, UserRole> BY_VALUE = new HashMap<>(2);

        //static block to initilize Map that stores enums
        static {
            for(UserRole role : values()) {
                BY_VALUE.put(role.value.toLowerCase(), role);
            }
        }

        //Constructor
        UserRole(String value) {
            this.value=value;
        }

        public String getValue() {
            return value;
        }

        //Search for ENUM by its value
        public static UserRole getEnumByValue(String value) {
            return BY_VALUE.get(value.toLowerCase());
        }
    }
}
