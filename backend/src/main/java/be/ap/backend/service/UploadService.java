package be.ap.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import be.ap.backend.dto.CoverDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final BookRepository bookRepository;

    private final int idLength = 16;

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
        Optional<Book> opt = bookRepository.findById(bookId);

        Book book = opt.orElse(null);
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
}
