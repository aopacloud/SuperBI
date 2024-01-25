package net.aopacloud.superbi.model.query;

import lombok.Data;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/8/9
 * @description:
 */
@Data
public class WorkspaceMemberQuery extends BaseQuery{

    private List<String> usernames;

}
