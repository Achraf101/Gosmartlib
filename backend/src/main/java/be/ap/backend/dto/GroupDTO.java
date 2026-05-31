package be.ap.backend.dto;

import lombok.Data;

/**
 * DTO representing an external reading group or community.
 */
@Data
public class GroupDTO {
    private String groupID;
    private String name;
    private String description;
    private String platform;
}
