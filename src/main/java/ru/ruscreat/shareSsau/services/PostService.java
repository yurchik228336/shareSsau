package ru.ruscreat.shareSsau.services;

import org.springframework.stereotype.Service;
import ru.ruscreat.shareSsau.models.Post;
import ru.ruscreat.shareSsau.repository.PostRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
    public List<Post> getPostsByAuthor(String author) {
        return postRepository.findByAuthor(author);
    }
}
