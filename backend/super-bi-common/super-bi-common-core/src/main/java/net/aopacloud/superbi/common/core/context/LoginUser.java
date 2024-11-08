package net.aopacloud.superbi.common.core.context;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginUser {

    private String token;

    private String username;

}
