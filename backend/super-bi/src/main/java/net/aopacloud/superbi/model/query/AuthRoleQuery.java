package net.aopacloud.superbi.model.query;

import lombok.Data;

import java.util.List;

@Data
public class AuthRoleQuery extends BaseQuery{

    private List<String> usernames;

}
