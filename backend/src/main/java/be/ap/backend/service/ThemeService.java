package be.ap.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.ap.backend.dto.ThemeDTO;
import be.ap.backend.entity.Theme;
import be.ap.backend.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThemeService {
    private final ThemeRepository themeRepository;

    public List<ThemeDTO> getAll() {
        return themeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ThemeDTO convertToDTO(Theme theme) {
        ThemeDTO dto = new ThemeDTO();
        dto.setId(theme.getId());
        dto.setName(theme.getName());
        return dto;
    }

    public Theme add(Theme theme) {
        return themeRepository.save(theme);
    }
}
