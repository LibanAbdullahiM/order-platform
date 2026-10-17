package com.liban.orderplatform.auth.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties(prefix = "admin")
public record AdminProperties(String email, String password) {
}
