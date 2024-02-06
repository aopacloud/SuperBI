package net.aopacloud.superbi.listener.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.aopacloud.superbi.model.entity.AuthRoleUser;

import java.util.List;

@Data
@AllArgsConstructor
public class AuthRoleUserUpdateEvent {

    private Long roleId;

    private List<AuthRoleUser> modifiedUsers;

}
