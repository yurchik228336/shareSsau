package ru.ruscreat.shareSsau.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.ruscreat.shareSsau.models.Post;
import ru.ruscreat.shareSsau.models.User;
import ru.ruscreat.shareSsau.services.PostService;
import ru.ruscreat.shareSsau.services.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String adminPanel(Model model) {
        List<User> users = userService.getAllUsers();
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("users", users);
        model.addAttribute("posts", posts);
        return "admin/panel";
    }

    @DeleteMapping("/posts/{id}")
    @ResponseBody
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) MultipartFile avatar,
            @RequestParam(required = false) MultipartFile banner) {
        
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

            if (username != null && !username.isEmpty()) {
                user.setUsername(username);
            }

            if (password != null && !password.isEmpty()) {
                user.setPassword(password); // BCrypt кодирование происходит в сеттере
            }

            if (bio != null) {
                user.setBio(bio);
            }

            if (avatar != null && !avatar.isEmpty()) {
                String avatarUrl = userService.saveUserFile(avatar, "avatars");
                user.setAvatar(avatarUrl);
            }

            if (banner != null && !banner.isEmpty()) {
                String bannerUrl = userService.saveUserFile(banner, "banners");
                user.setBanner(bannerUrl);
            }

            userService.saveUser(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/users/{id}/role")
    @ResponseBody
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
            
            if (!role.equals("ROLE_USER") && !role.equals("ROLE_ADMIN")) {
                return ResponseEntity.badRequest().body("Недопустимая роль");
            }
            
            user.setRole(role);
            userService.saveUser(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
