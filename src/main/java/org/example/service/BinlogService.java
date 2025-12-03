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
//binlog_row_image=FULL 避免 es 出现空数据
//

@Slf4j
@Service
public class BinlogService implements InitializingBean {

    @Value("${spring.datasource.host}")
    private String host;

    @Value("${spring.datasource.port}")
    private Integer port;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${binlog.offset.file:binlog-offset.json}")
    private String offsetFilePath;

    @Value("${binlog.server-id:1}")
    private long clientServerId;

    private BinlogOffsetStore offsetStore;
    private volatile boolean resumeLogged = false;
    private volatile boolean firstSaveLogged = false;

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
    public <T> T convert(Serializable[] rows, Map<Integer, String> tableColVos_label_map, Class<T> clazz) {
        Map<String, Object> objectMap = new HashMap<>(rows.length);
        for (int i = 0; i < rows.length; i++) {
            String colName = tableColVos_label_map.get(i);
            Serializable colValue = rows[i];
            if (colValue instanceof Long) {
                objectMap.put(colName, (Long) colValue);
            }
            if (colValue instanceof Integer) {
                objectMap.put(colName, (Integer) colValue);
            }
            if (colValue instanceof byte[]) {
                objectMap.put(colName, new String((byte[]) colValue));
            }
        }
        String jsonString = JSON.toJSONString(objectMap);
        return JSON.parseObject(jsonString, clazz);
    }

    // 新增：支持 includedColumns 位图的列映射，避免最小行镜像导致列错位
    public <T> T convert(Serializable[] rows, Map<Integer, String> tableColVos_label_map, BitSet includedColumns, Class<T> clazz) {
        Map<String, Object> objectMap = new HashMap<>(tableColVos_label_map.size());
        if (includedColumns != null) {
            int idx = 0;
            for (int pos = includedColumns.nextSetBit(0); pos >= 0; pos = includedColumns.nextSetBit(pos + 1)) {
                String colName = tableColVos_label_map.get(pos);
                if (colName == null) {
                    continue;
                }
                Serializable colValue = idx < rows.length ? rows[idx] : null;
                if (colValue == null) {
                    idx++;
                    continue;
                }
                if (colValue instanceof Long) {
                    objectMap.put(colName, (Long) colValue);
                } else if (colValue instanceof Integer) {
                    objectMap.put(colName, (Integer) colValue);
                } else if (colValue instanceof byte[]) {
                    objectMap.put(colName, new String((byte[]) colValue));
                } else {
                    objectMap.put(colName, colValue);
                }
                idx++;
            }
        } else {
            // 回退：不提供位图时沿用原有按索引映射逻辑
            for (int i = 0; i < rows.length; i++) {
                String colName = tableColVos_label_map.get(i);
                Serializable colValue = rows[i];
                if (colValue instanceof Long) {
                    objectMap.put(colName, (Long) colValue);
                } else if (colValue instanceof Integer) {
                    objectMap.put(colName, (Integer) colValue);
                } else if (colValue instanceof byte[]) {
                    objectMap.put(colName, new String((byte[]) colValue));
                } else {
                    objectMap.put(colName, colValue);
                }
            }
        }
        String jsonString = JSON.toJSONString(objectMap);
        return JSON.parseObject(jsonString, clazz);
    }

    public <T> List<T> convert(List<Serializable[]> rows, Map<Integer, String> tableColVos_label_map, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        for (Serializable[] row : rows) {
            list.add(convert(row, tableColVos_label_map, clazz));
        }
        return list;
    }

    // 新增：List 版本，支持位图
    public <T> List<T> convert(List<Serializable[]> rows, Map<Integer, String> tableColVos_label_map, BitSet includedColumns, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        for (Serializable[] row : rows) {
            list.add(convert(row, tableColVos_label_map, includedColumns, clazz));
        }
        return list;
    }

    public <T> List<T> convert(List<Serializable[]> rows, long tableId) {
        String tableName = tableIdToNameMap.get(tableId);
        Class<T> clazz = classMap.get(tableName);
        Map<Integer, String> tableColVos_label_map = tableNameToColMap.get(tableIdToNameMap.get(tableId));
        List<T> list = new ArrayList<>();
        for (Serializable[] row : rows) {
            list.add(convert(row, tableColVos_label_map, clazz));
        }
        return list;
    }

    // 新增：tableId 版本，支持位图
    public <T> List<T> convert(List<Serializable[]> rows, long tableId, BitSet includedColumns) {
        String tableName = tableIdToNameMap.get(tableId);
        Class<T> clazz = classMap.get(tableName);
        Map<Integer, String> tableColVos_label_map = tableNameToColMap.get(tableIdToNameMap.get(tableId));
        List<T> list = new ArrayList<>();
        for (Serializable[] row : rows) {
            list.add(convert(row, tableColVos_label_map, includedColumns, clazz));
        }
        return list;
    }

