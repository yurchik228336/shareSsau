package ru.ruscreat.shareSsau.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
public class Post {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Заголовок не может быть пустым")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Содержание не может быть пустым")
    @Size(max = 2500, message = "Содержание не может превышать 2500 символов")  // Ограничение на длину
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private String author;

    @Column(nullable = false)
    private String formattedDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    // В модели Post добавьте это поле:
    @Column
    private String authorAvatar;  // Ссылка на аватарку автора



    // Конструктор по умолчанию
    public Post() {
        this.createdAt = LocalDateTime.now();
        this.formattedDate = formatDateTime(this.createdAt);
        this.author = getCurrentUsername();
    }

    // Конструктор с параметрами
    public Post(String title, String content) {
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.formattedDate = formatDateTime(this.createdAt);
        this.author = getCurrentUsername();

    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "Anonymous";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        this.formattedDate = formatDateTime(createdAt);
    }
    // Геттеры и сеттеры для авторской аватарки
    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

}
