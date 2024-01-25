package net.aopacloud.superbi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConnectResultDTO {

    private boolean pass;

    private String message;

    public static ConnectResultDTO fail(String message) {
       return new ConnectResultDTO(Boolean.FALSE, message);
    }

    public static ConnectResultDTO fail() {
        return new ConnectResultDTO(Boolean.FALSE, null);
    }

    public static ConnectResultDTO success() {
        return new ConnectResultDTO(Boolean.TRUE, null);
    }
}
