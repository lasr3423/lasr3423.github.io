package com.bookstore.shop.readme.controller.admin;

import com.bookstore.shop.readme.dto.response.FileUploadResponse;
import com.bookstore.shop.readme.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/uploads")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class AdminUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/products")
    public ResponseEntity<FileUploadResponse> uploadProductThumbnail(
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(fileStorageService.storeProductThumbnail(file));
    }
}
