package be.ap.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class GroupsResponseDto {
    private List<GroupDTO> parentGroups;
}