package com.example.pi.controller;

import com.example.pi.dto.request.TransferRequest;
import com.example.pi.dto.response.TransferResponse;
import com.example.pi.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Transfer controller (doc item #9).
 *
 * POST /api/v1/transfers — atomically moves funds between two accounts.
 * Accepts an optional Idempotency-Key HTTP header for safe client retries (doc item #7).
 */
@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    /**
     * Execute an atomic A→B fund transfer.
     *
     * The optional {@code Idempotency-Key} header makes this endpoint safe to retry:
     * re-submitting the same key returns the original response without processing
     * a second transfer.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        TransferResponse response = transferService.transfer(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
