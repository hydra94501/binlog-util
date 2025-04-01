package org.example.entity;

import lombok.Data;
import org.example.aop.annotations.PathProcess;

/**
 * @author Alan_   2025/3/31 19:05
 */
@Data
public class Image {

    @PathProcess("https://www.baidu.com")
    private String url;
}
