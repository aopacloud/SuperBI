package net.aopacloud.superbi.enums;

/**
 * @Author shinnie
 * @Description
 * @Date 17:22 2023/9/19
 */
public enum FolderEnum {

    ROOT_FOLDER(-1L, "根目录"),

    UN_CLASSIFIED(-7L, "未分组"),
    SHARED(-2L, "共享给我的"),
    CREATE(-3L, "我创建的"),
    FAVORITE(-4L, "我收藏的"),
    AUTHORIZED(-5L, "我有权限的"),
    ALL(-6L, "全部"),

    NORMAL(0L, "普通文件夹");


    private Long folderId;

    private String folderName;

    FolderEnum(Long folderId, String folderName) {
        this.folderId = folderId;
        this.folderName = folderName;
    }

    public Long getFolderId() {
        return folderId;
    }

    public String getFolderName() {
        return folderName;
    }

    public static FolderEnum ofFolderId(Long folderId) {
        for (FolderEnum folderEnum : FolderEnum.values()) {
            if (folderEnum.getFolderId().equals(folderId)) {
                return folderEnum;
            }
        }
        return NORMAL;
    }
}
