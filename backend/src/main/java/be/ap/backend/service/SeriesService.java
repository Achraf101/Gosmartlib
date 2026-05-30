package be.ap.backend.service;

import be.ap.backend.dto.SeriesDTO;
import be.ap.backend.entity.Series;
import be.ap.backend.repository.SeriesRepository;
import jakarta.persistence.EntityNotFoundException;
import be.ap.backend.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final AuthorRepository authorRepository;

    public List<SeriesDTO> findAll() {
        return seriesRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SeriesDTO> searchByName(String name) {
        return seriesRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public SeriesDTO findById(Long id) {
    return seriesRepository.findById(id)
            .map(this::convertToDTO)
            .orElseThrow(() -> new EntityNotFoundException("Series niet gevonden met id: " + id));
}

    public SeriesDTO createSeries(SeriesDTO dto) {
        Series series = new Series();
        series.setName(dto.getName());
        series.setDescription(dto.getDescription());
        
        if (dto.getAuthorId() != null) {
            authorRepository.findById(dto.getAuthorId()).ifPresent(series::setAuthor);
        }
        
        Series saved = seriesRepository.save(series);
        return convertToDTO(saved);
    }

    private SeriesDTO convertToDTO(Series series) {
        SeriesDTO dto = new SeriesDTO();
        dto.setId(series.getId());
        dto.setName(series.getName());
        dto.setDescription(series.getDescription());
        if (series.getAuthor() != null) {
            dto.setAuthorId(series.getAuthor().getId());
            dto.setAuthorName(series.getAuthor().getName());
        }
        return dto;
    }
}