package net.aopacloud.superbi.service.impl;

import com.google.common.base.Strings;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.service.DashboardService;
import net.aopacloud.superbi.service.DatasetService;
import net.aopacloud.superbi.service.HandOverService;
import net.aopacloud.superbi.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HandOverServiceImpl implements HandOverService {

    private final DatasetService datasetService;

    private final DashboardService dashboardService;

    private final ReportService reportService;

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

        if(Strings.isNullOrEmpty(fromUser) || Strings.isNullOrEmpty(toUser)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.HANDOVER_USER_NOT_EMPTY));
        }

        if(Objects.isNull(position)) {
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
}
