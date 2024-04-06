package com.andreev.security.services.authentication;

import com.andreev.security.domain.authentication.UserEntity;
import com.andreev.security.repositories.authentication.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    public UserEntity getUserByUuid(String uuid) throws UsernameNotFoundException {
        return userRepository.findByUuid(uuid).orElseThrow(() -> new UsernameNotFoundException("This user does not exist."));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("This user does not exist."));
    }
}
