package org.example.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.ExistsRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Service
public class BaseEsCurdService {


    @Autowired
    ElasticsearchClient client;

    private final ObjectMapper objectMapper;

    private final ExecutorService executor;

    public BaseEsCurdService(ObjectMapper objectMapper) {
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        this.objectMapper = new ObjectMapper()
                // 支持MyBatis-Plus注解
                .registerModule(new ParameterNamesModule())
                // 默认使用下划线命名策略
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }


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
                                                    .id(doc.getId().toString())
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

    public <T extends Identifiable> void bulkInsertWithChineseAnalyzer(
            String indexName,
            List<T> documents,
            Function<T, String> commentTextExtractor) throws IOException, InterruptedException {
        // 1. 配置线程池和计数器
        int batchSize = 3000;
        int totalDocs = documents.size();
        int totalBatches = (totalDocs + batchSize - 1) / batchSize;

        CountDownLatch latch = new CountDownLatch(totalBatches);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        BlockingQueue<Exception> exceptionQueue = new LinkedBlockingQueue<>();

        // 2. 分批提交任务
        for (int i = 0; i < totalDocs; i += batchSize) {
            List<T> batch = documents.subList(i, Math.min(i + batchSize, totalDocs));
            executor.submit(() -> {
                try {
                    processBatch(indexName, batch, commentTextExtractor);
                    successCount.addAndGet(batch.size());
                    log.debug("成功处理批次: {}/{}", successCount.get(), totalDocs);
                } catch (Exception e) {
                    failureCount.addAndGet(batch.size());
                    exceptionQueue.offer(e);
                    log.error("批次处理失败: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 3. 等待所有任务完成
        boolean allFinished = latch.await(10, TimeUnit.MINUTES);

        // 4. 处理结果
        if (!allFinished) {
            log.warn("部分批次未在超时时间内完成");
        }

        if (!exceptionQueue.isEmpty()) {
            throw new IOException(String.format(
                    "批量插入完成但有错误: 成功=%d, 失败=%d, 首个错误: %s",
                    successCount.get(),
                    failureCount.get(),
                    exceptionQueue.peek().getMessage()
            ));
        }
        // 5. 刷新索引
        client.indices().refresh(r -> r.index(indexName));
        log.info("批量插入完成: 总数={}, 成功={}, 失败={}",
                totalDocs, successCount.get(), failureCount.get());
    }

    private <T extends Identifiable> void processBatch(
            String indexName,
            List<T> batch,
            Function<T, String> commentTextExtractor) throws IOException {

        BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
        for (T doc : batch) {
            Map<String, Object> docMap = objectMapper.convertValue(doc, Map.class);
            docMap.put("comment_text", commentTextExtractor.apply(doc));

            bulkBuilder.operations(op -> op
                    .index(idx -> idx
                            .index(indexName)
                            .id(doc.getId().toString())
                            .document(docMap)
                    ));
        }

        BulkResponse response = client.bulk(bulkBuilder.build());

        if (response.errors()) {
            response.items().stream()
                    .filter(item -> item.error() != null)
                    .forEach(item -> log.error(
                            "文档插入失败: ID={}, 原因: {}",
                            item.id(),
                            item.error().reason()
                    ));
            throw new IOException("批次中存在插入失败的文档");

        }
    }


    /**
     * 创建索引并设置 IK 分词器
     *
     * @param indexName
     * @throws IOException
     */
    public void createIndexWithIKAnalyzer(String indexName) throws IOException {
        client.indices().create(c -> c
                .index(indexName)
                .settings(s -> s
                        .analysis(a -> a
                                .analyzer("ik_analyzer", an -> an
                                        .custom(cu -> cu
                                                .tokenizer("ik_max_word") // 使用 ik_max_word 分词器
                                        )
                                )
                        )
                )
                .mappings(m -> m
                        .properties("comment_text", p -> p
                                .text(t -> t
                                        .analyzer("ik_analyzer") // 默认使用 ik_max_word
                                        .fields("ik_smart", f -> f.text(ft -> ft.analyzer("ik_smart"))) // 添加 ik_smart 子字段
                                )
                        )
                )
        );
    }


    public <T extends Identifiable> void update(T t) {
        try {
            UpdateResponse<? extends Identifiable> updateResponse = client.update(g -> g
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


    public boolean exists(String indexName, String id) {
        try {
            // 构建Exists请求
            ExistsRequest existsRequest = ExistsRequest.of(b -> b
                    .index(indexName)
                    .id(id)
            );
            // 执行请求
            BooleanResponse response = client.exists(existsRequest);
            // 返回结果
            return response.value();
        } catch (IOException e) {
            log.error("检查文档是否存在时发生IO异常, index={}, id={}", indexName, id, e);
            return false;
        } catch (ElasticsearchException e) {
            log.error("Elasticsearch检查文档存在时异常, index={}, id={}", indexName, id, e);
            return false;
        }
    }

    public void deleteIndex(String commentIndex) {
        try {
            // 先尝试删除可能存在的旧索引
            client.indices().delete(d -> d.index(commentIndex));
            log.info("已删除旧索引");
        } catch (Exception e) {
            log.error("删除索引时发生异常", e);
        }
    }
}
