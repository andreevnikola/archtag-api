package com.andreev.archtag.user.dto.authentication;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionDto {
    private String id;
    private String status;
    private Long created;
    private Long currentPeriodStart;
    private Long currentPeriodEnd;
}
