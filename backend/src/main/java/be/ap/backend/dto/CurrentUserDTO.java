package be.ap.backend.dto;

import lombok.Data;

@Data
public class CurrentUserDTO {
    private Long userId;
    private String role;
    private Long schoolId;
    private Long locationId;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
}