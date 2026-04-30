package be.ap.backend.dto;

import lombok.Data;

@Data
public class GroupDTO {
    private String groupID;
    private String name;
    private String description;
    private String platform;
}
