package be.ap.backend.controller;

import org.springframework.web.bind.annotation.*;
import be.ap.backend.entity.School;
import be.ap.backend.service.SchoolService;

@RestController
@RequestMapping("school")
public class SchoolController {
    private final SchoolService schoolService;

    public SchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @PostMapping
    public School addSchool(@RequestBody School school) {
        return schoolService.addSchool(school);
    }
}
