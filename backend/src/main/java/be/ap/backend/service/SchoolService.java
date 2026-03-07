package be.ap.backend.service;

import org.springframework.stereotype.Service;

import be.ap.backend.entity.School;
import be.ap.backend.repository.SchoolRepository;

@Service
public class SchoolService {
    private final SchoolRepository schoolRepository;

    public SchoolService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    public School addSchool(School school) {
        return schoolRepository.save(school);
    }
}
