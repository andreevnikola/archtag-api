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

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping("/delete-account")
    public ResponseEntity<Void> deleteAccount(@Valid @RequestBody DeleteAccountRequest request, @RequestHeader("Authorization") String authToken) {
        try {
            userProfileService.deleteAccount(request.getEmail(), authToken.substring(7)); // remove "Bearer " prefix
            return ResponseEntity.ok().build();
        } catch (ApiRequestException e) {
            return ResponseEntity.status(e.getStatus()).build();
        }
    }

    @PostMapping("/update-account")
    public ResponseEntity<Void> updateAccount(@Valid @RequestBody UpdateAccountRequest request, @RequestHeader("Authorization") String authToken) {
        try {
            userProfileService.updateAccount(request, authToken.substring(7)); // remove "Bearer " prefix
            return ResponseEntity.ok().build();
        } catch (ApiRequestException e) {
            return ResponseEntity.status(e.getStatus()).build();
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
                .onErrorResume(ApiRequestException.class, e ->
                        Mono.just(ResponseEntity.status(e.getStatus()).build()));
    }
}
