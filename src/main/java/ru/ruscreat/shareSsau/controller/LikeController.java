package ru.ruscreat.shareSsau.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.ruscreat.shareSsau.models.Like;
import ru.ruscreat.shareSsau.models.Post;
import ru.ruscreat.shareSsau.models.User;
import ru.ruscreat.shareSsau.repository.LikeRepository;
import ru.ruscreat.shareSsau.repository.PostRepository;
import ru.ruscreat.shareSsau.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/toggle/{postId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long postId, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username).orElse(null);
        Post post = postRepository.findById(postId).orElse(null);

        if (user == null || post == null) {
            return ResponseEntity.badRequest().build();
        }

        boolean liked = false;
        try {
            if (likeRepository.existsByPostAndUser(post, user)) {
                likeRepository.deleteByPostAndUser(post, user);
            } else {
                Like newLike = new Like(post, user);
                likeRepository.save(newLike);
                liked = true;
            }
        } catch (Exception e) {
            // In case of any database errors (like constraint violations)
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("likeCount", likeRepository.countByPost(post));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{postId}")
    public ResponseEntity<List<String>> getLikeUsers(@PathVariable Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return ResponseEntity.badRequest().build();
        }

        List<String> usernames = likeRepository.findByPost(post)
                .stream()
                .map(like -> like.getUser().getUsername())
                .collect(Collectors.toList());

        return ResponseEntity.ok(usernames);
    }

    @GetMapping("/status/{postId}")
    public ResponseEntity<Map<String, Object>> getLikeStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return ResponseEntity.badRequest().build();
        }

        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("liked", likeRepository.existsByPostAndUser(post, user));
        response.put("likeCount", likeRepository.countByPost(post));
        
        return ResponseEntity.ok(response);
    }
}
