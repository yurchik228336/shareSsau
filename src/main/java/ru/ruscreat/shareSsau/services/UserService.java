package ru.ruscreat.shareSsau.services;

import org.springframework.stereotype.Service;
import ru.ruscreat.shareSsau.models.User;
import ru.ruscreat.shareSsau.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
