package be.ap.backend.service;

import java.util.List;
import java.util.Optional;

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

    public List<School> getAll() {
        return schoolRepository.findAll();
    }

    public Optional<School> findById(Long id) {
        return schoolRepository.findById(id);
    }

}