    // 新增：Update 专用，合并前后镜像，确保主键与改动列都正确映射
    public <T> List<T> convertUpdate(List<Map.Entry<Serializable[], Serializable[]>> rows,
                                     long tableId,
                                     BitSet includedColumnsBeforeUpdate,
                                     BitSet includedColumnsAfterUpdate) {
        String tableName = tableIdToNameMap.get(tableId);
        Class<T> clazz = classMap.get(tableName);
        Map<Integer, String> colMap = tableNameToColMap.get(tableName);
        List<T> list = new ArrayList<>();
        for (Map.Entry<Serializable[], Serializable[]> entry : rows) {
            Map<String, Object> objectMap = new HashMap<>(colMap.size());
            // 先映射前镜像：用于定位的主键/唯一键
            if (includedColumnsBeforeUpdate != null) {
                int idxBefore = 0;
                for (int pos = includedColumnsBeforeUpdate.nextSetBit(0); pos >= 0; pos = includedColumnsBeforeUpdate.nextSetBit(pos + 1)) {
                    String colName = colMap.get(pos);
                    Serializable colValue = idxBefore < entry.getKey().length ? entry.getKey()[idxBefore] : null;
                    if (colName != null && colValue != null) {
                        if (colValue instanceof Long) {
                            objectMap.put(colName, (Long) colValue);
                        } else if (colValue instanceof Integer) {
                            objectMap.put(colName, (Integer) colValue);
                        } else if (colValue instanceof byte[]) {
                            objectMap.put(colName, new String((byte[]) colValue));
                        } else {
                            objectMap.put(colName, colValue);
                        }
                    }
                    idxBefore++;
                }
            }
            // 再映射后镜像：覆盖变更列
            if (includedColumnsAfterUpdate != null) {
                int idxAfter = 0;
                for (int pos = includedColumnsAfterUpdate.nextSetBit(0); pos >= 0; pos = includedColumnsAfterUpdate.nextSetBit(pos + 1)) {
                    String colName = colMap.get(pos);
                    Serializable colValue = idxAfter < entry.getValue().length ? entry.getValue()[idxAfter] : null;
                    if (colName != null && colValue != null) {
                        if (colValue instanceof Long) {
                            objectMap.put(colName, (Long) colValue);
                        } else if (colValue instanceof Integer) {
                            objectMap.put(colName, (Integer) colValue);
                        } else if (colValue instanceof byte[]) {
                            objectMap.put(colName, new String((byte[]) colValue));
                        } else {
                            objectMap.put(colName, colValue);
                        }
                    }
                    idxAfter++;
                }
            }
            String jsonString = JSON.toJSONString(objectMap);
            list.add(JSON.parseObject(jsonString, clazz));
        }
        return list;
    }



