package com.andreev.archtag.user.services.authentication;

import com.andreev.archtag.global.exception.ApiRequestException;
import com.andreev.archtag.user.domain.authentication.UserEntity;
import com.andreev.archtag.user.dto.authentication.UserDto;
import com.andreev.archtag.user.repositories.authentication.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public UserDto getUserByToken(String token) throws ApiRequestException {
        String uuid = jwtService.extractUuid(token);
        return UserDto.builder()
                .uuid(uuid)
                .firstname(jwtService.extractFirstName(token))
                .lastname(jwtService.extractLastName(token))
                .email(jwtService.extractEmail(token))
                .role(jwtService.extractClaim(token, "role"))
                .isBanned(jwtService.extractClaim(token, "isBanned"))
                .isVerified(jwtService.extractClaim(token, "isVerified"))
                .profilePictureFilename(jwtService.extractClaim(token, "profilePictureFilename"))
                .build();
    }

    public UserEntity getUserByUuid(String uuid) throws UsernameNotFoundException {
        return userRepository.findByUuid(uuid).orElseThrow(() -> new UsernameNotFoundException("This user does not exist."));
    }

    public void deleteUserByEmail(String email) {
        userRepository.deleteByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("This user does not exist."));
    }
}
