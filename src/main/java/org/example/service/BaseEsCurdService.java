package org.example.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Service
public class BaseEsCurdService {


    @Autowired
    ElasticsearchClient client;


    public <T extends Identifiable> void insert(T t) {
        try {
            client.index(i -> i
                    .index(t.index())
                    .id(t.getId().toString())
                    .document(t)
            );
        } catch (Exception e) {
            log.error("新增ES 数据失败", e);
        }
    }

    /**
     * 批量插入文档到指定索引
     *
     * @param indexName 索引名称
     * @param documents 要插入的文档列表
     * @param <T>       文档类型
     */
    public <T extends Identifiable> void bulkInsert(String indexName, List<T> documents) {
        // 定义线程池大小，可根据实际情况调整
        int threadPoolSize = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        // 将大列表分割成小批次
        int batchSize = 3000; // 每批处理3000条，可根据实际情况调整
        List<List<T>> batches = partitionList(documents, batchSize);
        // 使用CountDownLatch等待所有任务完成
        CountDownLatch latch = new CountDownLatch(batches.size());
        for (List<T> batch : batches) {
            executor.submit(() -> {
                try {
                    client.bulk(b -> b.operations(
                            batch.stream()
                                    .map(doc -> BulkOperation.of(op -> op
                                            .index(idx -> idx
                                                    .index(indexName)
                                                    .document(doc)
                                            )
                                    ))
                                    .collect(Collectors.toList())
                    ));
                    log.info("批次插入成功 {} 条", batch.size());
                } catch (Exception e) {
                    log.error("批次插入ES数据失败", e);
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            // 等待所有任务完成，设置超时时间避免无限等待
            latch.await(5, TimeUnit.MINUTES);
            log.info("全量新增完成，共 {} 条", documents.size());
        } catch (InterruptedException e) {
            log.error("等待线程完成时被中断", e);
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }


    public <T extends Identifiable> void update(T t) {
        try {
            UpdateResponse updateResponse = client.update(g -> g
                    .index(t.index())
                    .doc(t)
                    .refresh(Refresh.True)
                    .id(t.getId().toString()), t.getClass()
            );

            // 获取更新结果
            String result = updateResponse.result().jsonValue();
            log.info("ES update result 数据成功，索引: {}, ID: {}", t.index(), result);
            if ("updated".equals(result)) {
                log.info("更新ES数据成功，索引: {}, ID: {}", t.index(), t.getId());
            } else {
                log.warn("更新ES数据未成功，索引: {}, ID: {}, 结果: {}", t.index(), t.getId(), result);
            }

        } catch (Exception e) {
            log.error("更新ES 数据失败", e);
        }
    }

    public void delete(Long id, String INDEX) {
        try {
            client.delete(d -> d
                    .index(INDEX)
                    .id(id.toString())
            );
        } catch (Exception e) {
            log.error("删除ES 数据失败", e);
        }
    }

    // 辅助方法：将大列表分割成小批次
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }

}
