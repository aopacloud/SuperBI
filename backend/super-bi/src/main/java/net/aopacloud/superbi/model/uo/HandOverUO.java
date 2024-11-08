package net.aopacloud.superbi.model.uo;

import lombok.Data;
import net.aopacloud.superbi.enums.PositionEnum;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class HandOverUO {

    private Long id;

    private List<Long> idList;

    @NotNull
    private String fromUsername;

    @NotNull
    private List<String> fromUsernameList;

    @NotNull
    private String toUsername;

    private PositionEnum position;

    private Boolean autoTrans = false;

}
