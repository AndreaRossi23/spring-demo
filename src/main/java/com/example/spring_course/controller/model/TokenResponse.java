package com.example.spring_course.controller.model;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {}
