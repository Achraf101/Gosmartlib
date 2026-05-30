package be.ap.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import be.ap.backend.dto.CoverDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Material;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final BookRepository bookRepository;

    private final MaterialRepository materialRepository;

    private static final int idLength = 16;

    @Setter
    @Value("${app.upload-dir}")
    private String uploadDir;

    /**
     * Saves the provided cover and deletes the old one
     * 
     * @param file   the uploaded file
     * @param bookId the id of book to update
     * @return a DTO with the new location of the cover
     */
    public CoverDTO saveCover(MultipartFile file, Long bookId) {
        // first find book and current cover
        Book book = bookRepository.findById(bookId).orElse(null);

        // if a current cover delete file
        if (book == null) {
            return null;
        }
        String currentCover = book.getCover();
        if (currentCover != null && currentCover != "" && uploadDir != null) {
            // delete current cover
            Path path = Paths.get(uploadDir, "cover", currentCover);

            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
            }
        }

        // save the new cover
        String[] fileParts = file.getOriginalFilename().split("\\.");
        String fileExtension = "";
        if (fileParts.length >= 2) {
            fileExtension = fileParts[fileParts.length - 1];
        }
        String fileName;
        if (fileExtension != "" && fileExtension.length() <= 6) {
            fileName = generateId() + "." + fileExtension;
        } else {
            // file without extension
            fileName = generateId();
        }

        // save cover in book table
        int updated = bookRepository.updateCover(bookId, fileName);
        if (updated == 0) {
            // book not found/error dont save and return
            return null;
        }

        // then save file to disk
        Path coverPath = Paths.get(uploadDir, "cover", fileName);
        try {
            file.transferTo(coverPath);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        CoverDTO cover = new CoverDTO(bookId, fileName);
        return cover;
    }

    /**
     * Saves a course item to db and filesystem
     * 
     * @param file
     * @param bookId
     * @return
     */
    public Material saveMaterial(MultipartFile file, Long bookId, String note) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null)
            return null;

        String fileId = generateId();
        Material material = new Material();
        material.setBook(book);
        material.setFileName(file.getOriginalFilename());
        material.setFileId(fileId);
        material.setSize(file.getSize());
        material.setComment(note);

        Material m = materialRepository.save(material);

        Path coverPath = Paths.get(uploadDir, "file", fileId);
        try {
            Files.createDirectories(coverPath.getParent());
            file.transferTo(coverPath);
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        return m;
    }

    SecureRandom random = new SecureRandom();

    public String generateId() {
        byte[] bytes = new byte[idLength]; // 16 bytes (32 length)
        random.nextBytes(bytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public String saveCoverFromUrl(String imageUrl) {
        try {
            URI uri = new URI(imageUrl);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();

            if (connection.getResponseCode() != 200) {
                return null;
            }

            String contentType = connection.getContentType();
            String extension = getExtensionFromContentType(contentType);
            if (extension == null) {
                String path = url.getPath();
                int dot = path.lastIndexOf('.');
                if (dot >= 0 && path.length() - dot <= 6) {
                    extension = path.substring(dot + 1);
                }
            }

            String fileName = extension != null
                    ? generateId() + "." + extension
                    : generateId();

            Path coverPath = Paths.get(uploadDir, "cover", fileName);
            Files.createDirectories(coverPath.getParent());

            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, coverPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return fileName;

        } catch (URISyntaxException | IOException e) {
            return null;
        }
    }

    private String getExtensionFromContentType(String contentType) {
        if (contentType == null)
            return null;
        return switch (contentType.split(";")[0].trim().toLowerCase()) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> null;
        };
    }
}
