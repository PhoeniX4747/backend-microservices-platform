package com.example.order.dto;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, String requestId, int status, String error, String message, String path) {
}
