package ren.superk.zhihu.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQueryBuilder;
import org.springframework.stereotype.Service;
import ren.superk.zhihu.core.ZhihuEnum;
import ren.superk.zhihu.model.People;
import ren.superk.zhihu.model.Relation;
import ren.superk.zhihu.model.ZhihuPager;
import ren.superk.zhihu.repository.PeopleRepository;
import ren.superk.zhihu.repository.RelationRepository;
import ren.superk.zhihu.service.PeopleService;
import ren.superk.zhihu.service.PeopleUrlService;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class PeopleServiceImpl implements PeopleService {
    static Logger logger = LoggerFactory.getLogger(PeopleServiceImpl.class);

    public static ConcurrentHashMap<String,Relation> relationMap = new ConcurrentHashMap<>();
    static ArrayBlockingQueue<People> queryQueue = new ArrayBlockingQueue<People>(10, false);
    @Autowired
    private PeopleRepository peopleRepository;
    @Autowired
    private RelationRepository relationRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private PeopleUrlService peopleUrlService;

    @Override
    public void builkupsert(List<Map> list, ZhihuEnum type) {

        ArrayList<UpdateQuery> queries = new ArrayList<>();

        for (Map people : list) {
            try {
                 queries.add(new UpdateQueryBuilder()
                         .withId(people.get(type.getPk()).toString())
                         .withType(type.getType())
                         .withIndexName(type.getIndex())
                         .withClass(Map.class).withDoUpsert(true)
                         .withIndexRequest(new IndexRequest()
                                            .source(people)
                                            .index(type.getIndex())
                                            .type(type.getType())
                                            .id(people.get(type.getPk()).toString()))
                         .build()
                 );
                 logger.info("更新数据index = " +type.getIndex() + " type = " + type.getType() + " id = " + people.get(type.getPk()).toString());
            }catch (RuntimeException e){
                e.printStackTrace();
                logger.error(e.getLocalizedMessage());
            }
        }
        if (! queries.isEmpty())
        elasticsearchTemplate.bulkUpdate(queries);

    }
    public void builkupsertRelation(Relation relation) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String value = objectMapper.writeValueAsString(relation);
            ArrayList<UpdateQuery> queries = new ArrayList<>();
            UpdateQuery build = new UpdateQueryBuilder()
                    .withId(relation.getUrl_token())
                    .withType("relation")
                    .withIndexName("relation")
                    .withClass(Map.class).withDoUpsert(true)
                    .withIndexRequest(new IndexRequest()
                            .source(value, XContentType.JSON)
                            .index("relation")
                            .type("relation")
                            .id(relation.getUrl_token()))
                    .build();
            logger.info("更新数据index = relation, type = relation, id = " + relation.getUrl_token() + " ,  name = " + relation.getName() + " , count = " + relation.getFrom());
            elasticsearchTemplate.update(build);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


    }

    @Override
    public ConcurrentHashMap<String, Relation> getAllRelations() {
        Iterator<Relation> iterator = relationRepository.findAll().iterator();
        while (iterator.hasNext()){
            Relation relation = iterator.next();
            relationMap.put(relation.getUrl_token(),relation);
            logger.info("已经处理的数据 url_token = " + relation.getUrl_token() + " name = " + relation.getName() + " count = " + relation.getFrom());
        }
        return relationMap;
    }

    @Override
    public void initDataByThreadCount(int count) {
        ConcurrentHashMap<String, Relation> relations = getAllRelations();
        Random random = new Random();
        setQueryQueue(1);
        ExecutorService pool = Executors.newFixedThreadPool(count);
        for (int i = 0; i < count; i++) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        People poll = queryQueue.poll();
                        int checkSaveCompleted = checkSaveCompleted(poll);
                        if(checkSaveCompleted != 1){
                            if(checkSaveCompleted == 0){
                                String type = null;
                                int from  = 0;
                                Relation relation = relationMap.get(poll.getUrl_token());
                                type = relation.getName();
                                from =relation.getFrom();
                                boolean running = false;
                                for (ZhihuEnum zhihuEnum : ZhihuEnum.values()) {
                                    if(zhihuEnum.getValue().equals(type)){
                                        running = true;
                                        queryUrlAndSave(from,poll.getUrl_token(),zhihuEnum);
                                        continue;
                                    }
                                    if(running){
                                        queryUrlAndSave(0,poll.getUrl_token(),zhihuEnum);
                                    }
                                }
                            }else{
                                for (ZhihuEnum zhihuEnum : ZhihuEnum.values()) {
                                    queryUrlAndSave(0,poll.getUrl_token(),zhihuEnum);
                                }
                            }


                        }
                    }

                }
            });
        }
    }

    private void queryUrlAndSave(int from , String url_token,ZhihuEnum type){
        List listre = new ArrayList<>();
        try {
            listre = peopleUrlService.findList(url_token, from, 20, type, ZhihuPager.class).getData();
        }catch (RuntimeException e){
            e.printStackTrace();
        }
        List<Map> list = new ArrayList<>();
        for (Object o : listre) {
            Map map = new HashMap<>();
            if(o instanceof List){
                map = (HashMap<Object, Object>) ((List) o).get(0);
            }else if (o instanceof Map){
                map = (Map) o;
            }
            list.add(map);
        }
        builkupsert(list,type);

        if(!list.isEmpty())
        saveRelation(url_token,type,from + list.size());

        if(type == ZhihuEnum.FOLLOWEES){
            for (Map map : list) {
                if(!relationMap.contains(map.get("url_token"))) {
                    People people = new People();
                    BeanUtils.copyProperties(map,people);
                    boolean offer = queryQueue.offer(people);
                    if(offer){
                        logger.info(map.get("url_token")+"加入处理队列队列");
                    }
                }
            }

        }

        if(list.size() == 20){
            queryUrlAndSave(from + 20,url_token,type);
        }

    }
    private void saveRelation(String url_token, ZhihuEnum type, int count){
        Relation relation = new Relation();
        relation.setUrl_token(url_token);
        relation.setFrom(count);
        relation.setName(type.getValue());
        builkupsertRelation(relation);
        relationMap.put(url_token,relation);

    }
    private int checkSaveCompleted(People people){
        Relation relation = relationMap.get(people.getUrl_token());
            if(relation != null){
                if(ZhihuEnum.FOLLOWERS.getValue().equals(relation.getName()) && people.getFollower_count().equals(relation.getFrom())){
                        return  1;
                }else {
                    return 0;
                }
            }
        return -1;
    }
    /**
     * 设置到处理队列
     * @param page
     */
    private void setQueryQueue(int page){
        Page<People> list = peopleRepository.findAll(PageRequest.of(page, 20, Sort.by("follower_count").descending()));
        Iterator<People> peopleIterator = list.iterator();
        while (peopleIterator.hasNext()){
            People next = peopleIterator.next();
            String url_token = next.getUrl_token();
            if(!relationMap.contains(url_token)) {
                queryQueue.offer(next);
                logger.info("url_token : " + url_token + " 加入处理队列");
            }
        }
        if(queryQueue.size() < 6){
            setQueryQueue(page+1);
        }
    }
}
