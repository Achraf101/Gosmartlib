package be.ap.backend.service;

import java.util.List;

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

    public CampusDTO createCampus(CampusDTO dto) {
        Campus campus = new Campus();
        campus.setName(dto.getName());
        campus.setAdres(dto.getAdres());
        campus.setBorrowLimit(dto.getBorrowLimit());
        if (dto.getSchoolId() != null) {
            campus.setSchool(entityManager.find(School.class, dto.getSchoolId()));
        }
        return toDTO(campusRepository.save(campus));
    }

    public CampusDTO findById(Long id) {
        Campus campus = campusRepository.findById(id).orElseThrow();
        return toDTO(campus);
    }

    public List<CampusDTO> findAll() {
        return campusRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private CampusDTO toDTO(Campus campus) {
        CampusDTO dto = new CampusDTO();
        dto.setId(campus.getId());
        dto.setName(campus.getName());
        dto.setAdres(campus.getAdres());
        dto.setBorrowLimit(campus.getBorrowLimit());
        if (campus.getSchool() != null) {
            dto.setSchoolId(campus.getSchool().getId());
        }
        return dto;
    }
}
