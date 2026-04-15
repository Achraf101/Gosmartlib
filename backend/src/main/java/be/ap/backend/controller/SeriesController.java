package be.ap.backend.controller;

import be.ap.backend.dto.SeriesDTO;
import be.ap.backend.service.SeriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;

    @GetMapping
    public List<SeriesDTO> getAll() {
        return seriesService.findAll();
    }

    @GetMapping("/search/{name}")
    public List<SeriesDTO> search(@PathVariable String name) {
        return seriesService.searchByName(name);
    }

    @PostMapping
    public ResponseEntity<SeriesDTO> create(@RequestBody SeriesDTO dto) {
        return ResponseEntity.ok(seriesService.createSeries(dto));
    }
}