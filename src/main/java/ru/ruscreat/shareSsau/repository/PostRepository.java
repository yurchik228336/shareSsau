package ru.ruscreat.shareSsau.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ruscreat.shareSsau.models.Post;

import java.util.List;

@Repository // Пометка для Spring, что это репозиторий
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthor(String author);
}
