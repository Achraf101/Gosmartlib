package be.ap.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import be.ap.backend.entity.Material;
import be.ap.backend.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;

    public List<Material> getByBookId(Long bookId) {
        return materialRepository.findByBook_IdOrderByUploadedDesc(bookId);
    }
}