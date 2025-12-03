package org.example.service;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class BinlogOffsetStore {
    private final Path path;

    public BinlogOffsetStore(String filePath) {
        this.path = Paths.get(filePath).toAbsolutePath();
    }

    public synchronized BinlogOffset load() {
        try {
            if (!Files.exists(path)) {
                return null;
            }
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length == 0) {
                return null;
            }
            return JSON.parseObject(new String(bytes, StandardCharsets.UTF_8), BinlogOffset.class);
        } catch (Exception e) {
            log.warn("读取位点文件失败: {}", e.getMessage());
            return null;
        }
    }

    public synchronized void save(BinlogOffset offset) {
        try {
            byte[] bytes = JSON.toJSONString(offset).getBytes(StandardCharsets.UTF_8);
            // 简单写入；如需更强一致性，可使用临时文件+原子移动
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, bytes);
        } catch (IOException e) {
            log.warn("写入位点文件失败: {}", e.getMessage());
        }
    }
}