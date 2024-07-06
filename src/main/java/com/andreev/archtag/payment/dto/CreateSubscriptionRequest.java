package com.andreev.archtag.payment.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateSubscriptionRequest {
    @NotEmpty
    private String customerId;

    @NotEmpty
    private String priceId;
}
