package com.andreev.archtag.global.lib;

import com.andreev.archtag.user.domain.authentication.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationInfo {

    UserEntity getUserEntity();
    Authentication getAuthentication();
}
