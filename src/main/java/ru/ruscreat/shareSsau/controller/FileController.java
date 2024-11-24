package ru.ruscreat.shareSsau.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
public class FileController {

    // Метод для получения баннеров
    @GetMapping("/uploads/banners/{fileName}")
    public ResponseEntity<FileSystemResource> getBanner(@PathVariable String fileName) {
        FileSystemResource resource = new FileSystemResource("uploads/banners/" + fileName);
        if (resource.exists()) {
            return ResponseEntity.ok().body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Метод для получения аватарок
    @GetMapping("/uploads/avatars/{fileName}")
    public ResponseEntity<FileSystemResource> getAvatar(@PathVariable String fileName) {
        FileSystemResource resource = new FileSystemResource("uploads/avatars/" + fileName);
        if (resource.exists()) {
            return ResponseEntity.ok().body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
