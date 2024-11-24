package ru.ruscreat.shareSsau.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import ru.ruscreat.shareSsau.models.Post;
import ru.ruscreat.shareSsau.models.User;
import ru.ruscreat.shareSsau.services.PostService;
import ru.ruscreat.shareSsau.services.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    private final UserService userService;
    private final PostService postService;

    public UserProfileController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    // Метод для просмотра профиля, как текущего пользователя, так и другого пользователя по username
    @GetMapping({"/", "/{username}"})
    public String viewProfile(@PathVariable(required = false) String username, Model model, Authentication authentication) {
        if (username == null && authentication == null) {
            return "redirect:/login";
        }

        if (username == null) {
            username = authentication.getName();
        }

        User user = userService.getUserByUsername(username)
                .orElse(null);

        if (user == null) {
            return "user-not-found";
        }

        List<Post> posts = postService.getPostsByAuthor(username).stream()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .collect(Collectors.toList());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy года HH:mm")
                .withLocale(new Locale("ru"));

        posts.forEach(post -> {
            ZonedDateTime zonedDateTime = post.getCreatedAt().atZone(ZoneId.systemDefault());
            String formattedDate = zonedDateTime.format(formatter);
            post.setFormattedDate(formattedDate);

            if (post.getAuthor() != null) {
                String avatarUrl = getAvatarForUser(post.getAuthor());
                post.setAuthorAvatar(avatarUrl);
            }
        });

        model.addAttribute("user", user);
        model.addAttribute("posts", posts);

        return "profile";
    }



    @GetMapping("/profileEdit")
    public String editProfile(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login"; // Перенаправить на страницу входа, если пользователь не авторизован
        }

        String username = authentication.getName();
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        model.addAttribute("user", user);
        return "profile-edit"; // Страница редактирования
    }
    private boolean containsZalgo(String text) {
        if (text == null) {
            return false;
        }
        String regex = "[\u0E00-\u0E7F\u0E80-\u0EFF\u0F00-\u0FFF\u2000-\u206F\u3000-\u303F]+";
        return text.matches(regex);
    }
    @PostMapping("/update-profile")
    @ResponseBody
    public Map<String, Object> updateProfile(
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            @RequestParam(value = "banner", required = false) MultipartFile banner,
            @RequestParam(value = "bio", required = false) String bio,
            Authentication authentication) {
        if (containsZalgo(bio))bio=null;

        Map<String, Object> response = new HashMap<>();
        String username = authentication.getName();
        try {
            User user = userService.getUserByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

            // Обновление аватара, если он был загружен
            if (avatar != null && !avatar.isEmpty()) {
                String avatarUrl = uploadFile(avatar, "avatars", user);
                userService.updateAvatar(user, avatarUrl); // Обновление аватарки пользователя в базе данных

                // Теперь обновляем аватарки во всех постах пользователя
                List<Post> posts = postService.getPostsByAuthor(username);
                for (Post post : posts) {
                    post.setAuthorAvatar(avatarUrl); // Обновляем аватарку в постах
                    postService.savePost(post); // Сохраняем обновленный пост
                }

                response.put("avatarUrl", avatarUrl); // Ответ с новым URL аватарки
            }

            // Обновление баннера, если он был загружен
            if (banner != null && !banner.isEmpty()) {
                String bannerUrl = uploadFile(banner, "banners", user);
                userService.updateBanner(user, bannerUrl);
                response.put("bannerUrl", bannerUrl);
            }

            // Обновление биографии, если она была изменена
            if (bio != null && !bio.isEmpty()) {
                userService.updateBio(user, bio);
                response.put("bio", bio);
            }

            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }


    private String uploadFile(MultipartFile file, String folder, User user) throws IOException {
        String contentType = file.getContentType();
        String extension = getFileExtension(contentType);

        if (extension == null) {
            throw new IOException("Неверный формат файла");
        }

        String fileName = UUID.randomUUID().toString() + "." + extension;
        Path path = Paths.get("uploads/" + folder + "/" + fileName);

        Files.write(path, file.getBytes());
        return "/uploads/" + folder + "/" + fileName;
    }


    private String getFileExtension(String contentType) {
        switch (contentType) {
            case "image/jpeg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/gif":
                return "gif";
            case "image/bmp":
                return "bmp";
            default:
                return null;  // Поддерживаем только изображение
        }
    }
    public String getAvatarForUser(String username) {
        // Получаем пользователя по имени
        User user = userService.getUserByUsername(username).orElse(null);

        // Проверяем, есть ли у пользователя аватарка
        if (user != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            return user.getAvatar(); // Если аватарка есть, возвращаем её
        } else {
            // Если аватарки нет, возвращаем путь к изображению по умолчанию
            return "/uploads/avatars/img.png";
        }
    }
}
