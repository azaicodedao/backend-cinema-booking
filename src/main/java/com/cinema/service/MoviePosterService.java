package com.cinema.service;

import com.cinema.service.Cloudinary.CloudinaryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MoviePosterService {

    CloudinaryService cloudinaryService;

    /**
     * Upload ảnh poster, tham số file ảnh poster, trả về URL ảnh poster
     */
    public String uploadPoster(MultipartFile file) throws IOException {
        return cloudinaryService.uploadImage(file);
    }

    /**
     * Xóa ảnh poster sau khi commit
     * 
     * @param oldPosterUrl URL ảnh poster cần xóa
     */
    public void cleanupOldPosterAfterCommit(String oldPosterUrl) {
        if (!isCloudinaryUploadUrl(oldPosterUrl)) {
            return;
        }

        // Đăng ký synchronization để xóa ảnh poster sau khi commit
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deleteAsync(oldPosterUrl);
                }
            });
        } else {
            deleteAsync(oldPosterUrl);
        }
    }

    /**
     * Xóa ảnh poster
     * 
     * @param posterUrl URL ảnh poster cần xóa
     */
    private void deleteAsync(String posterUrl) {
        CompletableFuture.runAsync(() -> cloudinaryService.deleteImage(posterUrl));
    }

    /**
     * Kiểm tra xem URL ảnh poster có phải là URL của Cloudinary không
     * 
     * @param posterUrl URL ảnh poster cần kiểm tra
     * @return true nếu URL ảnh poster là URL của Cloudinary, ngược lại false
     */
    private boolean isCloudinaryUploadUrl(String posterUrl) {
        return posterUrl != null
                && posterUrl.contains("/upload/")
                && (posterUrl.contains("res.cloudinary.com") || posterUrl.contains("cloudinary"));
    }
}
