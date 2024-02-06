package net.aopacloud.superbi.controller;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.service.HistoryDownloadService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: hudong
 * @date: 2023/10/31
 * @description:
 */
@RestController
@RequiredArgsConstructor
public class HistoryDownloadController {

    private final HistoryDownloadService historyDownloadService;

    /**
     * download history result
     *
     * @param queryLogId
     */
    @PostMapping("dataset/queryLog/{queryLogId}/download")
    public void downloadHistoryResult(@PathVariable Long queryLogId) {
        historyDownloadService.downloadHistoryResult(queryLogId);
    }

}
