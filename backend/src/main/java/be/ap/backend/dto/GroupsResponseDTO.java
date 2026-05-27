package be.ap.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class GroupsResponseDTO {
    private List<GroupDTO> parentGroups;
}