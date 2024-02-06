package net.aopacloud.superbi.util;

import net.aopacloud.superbi.common.core.utils.ServletUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author: hudong
 * @date: 2023/10/31
 * @description:
 */
public class Downloader {

    public static HttpServletResponse setExcelResponseHeader(String fileName) throws UnsupportedEncodingException {
        HttpServletResponse response = ServletUtils.getResponse();
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Connection, User-Agent, Content-Disposition");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
        response.setContentType("application/vnd.ms-excel;charset=gb2312");
        return response;
    }

}
