package net.aopacloud.superbi.util;


import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author: hu.dong
 * @date: 2022/1/13
 **/
public class CollectionUtils {

    public static <T> List<T> subtract(List<T> originList, List<T> subList){
        List<T> list = Lists.newArrayList();
        for(T item : originList){
            if(!subList.contains(item)){
                list.add(item);
            }
        }
        return list;
    }

    public static <T> List<T> findSame(List<T> originList, List<T> subList){
        List<T> list = Lists.newArrayList();
        for(T item : originList){
            if(subList.contains(item)){
                list.add(item);
            }
        }
        return list;
    }



}
