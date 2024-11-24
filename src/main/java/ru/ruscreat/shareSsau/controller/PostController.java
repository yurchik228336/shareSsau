package ru.ruscreat.shareSsau.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import ru.ruscreat.shareSsau.models.Post;
import ru.ruscreat.shareSsau.models.User;
import ru.ruscreat.shareSsau.services.PostService;
import ru.ruscreat.shareSsau.services.UserService;


import java.util.List;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

@Controller
public class PostController {
    private final PostService postService;
    private final UserService userService;


    // Список для хранения всех SSE эмиттеров
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;

    }

    @GetMapping("/profile")
    public String redirectToProfile() {
        return "redirect:/profile/";
    }

    @GetMapping("/")
    public String viewPosts(Model model) {
        // Получаем все посты, сортируем их по дате
        List<Post> sortedPosts = postService.getAllPosts().stream()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .collect(Collectors.toList());

        // Форматируем дату для каждого поста с учётом часового пояса
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy года HH:mm")
                .withLocale(new Locale("ru")); // Локализация на русский

        sortedPosts.forEach(post -> {
            ZonedDateTime zonedDateTime = post.getCreatedAt().atZone(ZoneId.systemDefault()); // Время по местному часовому поясу
            String formattedDate = zonedDateTime.format(formatter);
            post.setFormattedDate(formattedDate); // Сохраняем отформатированную дату в объекте

            // Задаем аватарку автора, если она есть, или по умолчанию
            if (post.getAuthor() != null) {
                String avatarUrl = getAvatarForUser(post.getAuthor());
                post.setAuthorAvatar(avatarUrl);
            }
        });

        model.addAttribute("posts", sortedPosts);
        return "posts";
    }

    @GetMapping("/new")
    public String newPostForm(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        model.addAttribute("post", new Post());
        return "new-post";
    }

    @PostMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String createPost(@ModelAttribute @Valid Post post, BindingResult result, Model model, Authentication authentication) {
        if (result.hasErrors()) {
            return "new-post";
        }

        // Проверяем на наличие зальго-символов
        if (containsZalgo(post.getTitle()) || containsZalgo(post.getContent())) {
            model.addAttribute("errorMessage", "Текст содержит недопустимые символы.");
            return "new-post";
        }

        // Получаем аватарку автора и добавляем её в объект поста
        String avatarUrl = getAvatarForUser(authentication.getName());
        post.setAuthorAvatar(avatarUrl);

        // Сохраняем пост
        postService.savePost(post);

        // Оповещаем всех подключенных пользователей о новом посте
        notifyClients(post);

        return "redirect:/";
    }
    private boolean containsZalgo(String text) {
        if (text == null) {
            return false;
        }
        String regex = "[\u0E00-\u0E7F\u0E80-\u0EFF\u0F00-\u0FFF\u2000-\u206F\u3000-\u303F]+";
        return text.matches(regex);
    }



    // SSE endpoint для получения обновлений
    @GetMapping("/stream")
    @ResponseBody
    public SseEmitter streamPosts() {
        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);

        // Закрытие соединения после определенного времени (например, 10 минут)
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        return emitter;
    }

    // Оповещение всех пользователей о новом посте
    private void notifyClients(Post post) {
        emitters.forEach(emitter -> {
            try {
                emitter.send(post); // Отправка нового поста
            } catch (Exception e) {
                emitters.remove(emitter); // Удаляем неисправные эмиттеры
            }
        });
    }

    // Пример метода для получения аватарки пользователя или ставить по умолчанию
    private String getAvatarForUser(String username) {
        // Получаем пользователя по имени
        User user = userService.getUserByUsername(username).orElse(null);

        if (user != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            return user.getAvatar(); // Возвращаем аватарку
        } else {
            return "/uploads/avatars/default.png"; // Аватар по умолчанию
        }
    }

    @PostMapping("/upload")
    @ResponseBody
    public Map<String, Object> uploadFile(@RequestParam("upload") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Генерация случайного 32-символьного имени файла
            String fileName = UUID.randomUUID().toString().replace("-", "") +
                    "." + getFileExtension(file.getOriginalFilename());

            Path path = Paths.get("uploads/" + fileName);
            Files.write(path, file.getBytes());

            // Формируем URL для загрузки
            String url = "/uploads/" + fileName;

            // Формируем успешный ответ
            response.put("uploaded", true);
            response.put("url", url);
        } catch (IOException e) {
            // Формируем ответ с ошибкой
            response.put("uploaded", false);
            response.put("error", Map.of("message", "Ошибка загрузки файла"));
        }
        return response;
    }

    // Метод для получения расширения файла
    private String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot != -1) {
            return fileName.substring(lastIndexOfDot + 1);
        } else {
            return ""; // Если расширение не найдено, возвращаем пустую строку
        }
    }

    @GetMapping("/uploads/{fileName}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String fileName) throws IOException {
        Path path = Paths.get("uploads/" + fileName);
        Resource resource = new UrlResource(path.toUri());
        String contentType = Files.probeContentType(path);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping("/stats")
    public String getStatsPage() {
        return "stats";
    }
}
