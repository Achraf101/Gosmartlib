package be.ap.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.dto.ThemeDTO;
import be.ap.backend.service.ThemeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("theme")
@RequiredArgsConstructor
public class ThemeController {
    private final ThemeService themeService;

    @GetMapping()
    public List<ThemeDTO> getAll() {
        return themeService.getAll();
    }
}