    public void process() {
        // 配置 BinaryLogClient
        BinaryLogClient client = new BinaryLogClient(host, port, username, password);
        client.setServerId(clientServerId); // 设置唯一的 serverId（可配置）

        // 连接生命周期日志，便于确认恢复起点
        client.registerLifecycleListener(new BinaryLogClient.LifecycleListener() {
            @Override
            public void onConnect(BinaryLogClient c) {
                log.info("Binlog已连接，起始位点: {}@{}，GTID: {}，serverId: {}",
                        c.getBinlogFilename(), c.getBinlogPosition(), c.getGtidSet(), clientServerId);
            }
            @Override
            public void onDisconnect(BinaryLogClient c) {
                log.info("Binlog已断开连接");
            }
            @Override
            public void onCommunicationFailure(BinaryLogClient c, Exception ex) {
                log.error("Binlog通信失败", ex);
            }
            @Override
            public void onEventDeserializationFailure(BinaryLogClient c, Exception ex) {
                log.error("事件反序列化失败", ex);
            }
        });

        // 初始化位点存储
        this.offsetStore = new BinlogOffsetStore(offsetFilePath);
        BinlogOffset last = offsetStore.load();
        if (last != null) {
            if (last.getGtidSet() != null && !last.getGtidSet().isEmpty()) {
                client.setGtidSet(last.getGtidSet());
                log.info("从GTID集恢复: {}", last.getGtidSet());
            } else if (last.getBinlogFilename() != null && last.getPosition() != null) {
                client.setBinlogFilename(last.getBinlogFilename());
                client.setBinlogPosition(last.getPosition());
                log.info("从位点恢复: {}@{}", last.getBinlogFilename(), last.getPosition());
            }
        }

        // 配置 EventDeserializer
        EventDeserializer eventDeserializer = new EventDeserializer();
        eventDeserializer.setCompatibilityMode(
                EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG,
                EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
        );
        client.setEventDeserializer(eventDeserializer);

        // 注册事件监听器
        client.registerEventListener(event -> {
            // 首次确认开始处理的事件，用于判断是否已从位点恢复并开始消费
            if (!resumeLogged && event.getHeader() instanceof EventHeaderV4) {
                Long nextPos = ((EventHeaderV4) event.getHeader()).getNextPosition();
                log.info("已从位点恢复并开始消费事件: {}@{}，GTID: {}",
                        client.getBinlogFilename(), nextPos, client.getGtidSet());
                resumeLogged = true;
            }

            EventData data = event.getData();
            if (data instanceof TableMapEventData) {
                // 处理 TableMapEvent，保存表 ID 到表名的映射
                TableMapEventData tableMapEventData = (TableMapEventData) data;
                tableIdToNameMap.put(tableMapEventData.getTableId(), tableMapEventData.getTable());
                // 更新位点
                saveOffset(client, event);
            } else if (data instanceof WriteRowsEventData) {
                // 处理 INSERT 事件
                WriteRowsEventData writeRowsEventData = (WriteRowsEventData) data;
                String tableName = tableIdToNameMap.get(writeRowsEventData.getTableId());
                if (!syncTables.contains(tableName)) {
                    saveOffset(client, event);
                    return;
                }

                List<Identifiable> rows = convert(writeRowsEventData.getRows(), writeRowsEventData.getTableId(), writeRowsEventData.getIncludedColumns());
                for (Identifiable row : rows) {
                    log.info("发生了新增{},{}", row.getId(), row.toEsDocument());
                    esCurdService.insert(row.toEsDocument());
                    log.info("成功新增{},{}", row.getId(), row.toEsDocument());
                }
                // 事件处理成功后更新位点
                saveOffset(client, event);

            } else if (data instanceof UpdateRowsEventData) {
                UpdateRowsEventData updateRowsEventData = (UpdateRowsEventData) data;
                String tableName = tableIdToNameMap.get(updateRowsEventData.getTableId());
                if (!syncTables.contains(tableName)) {
                    saveOffset(client, event);
                    return;
                }

                List<Identifiable> rows = convertUpdate(updateRowsEventData.getRows(),
                        updateRowsEventData.getTableId(),
                        updateRowsEventData.getIncludedColumnsBeforeUpdate(),
                        updateRowsEventData.getIncludedColumns());
                for (Identifiable row : rows) {
                    if (esCurdService.exists(row.toEsDocument().index(), String.valueOf(row.getId()))) {
                        log.info("发生了更新{},{}", row.getId(), row.toEsDocument());
                        esCurdService.update(row.toEsDocument());
                        log.info("成功更新{},{}", row.getId(), row.toEsDocument());
                    } else {
                        log.info("发生了新增{},{}", row.getId(), row.toEsDocument());
                        esCurdService.insert(row.toEsDocument());
                        log.info("成功新增{},{}", row.getId(), row.toEsDocument());
                    }
                }
                // 事件处理成功后更新位点
                saveOffset(client, event);

            } else if (data instanceof DeleteRowsEventData) {
                DeleteRowsEventData deleteRowsEventData = (DeleteRowsEventData) data;
                String tableName = tableIdToNameMap.get(deleteRowsEventData.getTableId());
                if (!syncTables.contains(tableName)) {
                    saveOffset(client, event);
                    return;
                }

                List<Identifiable> rows = convert(deleteRowsEventData.getRows(), deleteRowsEventData.getTableId(), deleteRowsEventData.getIncludedColumns());
                for (Identifiable row : rows) {
                    log.info("发生了删除{} {} ", row.getId(), row.index());
                    esCurdService.delete(row.getId(), row.index());
                    log.info("成功删除{},{}", row.getId(), row.index());
                }
                // 事件处理成功后更新位点
                saveOffset(client, event);
            } else {
                // 非行事件也更新位点（如 Xid/Query/Rotate 等）
                saveOffset(client, event);
            }
        });

        // 启动监听
        log.info("-----------开始启动binlog----");
        try {
            client.connect();
        } catch (Exception e) {
            log.error("-----------启动binlog 失败-----", e);
            e.printStackTrace();
        }
        log.info("-----------启动binlog 成功-----");
    }

    private void saveOffset(BinaryLogClient client, Event event) {
        try {
            String currentFile = client.getBinlogFilename();
            Long nextPos = null;
            if (event.getHeader() instanceof EventHeaderV4) {
                nextPos = ((EventHeaderV4) event.getHeader()).getNextPosition();
            }
            BinlogOffset o = new BinlogOffset();
            o.setBinlogFilename(currentFile);
            o.setPosition(nextPos);
            // 如果使用了 GTID 模式，记录下当前 GTID 集
            String gtid = client.getGtidSet();
            if (gtid != null && !gtid.isEmpty()) {
                o.setGtidSet(gtid);
            }
            o.setTs(System.currentTimeMillis());
            offsetStore.save(o);
            if (!firstSaveLogged) {
                log.info("已开始持久化位点: {}@{}，GTID: {}", o.getBinlogFilename(), o.getPosition(), o.getGtidSet());
                firstSaveLogged = true;
            }
        } catch (Exception ex) {
            log.warn("持久化位点失败: {}", ex.getMessage());
        }
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
