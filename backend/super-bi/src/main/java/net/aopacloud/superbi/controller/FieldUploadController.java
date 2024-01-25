package net.aopacloud.superbi.controller;

import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.domain.ExcelReader;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FieldUploadController {

    /**
     * parse single column excel
     * @param file
     * @return
     */
    @PostMapping("excel/parse")
    public RestApiResponse<List<String>> parseExcel(@RequestParam("file") MultipartFile file){

        List<String> data = new ExcelReader().readSingleColumn(file);

        return RestApiResponse.success(data);
    }

}
