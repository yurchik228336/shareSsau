package ru.ruscreat.shareSsau.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ruscreat.shareSsau.models.Like;
import ru.ruscreat.shareSsau.models.Post;
import ru.ruscreat.shareSsau.models.User;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostAndUser(Post post, User user);
    boolean existsByPostAndUser(Post post, User user);
    List<Like> findByPost(Post post);
    long countByPost(Post post);
    void deleteByPostAndUser(Post post, User user);
}
