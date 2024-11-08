package net.aopacloud.superbi.service.impl;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.enums.StatusEnum;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.mapper.ResourceMapper;
import net.aopacloud.superbi.model.dto.DashboardComponentDTO;
import net.aopacloud.superbi.model.dto.DashboardDTO;
import net.aopacloud.superbi.model.dto.ReportDTO;
import net.aopacloud.superbi.model.entity.Resource;
import net.aopacloud.superbi.service.DashboardService;
import net.aopacloud.superbi.service.DatasetService;
import net.aopacloud.superbi.service.HandOverService;
import net.aopacloud.superbi.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HandOverServiceImpl implements HandOverService {

    private final DatasetService datasetService;

    private final DashboardService dashboardService;

    private final ReportService reportService;

    private final ResourceMapper resourceMapper;

    @Override
    public boolean checkUserHasResource(String username) {

        Integer dashboardCount = dashboardService.countByUsername(username);
        Integer datasetCount = datasetService.countByUsername(username);
        Integer reportCount = reportService.countByUsername(username);

        return dashboardCount > 0 || datasetCount > 0 || reportCount > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveUserResource(String fromUser, String toUser, PositionEnum position) {

        if (Strings.isNullOrEmpty(fromUser) || Strings.isNullOrEmpty(toUser)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.HANDOVER_USER_NOT_EMPTY));
        }

        if (Objects.isNull(position)) {
            dashboardService.handOver(fromUser, toUser);
            datasetService.handOver(fromUser, toUser);
            reportService.handOver(fromUser, toUser);
            return;
        }

        switch (position) {
            case DASHBOARD:
                dashboardService.handOver(fromUser, toUser);
                break;
            case DATASET:
                datasetService.handOver(fromUser, toUser);
                break;
            case REPORT:
                reportService.handOver(fromUser, toUser);
                break;
            default:

        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveUserResourceById(Long id, String fromUser, String toUser, PositionEnum position, Boolean autoTrans) {
        if (id == null || Strings.isNullOrEmpty(fromUser) || Strings.isNullOrEmpty(toUser) || Objects.isNull(position)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.HANDOVER_USER_NOT_EMPTY));
        }

        moveResource(id, toUser, position);
        switch (position) {
            case DASHBOARD:
                DashboardDTO dashboardDTO = dashboardService.findOne(id);
                dashboardService.handOverById(id, fromUser, toUser);
                if (autoTrans) {
                    if (Objects.nonNull(dashboardDTO)) {
                        List<Long> reportIdList = dashboardDTO.getDashboardComponents().stream().filter(dashboardComponentDTO -> dashboardComponentDTO.getType().equals("REPORT")).map(DashboardComponentDTO::getReportId).collect(Collectors.toList());
                        reportIdList.stream().filter(reportId -> reportService.findOne(reportId).getCreator().equals(fromUser)).collect(Collectors.toList()).forEach(
                                reportId -> {
                                    moveResource(reportId, toUser, PositionEnum.REPORT);
                                    reportService.handOverById(reportId, fromUser, toUser);
                                }
                        );
                    }
                }
                break;
            case DATASET:
                List<ReportDTO> reportDTOList = reportService.findByDataset(id);
                datasetService.handOverById(id, fromUser, toUser);
                if (autoTrans) {
                    if (reportDTOList != null && reportDTOList.size() > 0) {
                        reportDTOList.stream().filter(reportDTO -> reportDTO.getCreator().equals(fromUser)).collect(Collectors.toList()).forEach(
                                reportDTO -> {
                                    moveResource(reportDTO.getId(), toUser, PositionEnum.REPORT);
                                    reportService.handOverById(reportDTO.getId(), fromUser, toUser);
                                }
                        );
                    }
                }
                break;
            case REPORT:
                reportService.handOverById(id, fromUser, toUser);
                break;
            default:
        }
    }

    private void moveResource(Long id, String toUser, PositionEnum position) {
        Resource resource = new Resource();
        resource.setSourceId(id);
        resource.setPosition(position);
        resource.setCreator(toUser);
        resourceMapper.update(resource);
    }

    @Override
    public void moveUserResourceByIdList(List<Long> idList, List<String> fromUserList, String toUser, PositionEnum position, Boolean autoTrans) {
        for (int i = 0; i < idList.size(); i++) {
            moveUserResourceById(idList.get(i), fromUserList.get(i), toUser, position, autoTrans);
            moveResource(idList.get(i), toUser, position);
        }
    }
}
