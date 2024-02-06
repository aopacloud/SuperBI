package net.aopacloud.superbi.util;

import com.google.common.collect.Lists;
import lombok.Data;
import net.aopacloud.superbi.model.dto.DatasetApplyDTO;

import java.util.Date;
import java.util.List;

public class DatasetApplyUtils {

    public static String buildReviewStateJson(DatasetApplyDTO datasetApplyDTO) {

        List<Review> reviewList = Lists.newArrayList();

        Review applyReview = new Review();
        applyReview.setReviewName("开始审批");
        applyReview.setStatus("DONE");
        Handler handler = new Handler();
        handler.setName(datasetApplyDTO.getAliasName());
        handler.setStatus("DONE");
        handler.setCreateTime(datasetApplyDTO.getCreateTime());
        handler.setHandleTime(datasetApplyDTO.getCreateTime());
        applyReview.setHandler(Lists.newArrayList(handler));
        reviewList.add(applyReview);


        Review approveReview = new Review();
        approveReview.setReviewName("数据集创建人审批");
        if (datasetApplyDTO.getApproveStatus().isFinished()) {
            approveReview.setStatus("DONE");
        } else {
            approveReview.setStatus("DOING");
        }

        Handler approveHandler = new Handler();
        approveHandler.setName(datasetApplyDTO.getDatasetCreatorAliasName());
        if (datasetApplyDTO.getApproveStatus().isFinished()) {
            approveHandler.setStatus("DONE");
            approveHandler.setHandleTime(datasetApplyDTO.getUpdateTime());
        } else {
            approveHandler.setStatus("DOING");
        }

        approveReview.setHandler(Lists.newArrayList(approveHandler));
        reviewList.add(approveReview);


        Review finishReview = new Review();
        finishReview.setReviewName("完成审批");
        if (datasetApplyDTO.getApproveStatus().isFinished()) {
            finishReview.setStatus("DONE");
        } else {
            finishReview.setStatus("DOING");
        }
        reviewList.add(finishReview);


        return JSONUtils.toJsonString(reviewList);
    }

    @Data
    static class Review {
        private String reviewName;
        private String status;
        private List<Handler> handler;
    }

    @Data
    static class Handler {
        private String name;
        private String status;
        private Date handleTime;
        private Date createTime;
    }

}
