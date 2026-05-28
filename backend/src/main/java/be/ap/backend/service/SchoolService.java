package be.ap.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import be.ap.backend.dto.LocationDTO;
import be.ap.backend.dto.SchoolDTO;
import be.ap.backend.entity.School;
import be.ap.backend.entity.Section;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.exception.MissingArgumentsException;
import be.ap.backend.model.OneRosterCredentials;
import be.ap.backend.repository.SchoolRepository;
import be.ap.backend.repository.SectionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SchoolService {
    private final SchoolRepository schoolRepository;
    private final EncryptionService encryptionService;
    private final SectionRepository sectionRepository;

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
                    "Ontleenlimiet, ontleenperiode en verlengperiode mogen niet groter zijn dan 999!");
        }
        if (dto.getExtendLimit() > 10) {
            throw new ArgumentsInvalidException("Maximaal aantal verlengingen mag niet meer zijn dan 10!");
        }
        if (dto.getOneRosterClientId() == null) {
            throw new MissingArgumentsException("CLient ID is verplicht!");
        }
        if (dto.getOneRosterClientSecret() == null) {
            throw new MissingArgumentsException("CLient Secret is verplicht!");
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

        if (dto.getOneRosterClientId() != null) {
            saved.setOneRosterClientId(encryptionService.encrypt(dto.getOneRosterClientId()));
        }
        if (dto.getOneRosterClientSecret() != null) {
            saved.setOneRosterClientSecret(encryptionService.encrypt(dto.getOneRosterClientSecret()));
        }

        School school = schoolRepository.save(saved);
        createDefaultSections(school.getId());
        return toDTO(school);
    }

    public OneRosterCredentials getCredentials(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new MissingArgumentsException("School niet gevonden"));

        return new OneRosterCredentials(
                encryptionService.decrypt(school.getOneRosterClientId()),
                encryptionService.decrypt(school.getOneRosterClientSecret()));
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

    public SchoolDTO updateSchool(Long id, SchoolDTO dto) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("School niet gevonden met id: " + id));

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
                    "Ontleenlimiet, ontleenperiode en verlengperiode mogen niet groter zijn dan 999!");
        }
        if (dto.getExtendLimit() > 10) {
            throw new ArgumentsInvalidException("Maximaal aantal verlengingen mag niet meer zijn dan 10!");
        }

        school.setName(dto.getName());
        school.setAdres(dto.getAdres());
        school.setContact(dto.getContact());
        school.setDescription(dto.getDescription());
        school.setSsSubdomain(dto.getSsSubdomain());
        school.setBorrowLimit(dto.getBorrowLimit());
        school.setBorrowPeriod(dto.getBorrowPeriod());
        school.setExtendLimit(dto.getExtendLimit());
        school.setExtendPeriod(dto.getExtendPeriod());

        return toDTO(schoolRepository.save(school));
    }

    private void createDefaultSections(Long schoolId) {
        Section spotlightedSection = new Section();
        spotlightedSection.setTitle("In de kijker");
        spotlightedSection.setRanking((byte) 0);
        spotlightedSection.setSchoolId(schoolId);
        spotlightedSection.setHidden(false);
        sectionRepository.save(spotlightedSection);

        Section monthlySection = new Section();
        monthlySection.setTitle("Boek van de maand");
        monthlySection.setRanking((byte) 1);
        monthlySection.setSchoolId(schoolId);
        monthlySection.setHidden(false);
        sectionRepository.save(monthlySection);
    }
}
