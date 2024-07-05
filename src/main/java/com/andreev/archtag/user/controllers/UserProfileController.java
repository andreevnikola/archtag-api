package com.andreev.archtag.user.controllers;

import com.andreev.archtag.global.exception.ApiRequestException;
import com.andreev.archtag.user.dto.authentication.DeleteAccountRequest;
import com.andreev.archtag.user.dto.authentication.UpdateAccountRequest;
import com.andreev.archtag.user.services.profile.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.security.InvalidParameterException;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping("/delete-account")
    public ResponseEntity<Void> deleteAccount(@Valid @RequestBody DeleteAccountRequest request) {
        try {
            userProfileService.deleteAccount(request.getPassword());
            return ResponseEntity.ok().build();
        } catch (InvalidParameterException e) {
            throw new ApiRequestException(HttpStatus.BAD_REQUEST, "Грешна парола!");
        } catch (Exception e) {
            throw new ApiRequestException(HttpStatus.INTERNAL_SERVER_ERROR, "Имаше грешка със изтриването на акаунта. Моля, опитайте отново.");
        }
    }

    @PostMapping("/update-account")
    public ResponseEntity<Void> updateAccount(@Valid @RequestBody UpdateAccountRequest request, @RequestHeader("Authorization") String authToken) {
        try {
            userProfileService.updateAccount(request, authToken.substring(7)); // remove "Bearer " prefix
            return ResponseEntity.ok().build();
        } catch (InvalidParameterException e) {
            throw new ApiRequestException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            throw new ApiRequestException(HttpStatus.INTERNAL_SERVER_ERROR, "Имаше грешка със обновяването на акаунта. Моля, опитайте отново.");
        }
    }

    @PostMapping("/upload-profile-picture")
    public Mono<ResponseEntity<Void>> uploadProfilePicture(
            @RequestParam("email") String email,
            @RequestParam("profilePicture") MultipartFile profilePicture,
            @RequestHeader("Authorization") String authToken
    ) {
        return userProfileService.uploadProfilePicture(email, profilePicture, authToken.substring(7)) // remove "Bearer " prefix
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(InvalidParameterException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()))
                .onErrorResume(RuntimeException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
}