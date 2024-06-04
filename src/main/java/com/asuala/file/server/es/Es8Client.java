package com.asuala.file.server.es;

import cn.hutool.core.util.IdUtil;
import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.cat.aliases.AliasesRecord;
import co.elastic.clients.elasticsearch.cat.indices.IndicesRecord;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.Alias;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.util.ObjectBuilder;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.asuala.file.server.es.annotation.DocId;
import com.asuala.file.server.es.annotation.EsClass;
import com.asuala.file.server.es.annotation.EsField;
import com.asuala.file.server.es.entity.FileInfoEs;
import com.asuala.file.server.es.entity.TimeValue;
import com.asuala.file.server.es.enums.EsDataType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description:
 * @create: 2022/09/18
 **/
@ConditionalOnProperty(prefix = "file", name = "server.open", havingValue = "true")
@Component
@Slf4j
public class Es8Client {
    @Value("${es.port}")
    private int prot;

    @Value("${es.hostname}")
    private String hostname;

    @Value("${es.alals}")
    private boolean alals;

    @Value("${es.indexPrefix}")
    private String indexPrefix;

    @Value("${es.aliasPrefix}")
    private String aliasPrefix;

    private ElasticsearchClient client;
    private ElasticsearchAsyncClient asyncClient;


    // 同步客户端
    public ElasticsearchClient getClient() {
        return client;
    }

    // 异步客户端
    public ElasticsearchAsyncClient getAsyncClient() {
        return asyncClient;
    }

    public void afterPropertiesSet() throws Exception {
        /**
         restClient = RestClient.builder(
         new HttpHost(GSConstants.P_ES_HOST, GSConstants.P_ES_PORT, "http"))
         .setMaxRetryTimeoutMillis(TIMEOUT)
         .setHttpClientConfigCallback(new HttpClientConfigCallback(){
        @Override public HttpAsyncClientBuilder customizeHttpClient(
        HttpAsyncClientBuilder httpClientBuilder) {
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
        .setConnectTimeout(5*60*1000)//超时时间5分钟
        .setSocketTimeout(5*60*1000)//这就是Socket超时时间设置
        .setConnectionRequestTimeout(DEFAULT_CONNECTION_REQUEST_TIMEOUT_MILLIS);
        httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
        return httpClientBuilder;
        }
        }).build();
         **/
        RestClient restClient = RestClient.builder(new HttpHost(hostname, prot)).build();
        ElasticsearchTransport transport =
                new RestClientTransport(restClient, new JacksonJsonpMapper());
        // es 客户端
        this.client = new ElasticsearchClient(transport);
        this.asyncClient = new ElasticsearchAsyncClient(transport);
    }

    /**
     * 如果索引或者别名已经存在,那么就不会在创建了
     *
     * @return 是否成功
     * @throws Exception
     */
    public <T> boolean createIndexSettingsMappings(Class<T> tClass) throws Exception {
        afterPropertiesSet();

        EsClass annotation = tClass.getAnnotation(EsClass.class);
        String index = getClassIndex(tClass);
        String alias = getClassAlias(tClass);

        List<String> indexs = indexs();
        List<String> aliases = aliases();
        if (indexs.contains(index) || aliases.contains(alias)) {
            log.info("索引已存在 {}", tClass);
            return false;
        }
        int shards = annotation.shards();
        int replicas = annotation.replicas();
        StringBuilder stringBuilder = new StringBuilder("{");
        /**
         {
         "settings": {
         "analysis": {
         "analyzer": {
         "ngram_analyzer": {
         "tokenizer": "ngram_tokenizer",
         "filter": ["lowercase"]
         }
         }
         }
         }
         }
         **/
        stringBuilder.append("\"settings\": {\"index.max_ngram_diff\": 10,\"number_of_shards\": "
                + shards
                + ", \"number_of_replicas\": "
                + replicas + ",\"analysis\": {\"tokenizer\": {\"ngram_tokenizer\": {\"type\": \"ngram\",\"min_gram\": 1,\"max_gram\": 10,\"token_chars\": [\"letter\",\"digit\"]}},\"analyzer\": {\"ngram_analyzer\": { \"type\": \"custom\",\"tokenizer\": \"ngram_tokenizer\",\"filter\":[\"lowercase\"]}},}},");
        stringBuilder.append("\"mappings\": {  \"properties\": ");
        JSONObject jsonObject = new JSONObject();
        for (Field declaredField : tClass.getDeclaredFields()) {
            declaredField.setAccessible(true);
            JSONObject jsonObject1 = new JSONObject();
            DocId DocId = declaredField.getAnnotation(DocId.class);
            if (DocId != null) {
                jsonObject1.put("type", EsDataType.LONG.getType());
                jsonObject.put(declaredField.getName(), jsonObject1);
                continue;
            }

            EsField annotation1 = declaredField.getAnnotation(EsField.class);
            if (annotation1 != null) {
                String name = annotation1.name();
                name = "".equals(name) ? declaredField.getName() : name;
                EsDataType type = annotation1.type();
                String analyzer = annotation1.analyzer();
                String searchAnalyzer = annotation1.searchAnalyzer();
                jsonObject1.put("type", type.getType());
                if (!"".equals(analyzer)) {
                    jsonObject1.put("analyzer", analyzer);
                }
                if (!"".equals(searchAnalyzer)) {
                    jsonObject1.put("search_analyzer", searchAnalyzer);
                }
                jsonObject.put(name, jsonObject1);
            }
        }
        Assert.isTrue(jsonObject.size() > 0, "请添加es相关注解");
        stringBuilder.append(jsonObject);
        stringBuilder.append("}}");
        Reader queryJson = new StringReader(JSONObject.parseObject(stringBuilder.toString()).toJSONString());


        String finalIndex = index;
        String finalAlias = alias;
        CreateIndexRequest req =
                CreateIndexRequest.of(
                        b ->
                                b.index(finalIndex)
                                        .aliases(finalAlias, Alias.of(a -> a.isWriteIndex(true)))
                                        .withJson(queryJson));

        return client.indices().create(req).acknowledged();
    }

