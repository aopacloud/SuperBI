package net.aopacloud.superbi.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ApplyMessageDTO {

    private int applyingCount;

    private int reviewCount;

    private int operationCount;

}
