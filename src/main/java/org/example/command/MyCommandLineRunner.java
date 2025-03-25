package org.example.command;

import lombok.extern.slf4j.Slf4j;
import org.example.service.BinlogService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MyCommandLineRunner implements CommandLineRunner {

    @Autowired
    private BinlogService binlogService;

    @Override
    public void run(String... args) throws Exception {
        // Spring 容器已经初始化完毕，可以安全地访问 myService Bean
        if (binlogService != null) {
            binlogService.process();
        } else {
            log.info("binlogService is null");
        }
    }
}