    // 查询全部索引
    public List<String> indexs() {
        List<IndicesRecord> indices = null;
        try {
            indices = client.cat().indices().valueBody();

        } catch (IOException e) {
            e.printStackTrace();
        }
        assert indices != null;
        return indices.stream().map(IndicesRecord::index).collect(Collectors.toList());
    }

    // 查询全部别名
    public List<String> aliases() {
        List<AliasesRecord> aliasesRecords = null;
        try {
            aliasesRecords = client.cat().aliases().valueBody();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert aliasesRecords != null;
        return aliasesRecords.stream().map(AliasesRecord::alias).collect(Collectors.toList());
    }


    //查询全部数据
    public <T> List<T> queryAll(Class<T> tClass) throws IOException {
        List<T> list = new ArrayList<>();
        String index = getClassAlalsOrIndex(tClass);
        SearchResponse<T> search = client.search(s -> s.index(index).query(q -> q.matchAll(m -> m)), tClass);
        for (Hit<T> hit : search.hits().hits()) {
            list.add(hit.source());
        }
        return list;
    }

    public <T> Map<String, Object> complexQuery(Query query, Class<T> clazz, int pageNum, int pageSize) throws IOException {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        String index = getClassAlalsOrIndex(clazz);

        SearchResponse<T> response = client.search(s -> s
                        .index(index)
                        .query(query)
                        .size(pageSize)
                        .from(pageNum - 1)
                , clazz
        );
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", response.hits().total().value());
        return result;
    }


    // 添加数据

    /**
     * @param o     数据源
     * @param async 是否异步 true异步 ,false同步 ,如果是异步那么永远返回null
     * @return
     */
    public <T> String addData(T o, boolean async) {
        Object id = null;
        for (Field declaredField : o.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            DocId annotation = declaredField.getAnnotation(DocId.class);
            if (annotation != null) {
                try {
                    id = declaredField.get(o);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if (id == null) {
            id = IdUtil.simpleUUID();
        }
        IndexResponse response = null;

        try {
            IndexRequest.Builder<T> indexReqBuilder = new IndexRequest.Builder<>();
            indexReqBuilder.index(getClassAlias(o.getClass()));
            indexReqBuilder.id(String.valueOf(id));
            indexReqBuilder.document(o);
            if (async) {
                asyncClient.index(indexReqBuilder.build());
                return null;
            }
            response = client.index(indexReqBuilder.build());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert response != null;
        return response.id();
    }    // 添加数据

    /**
     * @param o     数据源
     * @param async 是否异步 true异步 ,false同步 ,如果是异步那么永远返回null
     * @return
     */
    public <T> String updateDataInsert(T o, boolean async) {
        Object id = null;
        for (Field declaredField : o.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            DocId annotation = declaredField.getAnnotation(DocId.class);
            if (annotation != null) {
                try {
                    id = declaredField.get(o);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if (id == null) {
            id = IdUtil.simpleUUID();
        }
        UpdateResponse response = null;

        try {
//            IndexRequest.Builder<T> indexReqBuilder = new IndexRequest.Builder<>();
//            indexReqBuilder.index(getClassAlias(o.getClass()));
//            indexReqBuilder.id(String.valueOf(id));
//            indexReqBuilder.document(o);

            UpdateRequest.Builder<FileInfoEs, Object> builder = new UpdateRequest.Builder<>();
            builder.index(getClassAlias(o.getClass()));
            builder.id(String.valueOf(id));
            builder.doc(o);
            builder.upsert((FileInfoEs) o);
            if (async) {
                asyncClient.update(builder.build(), FileInfoEs.class);
                return null;
            }
            response = client.update(builder.build(), FileInfoEs.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert response != null;
        return response.id();
    }

    //查询指定文档id数据是否存在
    public <T> boolean docIdexists(Class<T> tClass, String id) throws IOException {
        return client.exists(s -> s.index(getClassAlias(tClass)).id(id)).value();
    }


    // 批量添加
    public <T> void addData(List<T> list, boolean async) {

        BulkRequest.Builder br = new BulkRequest.Builder();
        for (T o : list) {
            Object id = null;
            for (Field declaredField : o.getClass().getDeclaredFields()) {
                declaredField.setAccessible(true);
                DocId annotation = declaredField.getAnnotation(DocId.class);
                if (annotation != null) {
                    try {
                        id = declaredField.get(o);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (id == null) {
                id = IdUtil.simpleUUID();
            }
            Object finalId = id;
            br.operations(
                    op ->
                            op.index(
                                    idx ->
                                            idx.index(getClassAlias(o.getClass())).id(String.valueOf(finalId)).document(o)));
        }

        if (async) {
            asyncClient.bulk(br.build());
            return;
        }
        BulkResponse result = null;
        try {
            result = client.bulk(br.build());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Log errors, if any
        assert result != null;
        if (result.errors()) {
            log.error("Bulk had errors");
            for (BulkResponseItem item : result.items()) {
                if (item.error() != null) {
                    log.error(item.error().reason());
                }
            }
        }
    }

    public <T> T getDocId(String DocId, Class<T> clazz) {
        GetResponse<T> response = null;
        try {

            response = client.get(g -> g.index(getClassAlias(clazz)).id(DocId), clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert response != null;
        return response.source();
    }

    public <T> List<T> complexQuery(Query query, Class<T> clazz) throws IOException {
        List<T> list = new ArrayList<>();

        SearchResponse<T> response =
                client.search(s -> s.index(getClassAlias(clazz)).query(query), clazz);
        List<Hit<T>> hits = response.hits().hits();
        for (Hit<T> hit : hits) {
            list.add(hit.source());
        }
        return list;
    }

    public <T> Aggregate complexQueryAggregations(
            Query query, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, Class<T> clazz)
            throws IOException {

        SearchResponse<T> response =
                client.search(
                        s ->
                                s.index(getClassAlias(clazz))
                                        .size(0) // 不需要显示数据 ,只想要聚合结果
                                        .query(query)
                                        .aggregations("aggregations", fn),
                        clazz);

        return response.aggregations().get("aggregations");
    }

    public <T> Map<String, Object> complexQueryHighlight(Query query, Class<T> clazz, List<String> fields, int pageNum, int pageSize) throws IOException {


        String index = getClassAlalsOrIndex(clazz);
        Highlight of = Highlight.of(h -> {
                    for (String field : fields) {
                        h.fields(
                                field
                                ,
                                h1 -> h1.preTags("<font color='red'>").postTags("</font>"));
                    }
                    return h;
                }
        );
        SearchResponse<T> response = client.search(s -> s
                        .index(index)
                        .query(query)
                        .highlight(of)
                        .size(pageSize)
                        .from((pageNum - 1) * pageSize)
                , clazz
        );

        List<Object> list = new ArrayList<>();

        for (Hit<T> hit : response.hits().hits()) {
            FileInfoEs source = ((FileInfoEs) hit.source());
            source.setName(hit.highlight().get("name").get(0));
            list.add(source);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", response.hits().total().value());
        return result;
    }

    //根据id进行删除
    public <T> void delDocId(String docId, Class<T> clazz) {
        String index = getClassAlalsOrIndex(clazz);
        DeleteRequest de = DeleteRequest.of(d -> d.index(index).id(docId));
        asyncClient.delete(de);
    }

    //根据查询进行删除
    public <T> void delByIds(Query query, Class<T> clazz) {
        String index = getClassAlalsOrIndex(clazz);
        DeleteByQueryRequest de = DeleteByQueryRequest.of(d -> d.index(index).query(query));
        asyncClient.deleteByQuery(de);
    }

    public <T> void delQuery(Query query, Class<T> clazz) throws IOException {
        String index = getClassAlalsOrIndex(clazz);
        DeleteByQueryRequest de = DeleteByQueryRequest.of(d -> d.index(index).query(query).timeout(Time.of(a -> a.time(TimeValue.timeValueMinutes(2).toString()))));
        client.deleteByQuery(de);
    }

    public <T> void delAll(Class<T> clazz) throws IOException {
        String index = getClassAlalsOrIndex(clazz);
        // 执行删除操作
        DeleteByQueryRequest de = DeleteByQueryRequest.of(d -> d.index(index));
        client.deleteByQuery(de);
    }

    public <T> void upDocId(String docId, T o, boolean async) throws Exception {
        //先查询出来
        Object docId1 = getDocId(docId, o.getClass());
        if (docId1 == null) {
            throw new Exception("没有docId:" + docId + "这条数据");
        }
        //进行对象拷贝
        BeanUtils.copyProperties(docId1, o);
        String index = getClassAlalsOrIndex(o.getClass());
        if (async) {
            asyncClient.update(UpdateRequest.of(d -> d.index(index).id(docId).doc(o)), o.getClass());
            return;
        }
        client.update(UpdateRequest.of(d -> d.index(index).id(docId).doc(o)), o.getClass());

    }

    public <T> void upQuery(Query query, T o, boolean async) throws Exception {
        Class<?> aClass = o.getClass();
        String index = getClassAlalsOrIndex(aClass);
        //获取全部字段和字段的名称以及需要修改的值
        StringBuilder stringBuilder = new StringBuilder();
        for (Field declaredField : aClass.getDeclaredFields()) {
            declaredField.setAccessible(true);
            Object o1 = declaredField.get(o);
            if (o1 == null) {
                continue;
            }
            declaredField.setAccessible(true);
            EsField field = declaredField.getAnnotation(EsField.class);
            String name = field.name();
            name = "".equals(name) ? declaredField.getName() : name;
            String finalName = name;
            String str = MessageFormat.format("ctx._source[''{0}''] = ''{1}'';", finalName, o1);
            stringBuilder.append(str);
        }

        UpdateByQueryRequest of = UpdateByQueryRequest.of(u -> u.index(index).query(query).script(s -> s.inline(i -> i.source(stringBuilder.toString()))));
        if (async) {
            asyncClient.updateByQuery(of);
            return;
        }
        client.updateByQuery(of);
    }

    /**
     * 使用索引还是使用别名
     *
     * @param clazz
     * @param <T>
     * @return
     */
    private <T> String getClassAlalsOrIndex(Class<T> clazz) {
        if (alals) {
            return getClassAlias(clazz);
        }
        return getClassIndex(clazz);

    }

    //获取类的索引名称(没有指定默认类名首字母小写,  前缀+索引)
    private <T> String getClassIndex(Class<T> clazz) {
        EsClass annotation = clazz.getAnnotation(EsClass.class);
        String index = annotation.index();
        index =
                "".equals(index)
                        ? Objects.requireNonNull(clazz.getName()).toLowerCase()
                        : index.toLowerCase();
        return indexPrefix + index;
    }

    private <T> String getClassAlias(Class<T> clazz) {
        EsClass annotation = clazz.getAnnotation(EsClass.class);
        String alias = annotation.alias();
        alias =
                "".equals(alias)
                        ? Objects.requireNonNull(clazz.getName()).toLowerCase()
                        : alias.toLowerCase();
        return aliasPrefix + alias;
    }

    //获取对象的文档id
    public <T> Long getObjId(T o) {
        Long id = null;
        for (Field declaredField : o.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            DocId annotation = declaredField.getAnnotation(DocId.class);
            if (annotation != null) {
                try {
                    id = (Long) declaredField.get(o);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return id;
    }
}