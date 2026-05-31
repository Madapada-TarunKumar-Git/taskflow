package com.example.taskflow.application.dto;

public record CustomerCsvRecord(
        String customerName,
        String email,
        String phone,
        String country
) {
}
