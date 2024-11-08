package net.aopacloud.superbi.model.vo;

import java.util.HashMap;

public class FieldPreviewVO extends HashMap<String, String> {


    public void putField(String tableAlias, String column, String fieldName) {
        this.put(String.format("%s.%s", tableAlias, column), fieldName);
    }

}
