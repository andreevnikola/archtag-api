package com.andreev.archtag.user.dto.authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserDto {

    private String uuid;
    private String firstname;
    private String lastname;
    private String email;
    private String role;
    private boolean isBanned;
    private boolean isVerified;
}
