package com.andreev.archtag.payment.controllers;

import com.andreev.archtag.payment.dto.CreateSubscriptionRequest;
import com.andreev.archtag.payment.services.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-subscription")
    public ResponseEntity<Subscription> createSubscription(@Valid @RequestBody CreateSubscriptionRequest request) {
        try {
            Subscription subscription = paymentService.createSubscription(request.getCustomerId(), request.getPriceId());
            return ResponseEntity.ok(subscription);
        } catch (StripeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }
}
