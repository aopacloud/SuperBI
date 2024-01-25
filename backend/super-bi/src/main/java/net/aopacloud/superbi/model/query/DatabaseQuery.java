package net.aopacloud.superbi.model.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author: hudong
 * @date: 2023/10/19
 * @description:
 */
@Data
public class DatabaseQuery {

    @NotNull
    private Long workspaceId;

}
