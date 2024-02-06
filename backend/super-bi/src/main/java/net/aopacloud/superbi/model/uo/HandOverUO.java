package net.aopacloud.superbi.model.uo;

import lombok.Data;
import net.aopacloud.superbi.enums.PositionEnum;

import javax.validation.constraints.NotNull;

@Data
public class HandOverUO {

    @NotNull
    private String fromUsername;

    @NotNull
    private String toUsername;

    private PositionEnum position;

}
