package be.ap.backend.dto;

import lombok.Data;

@Data
public class SchoolDTO {
    private Long id;
    private String name;
    private String adres;
    private String contact;
    private String description;
}