package com.andreev.archtag.user.domain.authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserFromToken {

    private String uuid;
    private String email;
    private String role;
    private Boolean isBanned;
    private Boolean isVerified;
    private String verificationCode;
    private String resetPasswordCode;
    private String profilePictureFilename;

    public UserFromToken(String token) {
        this.extractSelfFromToken(token);
    }

    public UserFromToken() {

    }

    private void extractSelfFromToken(String token) {

    }
}
