package com.andreev.archtag.user.services.profile;

import com.andreev.archtag.global.exception.ApiRequestException;
import com.andreev.archtag.user.domain.authentication.UserEntity;
import com.andreev.archtag.user.dto.authentication.UpdateAccountRequest;
import com.andreev.archtag.user.repositories.authentication.UserRepository;
import com.andreev.archtag.user.services.authentication.JwtService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${storage.location}")
    private String storageLocationPath;

    private Path storageLocation;

    @PostConstruct
    private void init() {
        storageLocation = Paths.get(storageLocationPath);
    }

    public void deleteAccount(String email, String authToken) {
        UserEntity authUser = getUserFromToken(authToken);
        if (!authUser.getEmail().equals(email)) {
            throw new ApiRequestException(HttpStatus.UNAUTHORIZED, "You can only delete your own account.");
        }
        userRepo.deleteByEmail(email);
    }

    public void updateAccount(UpdateAccountRequest request, String authToken) {
        UserEntity authUser = getUserFromToken(authToken);
        if (!authUser.getEmail().equals(request.getEmail())) {
            throw new ApiRequestException(HttpStatus.UNAUTHORIZED, "You can only update your own account.");
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), authUser.getPassword())) {
                throw new ApiRequestException(HttpStatus.UNAUTHORIZED, "Current password is incorrect.");
            }
            authUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getFirstname() != null && !request.getFirstname().isEmpty()) {
            authUser.setFirstname(request.getFirstname());
        }
        if (request.getLastname() != null && !request.getLastname().isEmpty()) {
            authUser.setLastname(request.getLastname());
        }
        userRepo.save(authUser);
    }

    public Mono<Void> uploadProfilePicture(String email, MultipartFile file, String authToken) {
        return Mono.fromRunnable(() -> {
            UserEntity authUser = getUserFromToken(authToken);
            if (!authUser.getEmail().equals(email)) {
                throw new ApiRequestException(HttpStatus.UNAUTHORIZED, "You can only update your own profile picture.");
            }

            if (file.isEmpty()) {
                throw new ApiRequestException(HttpStatus.BAD_REQUEST, "File is empty.");
            }

            if (!isImage(file)) {
                throw new ApiRequestException(HttpStatus.BAD_REQUEST, "Only image files are allowed.");
            }

            if (file.getSize() > 10 * 1024 * 1024) {
                throw new ApiRequestException(HttpStatus.BAD_REQUEST, "File size exceeds the maximum limit of 10MB.");
            }

            try {
                if (!Files.exists(storageLocation)) {
                    Files.createDirectories(storageLocation);
                }

                BufferedImage originalImage = ImageIO.read(file.getInputStream());
                BufferedImage resizedImage = Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_WIDTH, 720);

                String originalFilename = file.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

                String uniqueFilename = authUser.getUuid() + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "." + fileExtension;
                Path targetLocation = storageLocation.resolve(uniqueFilename);
                ImageIO.write(resizedImage, fileExtension, new File(targetLocation.toString()));

                // Delete old profile picture if exists
                String oldProfilePictureFilename = authUser.getProfilePictureFilename();
                if (oldProfilePictureFilename != null && !oldProfilePictureFilename.isEmpty()) {
                    Path oldPath = storageLocation.resolve(oldProfilePictureFilename);
                    Files.deleteIfExists(oldPath);
                }

                // Save new profile picture filename
                authUser.setProfilePictureFilename(uniqueFilename);
                userRepo.save(authUser);

            } catch (IOException e) {
                throw new ApiRequestException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not process and store file. Please try again!");
            }
        });
    }

    private boolean isImage(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            return image != null;
        } catch (IOException e) {
            return false;
        }
    }

    private UserEntity getUserFromToken(String token) {
        String userUuid = jwtService.extractUuid(token);
        return userRepo.findByUuid(userUuid).orElseThrow(() -> new ApiRequestException(HttpStatus.UNAUTHORIZED, "Invalid token."));
    }
}
