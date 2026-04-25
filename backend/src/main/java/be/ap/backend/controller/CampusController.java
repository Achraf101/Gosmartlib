package be.ap.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.dto.CampusDTO;
import be.ap.backend.service.CampusService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("campus")
@RequiredArgsConstructor
public class CampusController {
    private final CampusService campusService;

    @PostMapping
    public CampusDTO createCampus(@RequestBody CampusDTO dto) {
        return campusService.createCampus(dto);
    }

    @GetMapping
    public List<CampusDTO> getAll() {
        return campusService.findAll();
    }

    @GetMapping("/{id}")
    public CampusDTO getById(@PathVariable Long id) {
        return campusService.findById(id);
    }
}