package be.ap.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.dto.CampusDTO;
import be.ap.backend.entity.Campus;
import be.ap.backend.service.CampusService;

@RestController
@RequestMapping("campus")
public class CampusController {
    private final CampusService campusService;

    public CampusController(CampusService campusService) {
        this.campusService = campusService;
    }

    @PostMapping
    public Campus createCampus(@RequestBody CampusDTO dto) {
        Campus savedCampus = campusService.createCampus(dto);
        return savedCampus;
    }
}
