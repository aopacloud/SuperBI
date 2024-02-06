package net.aopacloud.superbi.listener.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.aopacloud.superbi.enums.EventActionEnum;
import net.aopacloud.superbi.model.entity.DatasetAuthorize;

/**
 * Dataset authorize update event
 *
 * @author: hudong
 * @date: 2023/9/15
 * @description:
 */
@Data
@AllArgsConstructor
public class DatasetAuthorizeUpdateEvent {

    private String username;

    private Long datasetId;

    private EventActionEnum action;

    private DatasetAuthorize datasetAuthorize;

    public static DatasetAuthorizeUpdateEvent of(DatasetAuthorize datasetAuthorize, EventActionEnum action) {
        return new DatasetAuthorizeUpdateEvent(datasetAuthorize.getUsername(), datasetAuthorize.getDatasetId(), action, datasetAuthorize);
    }

    public static DatasetAuthorizeUpdateEvent of(String username, DatasetAuthorize datasetAuthorize, EventActionEnum action) {
        return new DatasetAuthorizeUpdateEvent(username, datasetAuthorize.getDatasetId(), action, datasetAuthorize);
    }
}
