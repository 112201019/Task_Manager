package com.projects.task_manager.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public boolean isValid(String passwordField, ConstraintValidatorContext context) {
        if (passwordField == null || passwordField.length() < 8) {
            setCustomErrorMessage(context, "Password must be at least 8 characters long.");
            return false;
        }

        if (isPasswordPwned(passwordField)) {
            setCustomErrorMessage(context, "This password has appeared in a data breach. Please choose a different one.");
            return false;
        }

        return true;
    }

    private boolean isPasswordPwned(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02X", b));
            }
            String sha1Hash = sb.toString();

            String prefix = sha1Hash.substring(0, 5);
            String suffix = sha1Hash.substring(5);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.pwnedpasswords.com/range/" + prefix))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String[] lines = response.body().split("\\r?\\n");
                for (String line : lines) {
                    // The API returns lines in the format: SUFFIX:COUNT
                    if (line.startsWith(suffix)) {
                        return true; // Password is compromised!
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to connect to HIBP API: " + e.getMessage());
            return false;
        }

        return false;
    }

    private void setCustomErrorMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}