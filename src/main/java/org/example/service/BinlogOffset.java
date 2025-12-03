package org.example.service;

import lombok.Data;

@Data
public class BinlogOffset {
    private String binlogFilename;
    private Long position;
    private String gtidSet;
    private Long ts;
}