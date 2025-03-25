package org.example.service;

import com.alibaba.fastjson.JSON;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.*;
import org.example.mapper.ShortVideoMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BinlogService implements InitializingBean {

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    // 用于存储表 ID 到表名的映射
    private static final Map<Long, String> tableIdToNameMap = new HashMap<>();
    private static final Map<String, Map<Integer, String>> tableNameToColMap = new HashMap<>();

    private static final Map<String, Class> classMap = new HashMap<>();

    static {
        classMap.put("t_short_video", ShortVideo.class);
        classMap.put("t_label", Label.class);
        classMap.put("t_comment", Comment.class);
        classMap.put("t_comment_reply", CommentReply.class);
        classMap.put("t_user", User.class);
    }

    private Map<Integer, String> tableColVos_video_map;
    private Map<Integer, String> tableColVos_label_map;
    private Map<Integer, String> tableColVos_comment_map;
    private Map<Integer, String> tableColVos_comment_reply_map;
    private Map<Integer, String> tableColVos_user_map;

    private List<String> syncTables = Arrays.asList(
            "t_short_video", "t_label", "t_comment", "t_comment_reply", "t_user"
    );

    @Autowired
    private ShortVideoMapper shortVideoMapper;

    @Autowired
    private BaseEsCurdService esCurdService;


    // 泛型方法，用于将 JSON 字符串转换为指定类型的对象
    public <T> T convert(Serializable[] rows ,Map<Integer, String> tableColVos_label_map, Class<T> clazz) {
        Map<String,Object> objectMap = new HashMap<>(rows.length);
        for (int i = 0; i < rows.length; i++) {
            String colName =  tableColVos_label_map.get(i);
            Serializable colValue = rows[i];
            if(colValue instanceof Long){
                objectMap.put(colName, (Long) colValue);
            }
            if(colValue instanceof Integer){
                objectMap.put(colName, (Integer) colValue);
            }
            if(colValue instanceof byte[]){
                objectMap.put(colName,new String((byte[])colValue));
            }
        }
        String jsonString = JSON.toJSONString(objectMap);
        return JSON.parseObject(jsonString, clazz);
    }


    public <T> List<T> convert(List<Serializable[]> rows ,Map<Integer, String> tableColVos_label_map, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        for (Serializable[] row : rows) {
            list.add(convert(row,tableColVos_label_map,clazz));
        }
        return list;
    }

    public <T> List<T> convert(List<Serializable[]> rows ,long tableId) {
        String tableName =  tableIdToNameMap.get(tableId);
        Class<T> clazz = classMap.get(tableName);
        Map<Integer, String> tableColVos_label_map = tableNameToColMap.get(tableIdToNameMap.get(tableId));
        List<T> list = new ArrayList<>();
        for (Serializable[] row : rows) {
            list.add(convert(row,tableColVos_label_map,clazz));
        }
        return list;
    }



    public   void  process() {
        // 配置 BinaryLogClient
        BinaryLogClient client = new BinaryLogClient("shortVideo-mysql", 3306, username, password);
        client.setServerId(1); // 设置唯一的 serverId

        // 配置 EventDeserializer
        EventDeserializer eventDeserializer = new EventDeserializer();
        eventDeserializer.setCompatibilityMode(
                EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG,
                EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
        );
        client.setEventDeserializer(eventDeserializer);

        // 注册事件监听器
        client.registerEventListener(event -> {
            EventData data = event.getData();
            if (data instanceof TableMapEventData) {
                // 处理 TableMapEvent，保存表 ID 到表名的映射
                TableMapEventData tableMapEventData = (TableMapEventData) data;
                tableIdToNameMap.put(tableMapEventData.getTableId(), tableMapEventData.getTable());
                System.out.println("Table map event: " + tableMapEventData.getTable());
            } else if (data instanceof WriteRowsEventData) {
                // 处理 INSERT 事件
                WriteRowsEventData writeRowsEventData = (WriteRowsEventData) data;
                String tableName = tableIdToNameMap.get(writeRowsEventData.getTableId());
                if(!syncTables.contains(tableName) ){
                    return;
                }

                List<Identifiable> rows  =  convert(writeRowsEventData.getRows(),writeRowsEventData.getTableId());
                for (Identifiable  row : rows) {
                    log.info("发生了新增{},{}",row.getId(),row.toEsDocument());
                    esCurdService.insert(row.toEsDocument());
                    log.info("成功新增{},{}",row.getId(),row.toEsDocument());
                }

            } else if (data instanceof UpdateRowsEventData) {
                UpdateRowsEventData updateRowsEventData = (UpdateRowsEventData) data;
                String tableName = tableIdToNameMap.get(updateRowsEventData.getTableId());
                if(!syncTables.contains(tableName) ){
                    return;
                }

                List<Identifiable> rows  =  convert(updateRowsEventData.getRows().stream().map(Map.Entry::getValue).collect(Collectors.toList()), updateRowsEventData.getTableId());
                for (Identifiable  row : rows) {
                    log.info("发生了更新{},{}",row.getId(),row.toEsDocument());
                    esCurdService.update(row.toEsDocument());
                    log.info("成功更新{},{}",row.getId(),row.toEsDocument());
                }

            } else if (data instanceof DeleteRowsEventData) {
                DeleteRowsEventData deleteRowsEventData = (DeleteRowsEventData) data;
                String tableName = tableIdToNameMap.get(deleteRowsEventData.getTableId());
                if(!syncTables.contains(tableName) ){
                    return;
                }

                List<Identifiable> rows  =  convert(deleteRowsEventData.getRows(),deleteRowsEventData.getTableId());
                for (Identifiable row : rows) {
                    log.info("发生了删除{} {} ",row.getId(),row.index());
                    esCurdService.delete(row.getId(),row.index());
                    log.info("成功删除{},{}",row.getId(),row.index());
                }
            }
        });

        // 启动监听
        log.info("-----------开始启动binlog----");
        try {
            client.connect();
        } catch (Exception e) {
            log.error("-----------启动binlog 失败-----",e);
            e.printStackTrace();
        }
        log.info("-----------启动binlog 成功-----");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<TableColVo> tableColVos_video = shortVideoMapper.getTableSchemaByName("t_short_video");
        List<TableColVo> tableColVos_label = shortVideoMapper.getTableSchemaByName("t_label");
        List<TableColVo> tableColVos_comment = shortVideoMapper.getTableSchemaByName("t_comment");
        List<TableColVo> tableColVos_comment_reply = shortVideoMapper.getTableSchemaByName("t_comment_reply");
        List<TableColVo> tableColVos_user = shortVideoMapper.getTableSchemaByName("t_user");

        this.tableColVos_video_map = tableColVos_video.stream()
                .collect(Collectors.toMap(
                        TableColVo::getOrdinalPosition,       // key: ordinalPosition
                        TableColVo::getColumnName,                               // value: TableColVo 本身
                        (existing, replacement) -> existing   // 如果有重复的 ordinalPosition，保留第一个
                ));

        this.tableColVos_label_map = tableColVos_label.stream()
                .collect(Collectors.toMap(
                        TableColVo::getOrdinalPosition,       // key: ordinalPosition
                        TableColVo::getColumnName,                               // value: TableColVo 本身
                        (existing, replacement) -> existing   // 如果有重复的 ordinalPosition，保留第一个
                ));

        this.tableColVos_comment_map = tableColVos_comment.stream()
                .collect(Collectors.toMap(
                        TableColVo::getOrdinalPosition,
                        TableColVo::getColumnName,
                        (existing, replacement) -> existing
                ));

        this.tableColVos_comment_reply_map = tableColVos_comment_reply.stream()
                .collect(Collectors.toMap(
                        TableColVo::getOrdinalPosition,
                        TableColVo::getColumnName,
                        (existing, replacement) -> existing
                ));

        this.tableColVos_user_map = tableColVos_user.stream()
                .collect(Collectors.toMap(
                        TableColVo::getOrdinalPosition,
                        TableColVo::getColumnName,
                        (existing, replacement) -> existing
                ));

        tableNameToColMap.put("t_short_video", tableColVos_video_map);
        tableNameToColMap.put("t_label", tableColVos_label_map);
        tableNameToColMap.put("t_comment", tableColVos_comment_map);
        tableNameToColMap.put("t_comment_reply", tableColVos_comment_reply_map);
        tableNameToColMap.put("t_user", tableColVos_user_map);

    }
}
