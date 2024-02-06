package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.ReportDTO;

import java.util.List;

public interface ReportPrivilegeService {

    List<ReportDTO> batchFillPrivilege(List<ReportDTO> reports, String username);

    ReportDTO fillPrivilege(ReportDTO report, String username);
}
