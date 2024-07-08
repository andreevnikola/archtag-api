package com.andreev.archtag.payment.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateCheckoutSessionRequest {

    @NotEmpty
    private String lookupKey;
}