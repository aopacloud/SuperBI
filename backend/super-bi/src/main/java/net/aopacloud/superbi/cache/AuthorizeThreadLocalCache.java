package net.aopacloud.superbi.cache;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.google.common.collect.Maps;
import net.aopacloud.superbi.model.dto.DashboardShareDTO;
import net.aopacloud.superbi.model.entity.DatasetAuthorize;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class AuthorizeThreadLocalCache {

    private final static String DATASET_AUTHORIZE_KEY = "dataset";
    private final static String DASHBOARD_AUTHORIZE_KEY = "dashboard";
    private final static String SUPER_ADMIN_KEY = "superAdmin";


    private static final TransmittableThreadLocal<Map<String, Object>> THREAD_LOCAL = new TransmittableThreadLocal<>();

    public static void setSuperAdmin(boolean value) {
        Map<String, Object> map = getLocalMap();
        map.put(SUPER_ADMIN_KEY, value);
    }

    public static Boolean getSuperAdmin() {
        Map<String, Object> map = getLocalMap();
        return (Boolean) map.get(SUPER_ADMIN_KEY);
    }

    public static void setDatasetAuthorize(Long workspace, List<DatasetAuthorize> value) {

        Map<String, Object> map = getLocalMap();
        Map<Long, List<DatasetAuthorize>> cache = (Map<Long, List<DatasetAuthorize>>) map.get(DATASET_AUTHORIZE_KEY);
        if(Objects.isNull(cache)) {
            cache = Maps.newConcurrentMap();
            map.put(DATASET_AUTHORIZE_KEY, cache);
        }
        cache.put(workspace, value);

    }

    public static List<DatasetAuthorize> getDatasetAuthorize(Long workspace) {
        Map<String, Object> map = getLocalMap();
        Map<Long, List<DatasetAuthorize>> cache = (Map<Long, List<DatasetAuthorize>>) map.get(DATASET_AUTHORIZE_KEY);
        if (Objects.isNull(cache)) {
            return null;
        }

        return cache.get(workspace);
    }

    public static void setDashboardShare(Long workspace, List<DashboardShareDTO> value) {

        Map<String, Object> map = getLocalMap();
        Map<Long, List<DashboardShareDTO>> cache = (Map<Long, List<DashboardShareDTO>>) map.get(DASHBOARD_AUTHORIZE_KEY);
        if(Objects.isNull(cache)) {
            cache = Maps.newConcurrentMap();
            map.put(DASHBOARD_AUTHORIZE_KEY, cache);
        }
        cache.put(workspace, value);

    }

    public static List<DashboardShareDTO> getDashboardShare(Long workspace) {
        Map<String, Object> map = getLocalMap();
        Map<Long, List<DashboardShareDTO>> cache = (Map<Long, List<DashboardShareDTO>>) map.get(DASHBOARD_AUTHORIZE_KEY);
        if (Objects.isNull(cache)) {
            return null;
        }

        return cache.get(workspace);
    }

    public static Map<String, Object> getLocalMap() {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new ConcurrentHashMap<>();
            THREAD_LOCAL.set(map);
        }
        return map;
    }
    public static void remove() {
        THREAD_LOCAL.remove();
    }

}
