package com.andreev.archtag.user.services.profile;

import com.andreev.archtag.global.lib.AuthenticationInfo;
import com.andreev.archtag.user.domain.authentication.UserEntity;
import com.andreev.archtag.user.dto.authentication.UpdateAccountRequest;
import com.andreev.archtag.user.repositories.authentication.RefreshTokenRepository;
import com.andreev.archtag.user.repositories.authentication.UserRepository;
import com.andreev.archtag.user.services.authentication.JwtService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationInfo authenticationInfo;
    private final RefreshTokenRepository refreshTokenRepo;

    @Value("${storage.location}")
    private String storageLocationPath;

    private Path storageLocation;

    @PostConstruct
    private void init() {
        storageLocation = Paths.get(storageLocationPath);
    }

    public void deleteAccount(String password) {
        String email = authenticationInfo.getUserEntity().getEmail();

        UserEntity user = userRepo.findByEmail(email).orElseThrow();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidParameterException("Password is incorrect.");
        }

        userRepo.deleteByEmail(email);
    }

    public void updateAccount(UpdateAccountRequest request, String authToken) {
        String userUuid = jwtService.extractUuid(authToken);
        UserEntity authUser = userRepo.findByUuid(userUuid).orElseThrow();

        if (!authUser.getEmail().equals(request.getEmail())) {
            throw new InvalidParameterException("You can only update your own account.");
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), authUser.getPassword())) {
                throw new InvalidParameterException("Грешна парола!");
            }
            authUser.setPassword(passwordEncoder.encode(request.getPassword()));
            refreshTokenRepo.deleteAllByUserUuid(authUser.getUuid());
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
            String userUuid = jwtService.extractUuid(authToken);
            UserEntity authUser = userRepo.findByUuid(userUuid).orElseThrow();

            if (!authUser.getEmail().equals(email)) {
                throw new InvalidParameterException("You can only update your own profile picture.");
            }

            if (file.isEmpty()) {
                throw new InvalidParameterException("File is empty.");
            }

            if (!isImage(file)) {
                throw new InvalidParameterException("Only image files are allowed.");
            }

            if (file.getSize() > 10 * 1024 * 1024) {
                throw new InvalidParameterException("File size exceeds the maximum limit of 10MB.");
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
                throw new RuntimeException("Could not process and store file. Please try again!");
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
}
