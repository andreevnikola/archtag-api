package com.andreev.archtag.user.dto.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgottenPassResponse {
    private boolean success;
    private String message;
}
