package net.aopacloud.superbi.model.vo;

import net.aopacloud.superbi.enums.FolderEnum;
import net.aopacloud.superbi.model.entity.Folder;
import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: hudong
 * @date: 2022/6/7
 * @description:
 */
@Data
public class FolderNode extends Folder {

    private List<FolderNode> children = Lists.newArrayList();

    private Integer resourceNum = 0;

    private List resourceList = new ArrayList<>();

    public static FolderNode from(Folder folder) throws BeansException {
        FolderNode node = new FolderNode();
        BeanUtils.copyProperties(folder, node);
        return node;
    }


    public Folder toEntity() {
        Folder folder = new Folder();
        BeanUtils.copyProperties(this, folder);
        return folder;
    }

    public static List<Long> allNodeId(FolderNode node) {
        List list = new ArrayList();
        list.add(node.getId());
        for (FolderNode child : node.getChildren()) {
            list.addAll(allNodeId(child));
        }
        return list;
    }

    public static FolderNode initRootNode() {
        return initNode(FolderEnum.ROOT_FOLDER);
    }

    public static FolderNode initSharedNode() {
        return initNode(FolderEnum.SHARED);
    }


    public static FolderNode initAuthorityNode() {
        return initNode(FolderEnum.AUTHORIZED);
    }
    public static FolderNode initCreateNode() {
        return initNode(FolderEnum.CREATE);
    }

    public static FolderNode initFavoriteNode() {
        return initNode(FolderEnum.FAVORITE);
    }

    public static FolderNode initALLNode(){
        return initNode(FolderEnum.ALL);
    }

    public static FolderNode initUnClassifiedNode() {
        return initNode(FolderEnum.UN_CLASSIFIED);
    }
    private static FolderNode initNode(FolderEnum folderEnum) {
        FolderNode rootNode = new FolderNode();
        rootNode.setId(folderEnum.getFolderId());
        rootNode.setName(folderEnum.getFolderName());
        return rootNode;
    }

    public boolean isFavoriteNode() {
        return this.getId().equals(FolderEnum.FAVORITE.getFolderId());
    }

    public boolean isSharedNode() {
        return this.getId().equals(FolderEnum.SHARED.getFolderId());
    }

    public boolean isAuthorityNode() {
        return this.getId().equals(FolderEnum.AUTHORIZED.getFolderId());
    }

    public boolean isCreateNode() {
        return this.getId().equals(FolderEnum.CREATE.getFolderId());
    }

}
