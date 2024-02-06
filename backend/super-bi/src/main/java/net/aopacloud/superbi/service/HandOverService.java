package net.aopacloud.superbi.service;

import net.aopacloud.superbi.enums.PositionEnum;

public interface HandOverService {

    boolean checkUserHasResource(String username);

    void moveUserResource(String fromUser, String toUser, PositionEnum position);

}
