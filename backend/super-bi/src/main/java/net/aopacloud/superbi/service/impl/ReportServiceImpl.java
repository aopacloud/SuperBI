package net.aopacloud.superbi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.exception.ObjectNotFoundException;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.mapper.DashboardMapper;
import net.aopacloud.superbi.mapper.ReportFieldMapper;
import net.aopacloud.superbi.mapper.ReportMapper;
import net.aopacloud.superbi.model.converter.ReportConverter;
import net.aopacloud.superbi.model.dto.DashboardDTO;
import net.aopacloud.superbi.model.dto.DatasetAuthorizeDTO;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.ReportDTO;
import net.aopacloud.superbi.model.entity.Report;
import net.aopacloud.superbi.model.query.ReportQuery;
import net.aopacloud.superbi.model.vo.ReportVO;
import net.aopacloud.superbi.service.*;
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

    private final ReportConverter reportConverter;

    private final WorkspaceUserResourceService workspaceUserResourceService;

    private final DatasetAuthorizeService datasetAuthorizeService;

    private final ReportPrivilegeService reportPrivilegeService;

    private final SysUserService sysUserService;

    @Override
    public ReportDTO findOne(Long id) {
        Report report = Optional.ofNullable(reportMapper.selectById(id)).orElseThrow(() -> new ObjectNotFoundException("dataset not found"));
        ReportDTO reportDTO = reportConverter.entityToDTO(report);
        DatasetDTO datasetDTO = datasetService.findOne(reportDTO.getDatasetId());
        reportDTO.setDataset(datasetDTO);
        reportPrivilegeService.fillPrivilege(reportDTO, LoginContextHolder.getUsername());

        return reportDTO;
    }

    @Override
    public List<DashboardDTO> findDashboard(Long id) {
        List<DashboardDTO> dashboardDTOS = dashboardMapper.selectByReport(id);
        return dashboardDTOS;
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

        if (!datasetIds.isEmpty()) {
            query.setDatasetIds(datasetIds);
        }

        List<ReportDTO> reportDTOS = reportMapper.search(query);

        int start = Math.min(( query.getPageNum() -1 ) * query.getPageSize(), reportDTOS.size());
        int end = Math.min(start + query.getPageSize(), reportDTOS.size());

        List<ReportDTO> result = reportDTOS.subList(start, end);

        reportPrivilegeService.batchFillPrivilege(result, currentUser);

        result.stream().forEach(report -> report.setQueryParam(null));

        PageVO pageVO = new PageVO(reportConverter.toVOList(result), reportDTOS.size());
        return pageVO;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public ReportDTO saveOrUpdate(ReportDTO reportDTO) {

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
    public ReportDTO saveAs(ReportDTO reportDTO) {

        reportDTO.setId(null);

        return saveOrUpdate(reportDTO);
    }

    public void deleteById(Long id) {
        reportMapper.deleteById(id);
    }

    @Override
    public Integer countByUsername(String username) {
        return reportMapper.countByUsername(username);
    }

    @Override
    public void handOver(String fromUsername, String toUsername) {
        reportMapper.updateCreator(fromUsername, toUsername);
    }
}
