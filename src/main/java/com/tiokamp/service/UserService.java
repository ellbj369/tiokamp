package com.tiokamp.service;

import com.tiokamp.model.User;
import com.tiokamp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".heic");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Transactional
    public User register(String username, String password, MultipartFile profilePicture) throws IOException {
        String name = username == null ? "" : username.trim();
        if (!name.matches("[\\p{L}\\p{N} ._-]{2,30}")) {
            throw new IllegalArgumentException(
                    "Användarnamnet måste vara 2–30 tecken: bokstäver, siffror, mellanslag, punkt eller bindestreck.");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Lösenordet måste vara minst 4 tecken.");
        }
        if (userRepository.existsByUsernameIgnoreCase(name)) {
            throw new IllegalArgumentException("Användarnamnet '" + name + "' är upptaget — välj ett annat.");
        }
        String contentType = profilePicture.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Filen måste vara en bild.");
        }

        String filename = saveProfilePicture(profilePicture);
        String encoded  = passwordEncoder.encode(password);
        User user = new User(name, encoded, filename);
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Hittade ingen användare: " + username));
    }

    private String saveProfilePicture(MultipartFile file) throws IOException {
        Path dir = Paths.get(uploadDir).toAbsolutePath();
        Files.createDirectories(dir);

        String originalName = file.getOriginalFilename();
        String extension    = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase()
                : ".jpg";
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            extension = ".jpg";
        }

        String filename = UUID.randomUUID() + extension;
        Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }
}
