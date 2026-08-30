package com.example.pi.service;

import com.example.pi.dto.request.TransferRequest;
import com.example.pi.dto.response.TransferResponse;

public interface TransferService {
    /**
     * Atomically transfer funds from source account to destination account.
     * Locking order is deterministic (min account id first) to prevent deadlocks (doc item #8).
     * Idempotency key is honoured — duplicate requests return the original response (doc item #7).
     *
     * @param request the transfer request
     * @param idempotencyKey client-supplied dedup key (may be null; falls back to request body field)
     */
    TransferResponse transfer(TransferRequest request, String idempotencyKey);
}
