package com.andreev.security.user.dto.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SigninRequest {

    @NotBlank
    @Email
    @Size(min = 4, max = 35)
    private String email;

    @NotBlank
    @Size(min = 6, max = 35)
    private String password;
}