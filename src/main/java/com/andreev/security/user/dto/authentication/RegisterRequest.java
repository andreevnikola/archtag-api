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
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 12)
    private String firstname;

    @NotBlank
    @Size(min = 3, max = 16)
    private String lastname;

    @NotBlank
    @Email
    @Size(min = 4, max = 35)
    private String email;

    @NotBlank
    @Size(min = 6, max = 35)
    private String password;
}
