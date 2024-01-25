package net.aopacloud.superbi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.queryEngine.model.QueryParam;
import net.aopacloud.superbi.service.HistoryDownloadService;
import net.aopacloud.superbi.model.dto.DatasetQueryLogDTO;
import net.aopacloud.superbi.service.DatasetQueryLogService;
import net.aopacloud.superbi.service.QueryService;
import net.aopacloud.superbi.util.JSONUtils;
import org.springframework.stereotype.Service;

/**
 * @author: hudong
 * @date: 2023/10/31
 * @description:
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryDownloadServiceImpl implements HistoryDownloadService {

    private final DatasetQueryLogService datasetQueryLogService;

    private final QueryService queryService;

    @Override
    public void downloadHistoryResult(Long queryLogId) {
        try {
            DatasetQueryLogDTO queryLog = datasetQueryLogService.findOne(queryLogId);

            String queryParam = queryLog.getQueryParam();

            queryService.download(JSONUtils.parseObject(queryParam, QueryParam.class), Boolean.FALSE);

        }catch (Exception e) {
            log.error("download error", e);
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DOWNLOAD_ERROR));
        }

    }
}
