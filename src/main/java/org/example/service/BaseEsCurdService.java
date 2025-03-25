package org.example.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class BaseEsCurdService {


    @Autowired
    ElasticsearchClient client;



    public <T extends Identifiable> void insert(T  t)  {
        try {
              client.index(i -> i
                    .index(t.index())
                    .id(t.getId().toString())
                    .document(t)
              );
        }catch (Exception e){
            log.error("新增ES 数据失败",e);
        }
    }


    public <T extends Identifiable>  void  update(T  t)  {
        try {
              client.update(g -> g
                    .index(t.index())
                    .doc(t)
                    .id(t.getId().toString()),t.getClass()
            );
        } catch (Exception e){
            log.error("更新ES 数据失败",e);
        }
    }

    public void delete(Long id,String INDEX)   {
        try {
              client.delete(d -> d
                    .index(INDEX)
                    .id(id.toString())
            );
        }catch (Exception e){
            log.error("删除ES 数据失败",e);
        }
    }




}
