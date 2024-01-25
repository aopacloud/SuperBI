package net.aopacloud.superbi.enums;

public enum ApplyStatusEnum {

    /**
     * init status
     */
    INIT("init", 1),

    /**
     * approving status
     */
    UNDER_REVIEW("underReview", 2),

    /**
     * passed status
     */
    PASSED("passed", 3),

    /**
     * rejected status, reviewer rejected
     */
    REJECTED("rejected", 4),

    /**
     * delete status, applicant deleted
     */
    DELETE("delete", 5);

    private String name;
    private int code;

    ApplyStatusEnum(String name , int code){
        this.name = name ;
        this.code = code;
    }

    public boolean isFinished(){
        return this == PASSED || this == REJECTED || this == DELETE;
    }

    public static ApplyStatusEnum parse(String name){

        for(ApplyStatusEnum status : ApplyStatusEnum.values()){
            if(status.name.equalsIgnoreCase(name)){
                return status;
            }
        }
        return null;
    }
}
