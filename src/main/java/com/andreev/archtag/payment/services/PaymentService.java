package com.andreev.archtag.payment.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PriceListParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.andreev.archtag.user.repositories.authentication.UserRepository;
import com.andreev.archtag.user.domain.authentication.UserEntity;
import com.andreev.archtag.user.services.authentication.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepo;
    private final JwtService jwtService;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    public String createCheckoutSession(String authToken, String lookupKey) throws StripeException {
        String userUuid = jwtService.extractUuid(authToken);
        UserEntity user = userRepo.findByUuid(userUuid).orElseThrow(() -> new IllegalArgumentException("Потребителят не съществува!"));

        String customerId = user.getStripeCustomerId();
        if (customerId == null || customerId.isEmpty()) {
            customerId = createStripeCustomer(user.getEmail());
            user.setStripeCustomerId(customerId);
            userRepo.save(user);
        }

        Price price = getPriceByLookupKey(lookupKey);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setCustomer(customerId)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(price.getId())
                        .setQuantity(1L)
                        .build())
                .build();

        Session session = Session.create(params);

        return session.getUrl();
    }

    private String createStripeCustomer(String email) throws StripeException {
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(email)
                .build();
        Customer customer = Customer.create(params);
        return customer.getId();
    }

    private Price getPriceByLookupKey(String lookupKey) throws StripeException {
        PriceListParams params = PriceListParams.builder()
                .addLookupKey(lookupKey)
                .build();

        List<Price> prices = Price.list(params).getData();
        if (prices.isEmpty()) {
            throw new IllegalArgumentException("Не е намерен такъв пакет!  " + lookupKey);
        }
        return prices.get(0);
    }
}