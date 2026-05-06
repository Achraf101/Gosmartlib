package be.ap.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.entity.Material;
import be.ap.backend.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("material")
@RequiredArgsConstructor
public class MaterialController {

    @Autowired
    private MaterialRepository materialRepository;

    @GetMapping("{bookId}")
    public List<Material> getMaterial(@PathVariable Long bookId) {
        return materialRepository.findByBook_IdOrderByUploadedDesc(bookId);
    }
}
