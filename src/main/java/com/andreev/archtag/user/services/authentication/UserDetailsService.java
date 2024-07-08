package com.andreev.archtag.user.services.authentication;

import com.andreev.archtag.global.exception.ApiRequestException;
import com.andreev.archtag.payment.services.PaymentService;
import com.andreev.archtag.user.domain.authentication.UserEntity;
import com.andreev.archtag.user.dto.authentication.SubscriptionDto;
import com.andreev.archtag.user.dto.authentication.UserDto;
import com.andreev.archtag.user.repositories.authentication.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PaymentService paymentService;

    public UserDto getUserByToken(String token) throws ApiRequestException, StripeException {
        String uuid = jwtService.extractUuid(token);
        UserEntity user = getUserByUuid(uuid);

        SubscriptionDto subscriptionDto = getUserSubscription(user);

        return UserDto.builder()
                .uuid(uuid)
                .firstname(jwtService.extractFirstName(token))
                .lastname(jwtService.extractLastName(token))
                .email(jwtService.extractEmail(token))
                .role(jwtService.extractClaim(token, "role"))
                .isBanned(jwtService.extractClaim(token, "isBanned"))
                .isVerified(jwtService.extractClaim(token, "isVerified"))
                .profilePictureFilename(jwtService.extractClaim(token, "profilePictureFilename"))
                .subscription(subscriptionDto)
                .build();
    }

    public UserEntity getUserByUuid(String uuid) throws UsernameNotFoundException {
        return userRepository.findByUuid(uuid).orElseThrow(() -> new UsernameNotFoundException("This user does not exist."));
    }

    public void deleteUserByEmail(String email) {
        userRepository.deleteByEmail(email);
    }

    private SubscriptionDto getUserSubscription(UserEntity user) throws StripeException {
        Subscription subscription = paymentService.getUserSubscription(user);
        return SubscriptionDto.builder()
                .id(subscription.getId())
                .status(subscription.getStatus())
                .created(subscription.getCreated())
                .currentPeriodStart(subscription.getCurrentPeriodStart())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("This user does not exist."));
    }
}