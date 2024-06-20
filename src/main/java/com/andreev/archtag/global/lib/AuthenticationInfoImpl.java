package com.andreev.archtag.global.lib;

import com.andreev.archtag.user.domain.authentication.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationInfoImpl implements AuthenticationInfo {
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public UserEntity getUserEntity() {
        return (UserEntity) getAuthentication().getDetails();
    }
}
