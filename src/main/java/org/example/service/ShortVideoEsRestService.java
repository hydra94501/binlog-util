package org.example.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.*;
import org.example.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        for (ShortVideo shortVideo : shortVideoList) {
            baseEsCurdService.insert(shortVideo.toEsDocument());
        }
        log.info("全量更新成功 {} 条", shortVideoList.size());
        return "全量更新成功 " + shortVideoList.size() + " 条";
    }


    public String LabelFullInsert() {
        deleteAll("label_index");
        List<Label> labelList = labelMapper.selectAll();
        for (Label label : labelList) {
            baseEsCurdService.insert(label);
        }
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
            String jsonBody = "{ \"query\": { \"match_all\": {} } }";  // 查询所有文档

            String response = HttpRequest.post(url)
                    .body(jsonBody)
                    .execute()
                    .body();

            log.info("es delete all response: {}", response);
        } catch (Exception e) {
            log.error("删除所有ES数据失败", e);
        }
    }

    public String commentFullInsert() {
        deleteAll("comment_index");
        List<Comment> comments = commentMapper.selectAll();
        for (Comment Comment : comments) {
            baseEsCurdService.insert(Comment.toEsDocument());
        }
        log.info("全量更新成功 {} 条", comments.size());
        return "全量更新成功 " + comments.size() + " 条";
    }

    public String commentReplyFullInsert() {
        deleteAll("comment_reply_index");
        List<CommentReply> commentReplies = commentReplyMapper.selectAll();
        for (CommentReply commentReply : commentReplies) {
            baseEsCurdService.insert(commentReply.toEsDocument());
        }
        log.info("全量更新成功 {} 条", commentReplies.size());
        return "全量更新成功 " + commentReplies.size() + " 条";
    }

    public String userFullInsert() {
        deleteAll("user_index");
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            baseEsCurdService.insert(user.toEsDocument());
        }
        log.info("全量更新成功 {} 条", users.size());
        return "全量更新成功 " + users.size() + " 条";
    }
}
