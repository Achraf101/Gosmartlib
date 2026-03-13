package be.ap.backend.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import be.ap.backend.dto.CampusDTO;
import be.ap.backend.entity.Campus;
import be.ap.backend.entity.School;
import be.ap.backend.repository.CampusRepository;
import jakarta.persistence.EntityManager;

@Service
public class CampusService {
    @Autowired
    private CampusRepository campusRepository;

    @Autowired
    private EntityManager entityManager;

    public Campus createCampus(CampusDTO dto) {
        Campus campus = new Campus();

        campus.setName(dto.getName());
        campus.setAdres(dto.getAdres());
        campus.setBorrowLimit(dto.getBorrowLimit());

        if (dto.getSchoolId() != null) {
            campus.setSchool(entityManager.find(School.class, dto.getSchoolId()));
        }

        return campusRepository.save(campus);
    }

    public Optional<Campus> findById(Long id) {
        return campusRepository.findById(id);
    }

}
