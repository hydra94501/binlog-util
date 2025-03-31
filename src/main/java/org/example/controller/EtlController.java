package org.example.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Label;
import org.example.service.ShortVideoEsRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/etl")
public class EtlController {
    @Autowired
    private ShortVideoEsRestService videoEsRestService;

    @GetMapping("/full/video")
    public String video() {
        return videoEsRestService.ShortVideoFullInsert();
    }

    @GetMapping("/full/label")
    public String label() {
        return videoEsRestService.LabelFullInsert();
    }


    @GetMapping("/full/comment")
    public String comment() throws IOException {
        return videoEsRestService.commentFullInsert();
    }

    @GetMapping("/full/commentReply")
    public String commentReply() {
        return videoEsRestService.commentReplyFullInsert();
    }

    @GetMapping("/full/user")
    public String user() {
        return videoEsRestService.userFullInsert();
    }



    @Autowired
    ElasticsearchClient client;

    @GetMapping("test")
    public List<Label> test() throws IOException {
        test2("7");
        return null;
        //return list;
    }


    public List<Label> getLabelsByNamePattern(String like) throws IOException {
        SearchResponse<Label> searchResponse = client.search(s -> s
                        .index("label_index")
                        .query(q -> q.wildcard(t -> t.field("label").wildcard("*" + like + "*"))
                        ),
                Label.class
        );

        // 获取搜索结果中的 hits 数据
        HitsMetadata<Label> hitsMetadata = searchResponse.hits();

        // 用来保存 Label 对象的列表
        List<Label> LabelList = new ArrayList<>();

        // 遍历每一个 hit，将其转换为 Label 对象
        for (Hit<Label> hit : hitsMetadata.hits()) {
            // 获取 Label 对象
            Label Label = hit.source();
            // 将 Label 对象添加到列表中
            LabelList.add(Label);
        }

        // 返回所有符合条件的 Label 对象
        return LabelList;
    }

    public void test2(String like) throws IOException {
        // 构建查询请求
        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder().index("label_index"); // 使用 label_index 索引

        searchRequestBuilder.query(q -> q.wildcard(t -> t.field("label").wildcard("*" + like + "*")));


        client.search(searchRequestBuilder.build(), Label.class);
    }
}
