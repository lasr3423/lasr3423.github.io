package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.dto.response.FileUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    private final Path uploadRootPath;
    private final String baseUrl;

    public FileStorageService(
            @Value("${file.upload-dir:uploads}") String uploadDir,
            @Value("${app.base-url:http://localhost:8202}") String baseUrl
    ) {
        this.uploadRootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
    }

    public FileUploadResponse storeProductThumbnail(MultipartFile file) {
        validateImage(file);

        try {
            Path productDir = uploadRootPath.resolve("products");
            Files.createDirectories(productDir);

            String extension = extractExtension(file.getOriginalFilename());
            String storedFilename = UUID.randomUUID() + "." + extension;
            Path targetPath = productDir.resolve(storedFilename).normalize();

            if (!targetPath.startsWith(productDir)) {
                throw new IllegalArgumentException("잘못된 파일 경로입니다.");
            }

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String storedPath = "/uploads/products/" + storedFilename;
            String accessUrl = URI.create(baseUrl).resolve(storedPath).toString();

            return new FileUploadResponse(
                    storedPath,
                    accessUrl,
                    file.getOriginalFilename(),
                    file.getSize()
            );
        } catch (IOException e) {
            throw new UncheckedIOException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 이미지 파일이 없습니다.");
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("jpg, jpeg, png, gif, webp 형식만 업로드할 수 있습니다.");
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("파일 확장자를 확인할 수 없습니다.");
        }

        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
