package com.iconectiv.cscc.es;

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.ClientConfig;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Bulk;
import io.searchbox.core.Bulk.Builder;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchScroll;
import io.searchbox.params.Parameters;
import io.searchbox.params.SearchType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class ReindexData {

    public static String FROM_INDEX = System.getProperty("FROM_INDEX");
    public static String TO_INDEX = System.getProperty("TO_INDEX");
    private static final String INDEX_TYPE = System.getProperty("INDEX_TYPE");

    private int PAGE_SIZE = Integer.parseInt(System.getProperty("PAGE_SIZE", "1000"));

    private String ELASTICSEARCH_URL =
            System.getProperty(
                    "ELASTICSEARCH_URL",
                    "https://username:password@my.elasticsearch.com:443"
            );

    @Test
    public void reindex() {
        Assert.assertNotNull(ELASTICSEARCH_URL);
        Assert.assertNotNull(FROM_INDEX);
        Assert.assertNotNull(TO_INDEX);
        Assert.assertNotNull(INDEX_TYPE);

        // Configuration
        ClientConfig clientConfig =
                new ClientConfig.Builder(ELASTICSEARCH_URL)
                        .multiThreaded(false)
                        .build();
        HttpClientConfig httpConfig = new HttpClientConfig.Builder(ELASTICSEARCH_URL).multiThreaded(true).build();
        // Construct a new Jest client according to configuration via factory
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(httpConfig);
        JestClient client = factory.getObject();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        reindexData(client, searchSourceBuilder, INDEX_TYPE);

        System.out.println("************************");
    }

    private void reindexData(
            JestClient client,
            SearchSourceBuilder searchSourceBuilder,
            String type) {
        Search search = new Search.Builder(searchSourceBuilder.toString())
                .addIndex(FROM_INDEX)
                .addType(type)
                .setParameter(Parameters.SEARCH_TYPE, SearchType.SCAN)
                .setParameter(Parameters.SIZE, PAGE_SIZE)
                .setParameter(Parameters.SCROLL, "5m")
                .build();
        System.out.println(search.getData(null));
        JestResult result = handleResult(client, search);
        String scrollId = result.getJsonObject().get("_scroll_id").getAsString();

        int currentResultSize = 0;
        int pageNumber = 1;
        do {
            SearchScroll scroll = new SearchScroll.Builder(scrollId, "5m").build();
            result = handleResult(client, scroll);
            scrollId = result.getJsonObject().get("_scroll_id").getAsString();
            List hits = ((List) ((Map) result.getJsonMap().get("hits")).get("hits"));
            currentResultSize = hits.size();
            System.out.println("finished scrolling page # " + pageNumber++ + " which had " + currentResultSize + " results.");

            Builder bulkIndexBuilder = new Bulk.Builder()
                    .defaultIndex(TO_INDEX)
                    .defaultType(type);
            boolean somethingToIndex = false;
            for (int i = 0; i < currentResultSize; i++) {
                Map source = ((Map) ((Map) hits.get(i)).get("_source"));
                String sourceId = ((String) ((Map) hits.get(i)).get("_id"));
                System.out.println("adding " + sourceId + " for bulk indexing");

                // TODO: we could transform the source if we wanted to here,
                //       before adding it to the bulk index queue

                Index index = new Index.Builder(source)
                        .index(TO_INDEX)
                        .type(type)
                        .id(sourceId)
                        .build();
                bulkIndexBuilder = bulkIndexBuilder.addAction(index);
                somethingToIndex = true;
            }
            if (somethingToIndex) {
                Bulk bulk = bulkIndexBuilder.build();
                //System.out.println(bulk.getData(null));
                handleResult(client, bulk);
            } else {
                System.out.println("there weren't any results to index in this set/page");
            }
        } while (currentResultSize == PAGE_SIZE);
    }

    protected JestResult handleResult(JestClient client, Action action) {
        JestResult result = null;
        try {
            result = client.execute(action);
            if (result.isSucceeded()) {
                System.out.println(result.getJsonString());
                //List hits = ((List) ((Map) result.getJsonMap().get("hits")).get("hits"));
                //System.out.println("hits.size(): " + hits.size());
            } else {
                System.out.println(result.getErrorMessage());
                System.out.println(result.getJsonString());
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return result;
    }
}