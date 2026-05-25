package com.cinema.service.Cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    @Autowired
    private Cloudinary cloudinary;

    // Danh sách định dạng ảnh được phép upload
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/webp",
            "image/gif");

    // Kích thước file tối đa: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * Upload ảnh lên Cloudinary trong một folder cụ thể với validation
     * 
     * @param file   Ảnh cần upload
     * @param folder Folder lưu trữ ảnh trên Cloudinary
     * @return URL của ảnh sau khi upload
     * @throws IOException Nếu có lỗi xảy ra trong quá trình upload
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or not provided");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the limit of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file format. Only JPG, PNG, WEBP and GIF are allowed.");
        }
        // Cấu hình upload
        Map<String, Object> options = ObjectUtils.asMap("folder", folder != null ? folder : "cinema/general",
                "resource_type", "image");
        //
        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        return uploadResult.get("secure_url").toString();
    }

    /**
     * Upload ảnh lên Cloudinary trong folder "cinema/general"
     * 
     * @param file Ảnh cần upload
     * @return URL của ảnh sau khi upload
     * @throws IOException Nếu có lỗi xảy ra trong quá trình upload
     */
    public String uploadImage(MultipartFile file) throws IOException {
        return uploadImage(file, "cinema/general");
    }

    /**
     * Xóa ảnh trên Cloudinary dựa trên URL.
     */
    public void deleteImage(String url) {
        String publicId = extractPublicId(url);
        if (publicId != null) {
            try {
                log.info("Bắt đầu xóa ảnh trên Cloudinary với publicId: {}", publicId);
                Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Kết quả xóa ảnh trên Cloudinary: {}", result);
            } catch (IOException e) {
                log.error("Không thể xóa ảnh trên Cloudinary cho URL {}: {}", url, e.getMessage());
            }
        }
    }

    /**
     * Trích xuất public_id của ảnh từ URL Cloudinary.
     * Hỗ trợ cả url Cloudinary mặc định và CNAME custom domain có chứa "/upload/".
     * Giới hạn: Chỉ trích xuất được nếu URL tuân thủ cấu trúc phân đoạn có
     * "/upload/".
     */
    public String extractPublicId(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        try {
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1)
                return null;

            String subPath = url.substring(uploadIndex + 8); // Bỏ qua "/upload/"

            // Bỏ qua prefix phiên bản (v12345678/) nếu có
            if (subPath.startsWith("v") && subPath.indexOf('/') > 0) {
                subPath = subPath.substring(subPath.indexOf('/') + 1);
            }

            // Loại bỏ phần mở rộng file (.jpg, .png...)
            int dotIndex = subPath.lastIndexOf('.');
            if (dotIndex != -1) {
                subPath = subPath.substring(0, dotIndex);
            }
            return subPath;
        } catch (Exception e) {
            log.error("Lỗi khi phân tích publicId từ URL: {}", url, e);
            return null;
        }
    }
}
