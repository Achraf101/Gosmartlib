package be.ap.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * Response wrapper containing a list of top-level reading groups.
 */
@Data
public class GroupsResponseDTO {
    private List<GroupDTO> parentGroups;
}