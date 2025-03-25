package org.example.service;


public interface Identifiable {

    /**
     * 数据库的id . ES 根据这个id 作为索引id
     * @return
     */
    Long getId();

    /**
     * ES 的索引
     * @return
     */
    String index();

    /**
     * 将 数据库字段 映射 为 ES 的字段
     * @return
     */
    Identifiable toEsDocument();
}
