package net.aopacloud.superbi.auth;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.exception.auth.NoWorkspacePermissionException;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.model.dto.WorkspaceDTO;
import net.aopacloud.superbi.service.WorkspaceService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class WorkspaceChecker {

    private final WorkspaceService workspaceService;

    public void check(Long workspaceId, String username) {

        if (Objects.isNull(workspaceId)) {
            throw new NoWorkspacePermissionException(LocaleMessages.getMessage(MessageConsist.WORKSPACE_FORBIDDEN_ERROR));
        }

        List<WorkspaceDTO> workspaces = workspaceService.listBelongMe();

        boolean result = workspaces.stream().map(WorkspaceDTO::getId).anyMatch(id -> id.equals(workspaceId));

        if (!result) {
            throw new NoWorkspacePermissionException(LocaleMessages.getMessage(MessageConsist.WORKSPACE_FORBIDDEN_ERROR));
        }
    }

}
