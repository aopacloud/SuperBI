package net.aopacloud.superbi.service.impl;

import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.exception.ObjectNotFoundException;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.common.core.utils.PageUtils;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.enums.StatusEnum;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.mapper.*;
import net.aopacloud.superbi.model.converter.DatasetConverter;
import net.aopacloud.superbi.model.converter.ReportConverter;
import net.aopacloud.superbi.model.dto.DashboardDTO;
import net.aopacloud.superbi.model.dto.DatasetAuthorizeDTO;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.ReportDTO;
import net.aopacloud.superbi.model.entity.Dataset;
import net.aopacloud.superbi.model.entity.Report;
import net.aopacloud.superbi.model.query.RecycleQuery;
import net.aopacloud.superbi.model.query.ReportQuery;
import net.aopacloud.superbi.model.vo.RecycleVO;
import net.aopacloud.superbi.model.vo.ReportVO;
import net.aopacloud.superbi.service.*;
import org.apache.commons.compress.utils.Lists;
import org.assertj.core.util.Sets;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author shinnie
 * @Description
 * @Date 10:55 2023/8/29
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    private final ReportFieldMapper reportFieldMapper;

    private final DashboardMapper dashboardMapper;

    private final DatasetService datasetService;

    private final DatasetMapper datasetMapper;

    private final ReportConverter reportConverter;

    private final DatasetConverter datasetConverter;

    private final WorkspaceUserResourceService workspaceUserResourceService;

    private final DatasetAuthorizeService datasetAuthorizeService;

    private final ReportPrivilegeService reportPrivilegeService;

    private final SysUserService sysUserService;

    private final ResourceMapper resourceMapper;

    @Override
    public ReportDTO findOne(Long id) {
        Report report = Optional.ofNullable(reportMapper.selectById(id)).orElseThrow(() -> new ObjectNotFoundException("report not found"));
        ReportDTO reportDTO = reportConverter.entityToDTO(report);
        DatasetDTO datasetDTO = datasetService.findOne(reportDTO.getDatasetId());
        reportDTO.setDataset(datasetDTO);
        reportPrivilegeService.fillPrivilege(reportDTO, LoginContextHolder.getUsername());

        return reportDTO;
    }

    @Override
    public ReportDTO selectByIdRecycle(Long id) {
        Report report = Optional.ofNullable(reportMapper.selectByIdRecycle(id)).orElseThrow(() -> new ObjectNotFoundException("report not found"));
        ReportDTO reportDTO = reportConverter.entityToDTO(report);
        Dataset dataset = datasetMapper.selectByIdRecycle(reportDTO.getDatasetId());
        reportDTO.setDataset(datasetConverter.entityToDTO(dataset));
        return reportDTO;
    }

    @Override
    public List<DashboardDTO> findDashboard(Long id) {
        return dashboardMapper.selectByReport(id);
    }

    //    @Override
