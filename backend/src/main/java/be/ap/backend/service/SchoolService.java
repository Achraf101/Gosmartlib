package be.ap.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import be.ap.backend.dto.LocationDTO;
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

        School saved = new School();
        saved.setName(dto.getName());
        saved.setAdres(dto.getAdres());
        saved.setContact(dto.getContact());
        saved.setDescription(dto.getDescription());
        saved.setSsSubdomain(dto.getSsSubdomain());
        saved.setBorrowLimit(dto.getBorrowLimit());
        saved.setBorrowPeriod(dto.getBorrowPeriod());
        saved.setExtendLimit(dto.getExtendLimit());
        saved.setExtendPeriod(dto.getExtendPeriod());

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
        dto.setBorrowLimit(school.getBorrowLimit());
        dto.setBorrowPeriod(school.getBorrowPeriod());
        dto.setExtendLimit(school.getExtendLimit());
        dto.setExtendPeriod(school.getExtendPeriod());

        List<LocationDTO> locationDTOs = school.getLocations().stream()
                .map(location -> {
                    LocationDTO c = new LocationDTO();
                    c.setId(location.getId());
                    c.setName(location.getName());
                    c.setAdres(location.getAdres());
                    c.setSchoolId(school.getId());
                    return c;
                })
                .collect(java.util.stream.Collectors.toList());

        dto.setLocations(locationDTOs);
        return dto;
    }
}
