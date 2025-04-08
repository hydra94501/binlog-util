package org.example.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.*;
import org.example.mapper.*;
import org.example.pojo.vo.CommentVideoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
public class ShortVideoEsRestService {


    private static final String INDEX = "short_video_index";

    private static final String ES_HOST = "http://shortVideo-es:9200";

    @Autowired
    private ShortVideoMapper shortVideoMapper;

    @Autowired
    private LabelMapper labelMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentReplyMapper commentReplyMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BaseEsCurdService baseEsCurdService;

    public String ShortVideoFullInsert() {
        deleteAll("short_video_index");
        List<ShortVideo> shortVideoList = shortVideoMapper.selectAll();
        baseEsCurdService.bulkInsert("short_video_index", shortVideoList);
        log.info("全量更新成功 {} 条", shortVideoList.size());
        return "全量更新成功 " + shortVideoList.size() + " 条";
    }


    public String LabelFullInsert() {
        deleteAll("label_index");
        List<Label> labelList = labelMapper.selectAll();
        baseEsCurdService.bulkInsert("label_index", labelList);
        log.info("全量更新成功 {} 条", labelList.size());
        return "全量更新成功 " + labelList.size() + " 条";
    }


    public IndexResponse insert(ShortVideo ShortVideo) {

        try {
            String json = JSONUtil.toJsonStr(ShortVideo.toEsDocument());
            String url = ES_HOST + "/" + INDEX + "/_doc/" + ShortVideo.getId();
            String response = HttpRequest.post(url)
                    .contentType("application/json")
                    .body(json)
                    .execute()
                    .body();
            log.info("es insert response{}", response);
        } catch (Exception e) {
            log.error("新增ES 数据失败", e);
        }
        return null;
    }


    public UpdateResponse update(ShortVideo ShortVideo) {
        try {
            // 创建 JSON 格式的请求体
            String json = JSONUtil.toJsonStr(ShortVideo.toEsDocument());
            // 发送 HTTP POST 请求进行更新
            String url = ES_HOST + "/" + INDEX + "/_update/" + ShortVideo.getId();
            String response = HttpRequest.post(url)
                    .contentType("application/json")
                    .body("{\"doc\":" + json + "}") // 使用 _update 需要将更新数据放在 "doc" 里面
                    .execute()
                    .body();
            log.info("es update response{}", response);
        } catch (Exception e) {
            log.error("更新ES 数据失败", e);
        }
        return null;
    }

    public DeleteResponse delete(Long id) {
        try {
            // 发送 HTTP DELETE 请求进行删除
            String url = ES_HOST + "/" + INDEX + "/_doc/" + id;
            String response = HttpRequest.delete(url)
                    .execute()
                    .body();
            log.info("es delete response{}", response);
        } catch (Exception e) {
            log.error("删除ES 数据失败", e);
        }
        return null;
    }


    public void deleteAll(String index) {
        try {
            // 使用 _delete_by_query 来删除所有文档
            String url = ES_HOST + "/" + index + "/_delete_by_query";
            // 查询所有文档
            String jsonBody = "{ \"query\": { \"match_all\": {} } }";

            String response = HttpRequest.post(url)
                    .body(jsonBody)
                    .execute()
                    .body();

            log.info("es delete all response: {}", response);
        } catch (Exception e) {
            log.error("删除所有ES数据失败", e);
        }
    }

    public String commentFullInsert() throws IOException, InterruptedException {
        // 1. 清理旧索引
        deleteAll("comment_index");
        baseEsCurdService.deleteIndex("comment_index");
        baseEsCurdService.createIndexWithIKAnalyzer("comment_index");

        // 2. 初始化分页参数
        int currentPage = 1;
        int pageSize = 50000;  // 每页50000条数据
        long totalSize = 0;
        long processedCount = 0;

        // 3. 创建线程池
        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        // 4. 先查询总数
        Page<CommentVideoVo> countPage = new Page<>(currentPage, 1);
        IPage<CommentVideoVo> totalCountPage = commentMapper.selectAll(countPage);
        totalSize = totalCountPage.getTotal();

        // 5. 分页查询 + 多线程批量插入
        while (processedCount < totalSize) {
            Page<CommentVideoVo> dataPage = new Page<>(currentPage, pageSize);
            IPage<CommentVideoVo> commentPage = commentMapper.selectAll(dataPage);
            List<CommentVideoVo> batchRecords = commentPage.getRecords();
            // 提交任务到线程池
            int finalCurrentPage = currentPage;
            futures.add(executorService.submit(() -> {
                try {
                    baseEsCurdService.bulkInsertWithChineseAnalyzer(
                            "comment_index", batchRecords, CommentVideoVo::getCommentText);
                } catch (IOException | InterruptedException e) {
                    log.error("批量插入失败，当前页: {}", finalCurrentPage, e);
                }
            }));
            processedCount += batchRecords.size();
            currentPage++;
            // 每处理10页打印一次进度
            if (currentPage % 10 == 0) {
                log.info("处理进度: {}/{} ({}%)",
                        processedCount, totalSize,
                        (processedCount * 100 / totalSize));
            }
        }
        // 6. 等待所有线程完成
        for (Future<?> future : futures) {
            try {
                future.get(); // 阻塞直到任务完成
            } catch (ExecutionException e) {
                log.error("线程执行异常", e);
            }
        }
        // 7. 关闭线程池
        executorService.shutdown();

        log.info("全量更新完成，总计 {} 条数据", totalSize);
        return "全量更新成功，共 " + totalSize + " 条数据";
    }

    public String commentReplyFullInsert() {
        deleteAll("comment_reply_index");
        List<CommentReply> commentReplies = commentReplyMapper.selectAll();
        baseEsCurdService.bulkInsert("comment_reply_index", commentReplies);
        log.info("全量更新成功 {} 条", commentReplies.size());
        return "全量更新成功 " + commentReplies.size() + " 条";
    }

    public String userFullInsert() {
        // 记录总开始时间
        deleteAll("user_index");
        List<User> users = userMapper.selectAll();
        baseEsCurdService.bulkInsert("user_index", users);
        return "全量更新成功 " + users.size() + " 条";
    }
}
