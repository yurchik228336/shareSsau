package ru.ruscreat.shareSsau.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ru.ruscreat.shareSsau.models.User;
import ru.ruscreat.shareSsau.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserService() {
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void updateAvatar(User user, String avatarPath) {
        user.setAvatar(avatarPath);  // Обновляем аватарку
        userRepository.save(user);
    }
    public void updateBio(User user, String bio) {
        user.setBio(bio);  // Обновляем биографию
        userRepository.save(user);  // Сохраняем изменения
    }

    public void updateBanner(User user, String bannerPath) {
        user.setBanner(bannerPath);  // Обновляем баннер
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public String saveUserFile(MultipartFile file, String directory) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get("uploads", directory);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return "/uploads/" + directory + "/" + fileName;
    }
}
