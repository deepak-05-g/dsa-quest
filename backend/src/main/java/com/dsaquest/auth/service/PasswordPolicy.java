package com.dsaquest.auth.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordPolicy {
    private static final Pattern POLICY = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{12,72}$");

    public boolean isValid(String password) {
        return password != null && POLICY.matcher(password).matches();
    }
}
