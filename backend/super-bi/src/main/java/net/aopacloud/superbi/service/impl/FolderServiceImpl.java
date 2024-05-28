package net.aopacloud.superbi.service.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.enums.FolderEnum;
import net.aopacloud.superbi.enums.FolderTypeEnum;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.mapper.FolderMapper;
import net.aopacloud.superbi.mapper.FolderResourceRelMapper;
import net.aopacloud.superbi.model.converter.FolderConverter;
import net.aopacloud.superbi.model.converter.FolderResourceRelationshipConverter;
import net.aopacloud.superbi.model.dto.FolderDTO;
import net.aopacloud.superbi.model.dto.FolderResourceRelationshipDTO;
import net.aopacloud.superbi.model.dto.FullFolderDTO;
import net.aopacloud.superbi.model.entity.Folder;
import net.aopacloud.superbi.model.entity.FolderResourceRel;
import net.aopacloud.superbi.model.query.FolderQuery;
import net.aopacloud.superbi.model.vo.FolderNode;
import net.aopacloud.superbi.service.FolderResourceCountService;
import net.aopacloud.superbi.service.FolderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: hudong
 * @date: 2023/10/26
 * @description:
 */
@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderMapper folderMapper;

    private final FolderResourceRelMapper folderResourceRelMapper;

    private final FolderConverter folderConverter;

    private final FolderResourceCountService resourceCountService;

    private final FolderResourceRelationshipConverter relationshipConverter;

    @Override
    public FolderDTO save(FolderDTO folderDTO) {
        Folder folder = folderConverter.toEntity(folderDTO);
        folder.setCreator(LoginContextHolder.getUsername());
        if (folder.getType() == FolderTypeEnum.PERSONAL) {
            folder.setCreator(LoginContextHolder.getUsername());
        }
        Folder existsFolder = folderMapper.select(folder);
        if (existsFolder != null) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DUPLICATE_NAME_ERROR));
        }

        folderMapper.save(folder);
        return folderConverter.entityToDTO(folder);
    }

    @Override
    public FolderDTO update(FolderDTO folderDTO) {
        Folder existsFolder = folderMapper.select(folderConverter.toEntity(folderDTO));
        if (existsFolder != null && !existsFolder.getId().equals(folderDTO.getId())) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DUPLICATE_NAME_ERROR));
        }
        folderMapper.update(folderConverter.toEntity(folderDTO));
        return folderDTO;
    }

    @Override
    public FolderDTO delete(Long id) {

        if (id < 0) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.SYSTEM_FOLDER_FORBIDDEN_ERROR));
        }

        Folder folder = folderMapper.selectById(id);

        FolderQuery folderQuery = new FolderQuery();
        folderQuery.setCreator(folder.getCreator());
        folderQuery.setType(folder.getType());
        folderQuery.setPosition(folder.getPosition());
        folderQuery.setWorkspaceId(folder.getWorkspaceId());
        List<Folder> children = firstGenChild(id, folderQuery);
        if (children != null && !children.isEmpty()) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.FOLDER_EXISTS_CHILDREN_ERROR));
        }
        int cnt = folderResourceRelMapper.countByFolder(folder);
        if (cnt > 0) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.FOLDER_EXISTS_RESOURCE_ERROR));
        }
        folderMapper.deleteById(id);
        folderResourceRelMapper.deleteByFolderId(id);
        return folderConverter.entityToDTO(folder);
    }

    @Override
    public FolderNode findFolderTree(FolderQuery folderQuery) {
        return folderTree(folderQuery, true);
    }

    @Override
    public FolderNode findFolderTreeWithoutCount(FolderQuery folderQuery) {
        return folderTree(folderQuery, false);
    }

    @Override
    public FolderResourceRelationshipDTO moveResource(FolderResourceRelationshipDTO relationship) {
        if (relationship.getTargetId() == null || relationship.getTargetId() == 0) {
            return relationship;
        }
        relationship.setCreator(LoginContextHolder.getUsername());
        //1. 若 关系没变动 直接返回
        Folder folder = folderMapper.selectByRelationship(relationship);
        if (folder != null && folder.getId().equals(relationship.getFolderId())) {
            return relationship;
        }

        // 2. 删除之前的关系
        folderResourceRelMapper.deleteByRelationship(relationship);

        // 3. 如果 重新 放入未分组
        if (FolderEnum.UN_CLASSIFIED == FolderEnum.ofFolderId(relationship.getFolderId())) {
            return relationship;
        }

        // 4. 写入新的 relationship
        FolderResourceRel entity = relationshipConverter.toEntity(relationship);
        folderResourceRelMapper.insert(entity);

        return relationshipConverter.entityToDTO(entity);
    }

    @Override
    public FullFolderDTO findByTarget(Long targetId, PositionEnum position) {
        Folder folder = folderMapper.selectByTarget(targetId, position.getName());
        if (Objects.isNull(folder)) {
            FolderNode unClassifiedNode = FolderNode.initUnClassifiedNode();
            FullFolderDTO retFolder = new FullFolderDTO();
            BeanUtils.copyProperties(unClassifiedNode, retFolder);
            retFolder.setAbsolutePath(unClassifiedNode.getName());
            return retFolder;
        }
        LinkedList<Folder> parent = Lists.newLinkedList();
        parent.addFirst(folder);
        findAllParent(folder, parent);
        String absolutePath = Joiner.on("/").join(parent.stream().map(Folder::getName).collect(Collectors.toList()));

        FullFolderDTO fullFolder = new FullFolderDTO();
        BeanUtils.copyProperties(folder, fullFolder);
        fullFolder.setAbsolutePath(absolutePath);

        return fullFolder;
    }

    public FullFolderDTO findPersonalByTarget(Long targetId, PositionEnum position, String username) {
        Folder folder = folderMapper.selectPersonalByTarget(targetId, position.getName(), username);
        if (Objects.isNull(folder)) {
            return null;
        }
        LinkedList<Folder> parent = Lists.newLinkedList();
        parent.addFirst(folder);
        findAllParent(folder, parent);
        String absolutePath = Joiner.on("/").join(parent.stream().map(Folder::getName).collect(Collectors.toList()));

        FullFolderDTO fullFolder = new FullFolderDTO();
        BeanUtils.copyProperties(folder, fullFolder);
        fullFolder.setAbsolutePath(absolutePath);

        return fullFolder;
    }

    private void findAllParent(Folder folder, LinkedList<Folder> folders) {

        if (folder != null && folder.getParentId() != null && folder.getParentId() > 0) {
            Folder parent = folderMapper.selectById(folder.getParentId());
            folders.addFirst(parent);
            findAllParent(parent, folders);
        }

    }


    @Override
    public void addFolderResourceRel(Long folderId, Long targetId, PositionEnum positionEnum) {

        FolderResourceRelationshipDTO relationship = new FolderResourceRelationshipDTO();
        relationship.setFolderId(folderId);
        relationship.setTargetId(targetId);
        relationship.setPosition(positionEnum);
        relationship.setCreator(LoginContextHolder.getUsername());
        relationship.setType(FolderTypeEnum.ALL);

        folderResourceRelMapper.insert(relationshipConverter.toEntity(relationship));
    }

    private List<Folder> firstGenChild(Long id, FolderQuery folderQuery) {
        return folderMapper.selectChildren(id, folderQuery);
    }

    private FolderNode folderTree(FolderQuery folderQuery, boolean withResourceNum) {
        FolderNode rootNode = FolderNode.initRootNode();
        // 全部文件夹
        if (FolderTypeEnum.ALL == folderQuery.getType()) {

            buildTree(rootNode, folderQuery, withResourceNum);

            FolderNode unNode = FolderNode.initUnClassifiedNode();
            unNode.setResourceNum(-1);

            rootNode.getChildren().add(unNode);

        } else {
            // 与我相关

            if (PositionEnum.DASHBOARD == folderQuery.getPosition()) {
                // 共享给我的
                FolderNode sharedNode = FolderNode.initSharedNode();
                buildTree(sharedNode, folderQuery, withResourceNum);
                rootNode.getChildren().add(sharedNode);
            } else {
                // 我有权限的
                FolderNode authorityNode = FolderNode.initAuthorityNode();
                buildTree(authorityNode, folderQuery, withResourceNum);
                rootNode.getChildren().add(authorityNode);
            }

            // 我创建的
            FolderNode createNode = FolderNode.initCreateNode();
            buildTree(createNode, folderQuery, withResourceNum);

            // 我收藏的
            FolderNode collectNode = FolderNode.initFavoriteNode();
            buildTree(collectNode, folderQuery, withResourceNum);
            // 全部
            FolderNode allNode = FolderNode.initALLNode();
            buildTree(allNode, folderQuery, withResourceNum);
            allNode.setResourceNum(-1);


            rootNode.getChildren().add(createNode);
            rootNode.getChildren().add(collectNode);
            rootNode.getChildren().add(allNode);
        }
        return rootNode;
    }

    public FolderNode findTreeByFolder(Long folderId) {

        Folder folder = folderMapper.selectById(folderId);
        FolderNode fistGenChild = FolderNode.from(folder);


        FolderQuery folderQuery = new FolderQuery();
        folderQuery.setCreator(folder.getCreator());
        folderQuery.setType(folder.getType());
        folderQuery.setPosition(folder.getPosition());
        folderQuery.setWorkspaceId(folder.getWorkspaceId());

        buildTree(fistGenChild, folderQuery, false);

        return fistGenChild;
    }


    private void buildTree(FolderNode parent, FolderQuery folderQuery, boolean withResourceNum) {

        List<Folder> children = firstGenChild(parent.getId(), folderQuery);
        if (withResourceNum) {
            parent.setResourceNum(countResourceNum(parent, folderQuery));
        }
        if (children == null || children.isEmpty()) {
            return;
        }

        List<FolderNode> nodes = children.stream().map(FolderNode::from).collect(Collectors.toList());

        parent.setChildren(nodes);

        for (FolderNode node : nodes) {
            buildTree(node, folderQuery, withResourceNum);
            if (parent.getId() > 0 && withResourceNum) {
                parent.setResourceNum(parent.getResourceNum() + node.getResourceNum());
            }
        }
    }

    public Integer countResourceNum(FolderNode node, FolderQuery folderQuery) {
        if (node == null) {
            return 0;
        }
        if (node.getId() > 0) {
            return resourceCountService.countResourceByFolder(node, LoginContextHolder.getUsername());
        }
        if (node.isFavoriteNode()) {
            if (PositionEnum.DASHBOARD == folderQuery.getPosition()) {
                return resourceCountService.countFavoriteDashboardByUser(folderQuery.getCreator(), folderQuery.getWorkspaceId());
            }
            if (PositionEnum.DATASET == folderQuery.getPosition()) {
                return resourceCountService.countFavoriteDatasetByUser(folderQuery.getCreator(), folderQuery.getWorkspaceId());
            }
        }
        if (node.isCreateNode()) {
            if (PositionEnum.DASHBOARD == folderQuery.getPosition()) {
                return resourceCountService.countCreateDashboard(folderQuery.getCreator(), folderQuery.getWorkspaceId());
            }
            if (PositionEnum.DATASET == folderQuery.getPosition()) {
                return resourceCountService.countCreateDataset(folderQuery.getCreator(), folderQuery.getWorkspaceId());
            }
        }

        if (node.isSharedNode()) {
            return resourceCountService.countShareToMeDashboard(folderQuery.getCreator(), folderQuery.getWorkspaceId());
        }

        if (node.isAuthorityNode()) {
            return resourceCountService.countHasPermissionDataset(folderQuery.getCreator(), folderQuery.getWorkspaceId());
        }

        return 0;
    }
}