//    public List<ReportDTO> search(ReportQuery query) {
//        Set<Long> datasetIds = Sets.newHashSet();
//
//        String currentUser = LoginContextHolder.getUsername();
//        boolean isSuperAdmin = sysUserService.isSuperAdmin(currentUser);
//        Set<String> resourceCodes = workspaceUserResourceService.getResourceCodes(query.getWorkspaceId());
//        if (isSuperAdmin || resourceCodes.contains(BiConsist.REPORT_ALL_WORKSPACE_CODE)) {
//            List<Long> ids = datasetService.selectIdsByWorkspaceAndCreator(query.getWorkspaceId(), null);
//            datasetIds.addAll(ids);
//        } else {
//            List<DatasetAuthorizeDTO> activedAuthorize = datasetAuthorizeService.findActivedAuthorize(currentUser, query.getWorkspaceId());
//            List<Long> ids = activedAuthorize.stream().map(DatasetAuthorizeDTO::getDatasetId).collect(Collectors.toList());
//            datasetIds.addAll(ids);
//        }
//        List<Long> ids = datasetService.selectIdsByWorkspaceAndCreator(query.getWorkspaceId(), currentUser);
//        datasetIds.addAll(ids);
//
//        if (!datasetIds.isEmpty()) {
//            query.setDatasetIds(datasetIds);
//        }
//
//        List<ReportDTO> reportDTOS = reportMapper.search(query);
//
//        reportPrivilegeService.batchFillPrivilege(reportDTOS, currentUser);
//
//        reportDTOS.stream().forEach(report -> report.setQueryParam(null));
//        return reportDTOS.stream().filter(report -> report.getPermission() != PermissionEnum.NONE).collect(Collectors.toList());
//
//    }
    public PageVO<ReportVO> search(ReportQuery query) {
        Set<Long> datasetIds = Sets.newHashSet();

        String currentUser = LoginContextHolder.getUsername();
        boolean isSuperAdmin = sysUserService.isSuperAdmin(currentUser);
        Set<String> resourceCodes = workspaceUserResourceService.getResourceCodes(query.getWorkspaceId());
        if (isSuperAdmin || resourceCodes.contains(BiConsist.REPORT_ALL_WORKSPACE_CODE)) {
            List<Long> ids = datasetService.selectIdsByWorkspaceAndCreator(query.getWorkspaceId(), null);
            datasetIds.addAll(ids);
        } else {
            List<DatasetAuthorizeDTO> activedAuthorize = datasetAuthorizeService.findActivedAuthorize(currentUser, query.getWorkspaceId());
            List<Long> ids = activedAuthorize.stream().map(DatasetAuthorizeDTO::getDatasetId).collect(Collectors.toList());
            datasetIds.addAll(ids);
        }
        List<Long> ids = datasetService.selectIdsByWorkspaceAndCreator(query.getWorkspaceId(), currentUser);
        datasetIds.addAll(ids);

        if (query.getHasPermission() == 1 && datasetIds.isEmpty()) {
            return new PageVO<>(Lists.newArrayList(), 0);
        }
        query.setDatasetIds(datasetIds);

        List<ReportDTO> reportDTOS = reportMapper.search(query);

        int start = Math.min((query.getPageNum() - 1) * query.getPageSize(), reportDTOS.size());
        int end = Math.min(start + query.getPageSize(), reportDTOS.size());

        List<ReportDTO> result = reportDTOS.subList(start, end);

        reportPrivilegeService.batchFillPrivilege(result, currentUser);

        result.stream().forEach(report -> report.setQueryParam(null));

        return new PageVO(reportConverter.toVOList(result), reportDTOS.size());
    }

    public PageVO<RecycleVO> searchByRecycle(RecycleQuery recycleQuery) {
        PageUtils.startPage();
        List<RecycleVO> recycleVOS = reportMapper.searchByRecycle(recycleQuery);
        recycleVOS.forEach(recycleVO -> {
            recycleVO.setCreatorAlias(sysUserService.getUserAliasName(recycleVO.getCreator()));
            recycleVO.setPosition(recycleQuery.getPosition());
        });
        PageInfo<RecycleVO> pageInfo = new PageInfo<>(recycleVOS);
        return new PageVO<>(recycleVOS, pageInfo.getTotal());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public ReportDTO saveOrUpdate(ReportDTO reportDTO) {

        return doSaveOrUpdate(reportDTO);
    }

    private ReportDTO doSaveOrUpdate(ReportDTO reportDTO) {
        try {
            if (Objects.isNull(reportDTO.getId())) {
                reportDTO.setCreator(LoginContextHolder.getUsername());
                reportDTO.setOperator(LoginContextHolder.getUsername());
                Report report = reportConverter.toEntity(reportDTO);
                reportMapper.insert(report);
                reportDTO.setId(report.getId());
            } else {
                // 更新
                reportDTO.setOperator(LoginContextHolder.getUsername());
                reportMapper.update(reportConverter.toEntity(reportDTO));
                reportFieldMapper.deleteByReport(reportDTO.getId());
            }
            return reportDTO;
        } catch (DuplicateKeyException e) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DUPLICATE_NAME_ERROR));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportDTO saveAs(ReportDTO reportDTO) {

        reportDTO.setId(null);
        return doSaveOrUpdate(reportDTO);
    }

    public void deleteById(Long id) {
        reportMapper.updateOperatorById(id, LoginContextHolder.getUsername());
        reportMapper.deleteById(id);
        resourceMapper.delete(id, PositionEnum.REPORT);
    }

    public void recycleDelete(Long id) {
        reportMapper.recycleDelete(id);
    }

    public void restore(Long id) {
        reportMapper.restore(id);
    }

    @Override
    public Integer countByUsername(String username) {
        return reportMapper.countByUsername(username);
    }

    @Override
    public Boolean isActive(Long id) {

        Report report = reportMapper.selectById(id);
        if (Objects.isNull(report)) {
            return Boolean.FALSE;
        }
        try {
            DatasetDTO datasetDTO = datasetService.findOneWithoutFields(report.getDatasetId());
            if (Objects.isNull(datasetDTO) || datasetDTO.getStatus() != StatusEnum.ONLINE) {
                return Boolean.FALSE;
            }
        }catch (Exception e) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public void handOver(String fromUsername, String toUsername) {
        reportMapper.updateCreator(fromUsername, toUsername);
    }

    @Override
    public void handOverById(Long id, String fromUsername, String toUsername) {
        reportMapper.updateCreatorById(id, fromUsername, toUsername);
    }

    @Override
    public List<ReportDTO> findByDataset(Long datasetId) {
        return reportMapper.selectByDatasetId(datasetId);
    }

    @Override
    public List<ReportDTO> selectByDatasetRecycle(Long datasetId) {
        return reportMapper.selectByDatasetRecycle(datasetId);
    }
}
