package net.aopacloud.superbi.model.uo;

import lombok.Data;

import java.util.List;

@Data
public class WorkspaceMemberAddUO {

    private Long workspaceId;

    private List<String> usernames;

}
