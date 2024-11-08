package net.aopacloud.superbi.service;

import net.aopacloud.superbi.enums.PositionEnum;

import java.util.List;

public interface HandOverService {

    boolean checkUserHasResource(String username);

    void moveUserResource(String fromUser, String toUser, PositionEnum position);

    void moveUserResourceById(Long id, String fromUser, String toUser, PositionEnum position, Boolean autoTrans);

    void moveUserResourceByIdList(List<Long> idList, List<String> fromUserList, String toUser, PositionEnum position, Boolean autoTrans);

}
