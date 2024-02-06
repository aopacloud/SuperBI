package net.aopacloud.superbi.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.model.vo.FunctionVO;
import net.aopacloud.superbi.service.FunctionService;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: hudong
 * @date: 2023/10/26
 * @description:
 */
@Service
@Slf4j
public class FunctionServiceImpl implements FunctionService {

    private List<FunctionVO> functions = Lists.newArrayList();

    @Override
    public List<FunctionVO> findAll() {

        if (functions.isEmpty()) {
            synchronized (this) {
                if (functions.isEmpty()) {
                    try (InputStream in = FunctionService.class.getClassLoader().getResourceAsStream("functions")) {
                        List<String> list = IOUtils.readLines(in);
                        functions = list.stream().map(line -> {
                            String[] items = line.split("--");
                            if (items.length == 3) {
                                return new FunctionVO(items[0], items[1], items[2]);
                            } else if (items.length == 2) {
                                return new FunctionVO(items[0], items[1]);
                            } else {
                                return new FunctionVO(items[0]);
                            }
                        }).collect(Collectors.toList());
                    } catch (Exception e) {
                        log.error("read functions file error", e);
                    }
                }
            }
        }
        return functions;
    }
}
