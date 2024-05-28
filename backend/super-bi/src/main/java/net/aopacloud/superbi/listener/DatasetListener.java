package net.aopacloud.superbi.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.listener.event.DatasetDeleteEvent;
import net.aopacloud.superbi.listener.event.DatasetOfflineEvent;
import net.aopacloud.superbi.mapper.ReportMapper;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.ReportDTO;
import net.aopacloud.superbi.service.ReportService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatasetListener {

    private final ReportMapper reportMapper;

    @EventListener
    public void onDatasetDelete(DatasetDeleteEvent event) {
        log.info("receive event {}" , event);
        deletedReport(event.getDataset());
    }

    @EventListener
    public void onDatasetOffline(DatasetOfflineEvent event) {
        log.info("receive event {}" , event);
//        deletedReport(event.getDataset());
    }

    private void deletedReport(DatasetDTO datasetDTO) {
        if (Objects.isNull(datasetDTO)) {
            return;
        }
        List<ReportDTO> reports = reportMapper.selectByDatasetId(datasetDTO.getId());

        for(ReportDTO report: reports) {
            log.warn("delete report {} with dataset {} does offline or deleted", report, datasetDTO.getId());
            reportMapper.deleteById(report.getId());
        }
    }

}
