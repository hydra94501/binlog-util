package org.example.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.ShortVideo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShortVideoEsDaoService {

    @Autowired
    ElasticsearchClient client;

    private static final String INDEX = "short_video_index";

    public IndexResponse insert(ShortVideo ShortVideo)  {
        try {
            return client.index(i -> i
                    .index(INDEX)
                    .id(ShortVideo.getId().toString())
                    .document(ShortVideo)
            );
        }catch (Exception e){
            log.error("新增ES 数据失败",e);
        }
        return null;
    }


    public UpdateResponse update(ShortVideo ShortVideo)  {
        try {
           return client.update(g -> g
                    .index(INDEX)
                    .doc(ShortVideo)
                    .id(ShortVideo.getId().toString()),ShortVideo.class
            );
        } catch (Exception e){
            log.error("更新ES 数据失败",e);
        }
        return null;
    }

    public DeleteResponse delete(Long id)   {
        try {
            return client.delete(d -> d
                    .index(INDEX)
                    .id(id.toString())
            );
        }catch (Exception e){
            log.error("删除ES 数据失败",e);
        }
        return null;
    }
}
