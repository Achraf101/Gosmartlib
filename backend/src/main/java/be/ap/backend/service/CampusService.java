package be.ap.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import be.ap.backend.dto.CampusDTO;
import be.ap.backend.entity.Campus;
import be.ap.backend.entity.School;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.exception.MissingArgumentsException;
import be.ap.backend.repository.CampusRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CampusService {
    private final CampusRepository campusRepository;

    private final EntityManager entityManager;

    public CampusDTO createCampus(CampusDTO dto) {
        if (dto.getSchoolId() == null) {
            throw new MissingArgumentsException("School is verplicht!");
        }
        if (dto.getBorrowLimit() <= 0 || dto.getBorrowPeriod() <= 0 || dto.getExtendLimit() <= 0
                || dto.getExtendPeriod() <= 0) {
            throw new ArgumentsInvalidException("Getallen moeten minimaal 1 zijn!");
        }
        if (dto.getBorrowLimit() > 999 || dto.getBorrowPeriod() > 999 || dto.getExtendPeriod() > 999) {
            throw new ArgumentsInvalidException(
                    "Uitleenlimiet, uitleenperiode en verlengperiode mogen niet groter zijn dan 999!");
        }
        if (dto.getExtendLimit() > 10) {
            throw new ArgumentsInvalidException("Maximaal aantal verlengingen mag niet meer zijn dan 10!");
        }

        Campus campus = new Campus();
        campus.setName(dto.getName());
        campus.setAdres(dto.getAdres());
        campus.setBorrowLimit(dto.getBorrowLimit());
        campus.setBorrowPeriod(dto.getBorrowPeriod());
        campus.setExtendLimit(dto.getExtendLimit());
        campus.setExtendPeriod(dto.getExtendPeriod());
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
        dto.setBorrowPeriod(campus.getBorrowPeriod());
        dto.setExtendLimit(campus.getExtendLimit());
        dto.setExtendPeriod(campus.getExtendPeriod());
        if (campus.getSchool() != null) {
            dto.setSchoolId(campus.getSchool().getId());
        }
        return dto;
    }
}
