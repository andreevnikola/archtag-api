package com.andreev.archtag.payment.controllers;

import com.andreev.archtag.payment.dto.CreateCheckoutSessionRequest;
import com.andreev.archtag.payment.services.PaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<String> createCheckoutSession(
            @RequestHeader("Authorization") String authToken,
            @Valid @RequestBody CreateCheckoutSessionRequest request) {
        try {
            String token = authToken.replace("Bearer ", "");
            String url = paymentService.createCheckoutSession(token, request.getLookupKey());
            return ResponseEntity.ok(url);
        } catch (StripeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }
}