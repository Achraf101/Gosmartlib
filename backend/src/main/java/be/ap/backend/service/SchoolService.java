package be.ap.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import be.ap.backend.dto.CampusDTO;
import be.ap.backend.dto.SchoolDTO;
import be.ap.backend.entity.School;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.exception.MissingArgumentsException;
import be.ap.backend.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SchoolService {
    private final SchoolRepository schoolRepository;

    public SchoolDTO addSchool(SchoolDTO dto) {
        if (dto.getName() == null) {
            throw new MissingArgumentsException("Schoolnaam is verplicht!");
        }
        if (dto.getAdres().length() > 500 || dto.getContact().length() > 500) {
            throw new ArgumentsInvalidException("Adres en contact mogen niet langer zijn dan 500 tekens!");
        }
        if (dto.getDescription().length() > 1000) {
            throw new ArgumentsInvalidException("Beschrijving mag niet langer zijn dan 1000 tekens!");
        }
        if (dto.getSsSubdomain() == null) {
            throw new MissingArgumentsException("Smartschool url is verplicht!");
        }

        School saved = new School();
        saved.setName(dto.getName());
        saved.setAdres(dto.getAdres());
        saved.setContact(dto.getContact());
        saved.setDescription(dto.getDescription());
        saved.setSsSubdomain(dto.getSsSubdomain());

        return toDTO(schoolRepository.save(saved));
    }

    public List<SchoolDTO> getAll() {
        return schoolRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<SchoolDTO> findById(Long id) {
        return schoolRepository.findById(id)
                .map(this::toDTO);
    }

    private SchoolDTO toDTO(School school) {
        SchoolDTO dto = new SchoolDTO();
        dto.setId(school.getId());
        dto.setName(school.getName());
        dto.setAdres(school.getAdres());
        dto.setContact(school.getContact());
        dto.setDescription(school.getDescription());
        dto.setSsSubdomain(school.getSsSubdomain());

        List<CampusDTO> campusDTOs = school.getCampuses().stream()
                .map(campus -> {
                    CampusDTO c = new CampusDTO();
                    c.setId(campus.getId());
                    c.setName(campus.getName());
                    c.setAdres(campus.getAdres());
                    c.setBorrowLimit(campus.getBorrowLimit());
                    c.setSchoolId(school.getId());
                    return c;
                })
                .collect(java.util.stream.Collectors.toList());

        dto.setCampuses(campusDTOs);
        return dto;
    }
}
