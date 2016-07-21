package com.prokarma.demo.course_neo4j;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@EnableAutoConfiguration
@ComponentScan
@RestController("/")
public class CourseApplication extends WebMvcConfigurerAdapter {
    
    @Autowired
    Driver driver;

    static final String GET_MOVIE_QUERY = "MATCH (movie:Movie {title:{1}})"
            + " OPTIONAL MATCH (movie)<-[r]-(person:Person)\n"
            + " RETURN movie.title as title, collect({name:person.name, job:head(split(lower(type(r)),'_')), role:r.roles}) as cast LIMIT 1";

    @RequestMapping("/movie/{title}")
    public Map<String, Object> movie(@PathVariable("title") String title) {

        Session session = driver.session();
        StatementResult result = session.run(GET_MOVIE_QUERY, Values.parameters("1", title));
        Map<String, Object> map = result.hasNext() ? result.next().asMap() : Collections.emptyMap();
        session.close();

        return map;
    }

    public static class Movie {
        public String title;
        public int released;
        public String tagline;

        public Movie(String title, int released, String tagline) {
            this.title = title;
            this.released = released;
            this.tagline = tagline;
        }
    }

    static final String SEARCH_MOVIES_QUERY = " MATCH (movie:Movie)\n" + " WHERE movie.title =~ {1}\n"
            + " RETURN movie.title as title, movie.released as released, movie.tagline as tagline";

    @RequestMapping("/search")
    public List<Movie> search(@RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty())
            return Collections.emptyList();
        String queryParam = "(?i).*" + query + ".*";
        
        Session session = driver.session();
        StatementResult result = session.run(SEARCH_MOVIES_QUERY, Values.parameters("1", queryParam));
        List<Movie> movies = new LinkedList<>();
        while (result.hasNext()) {
            Record record = result.next();
            movies.add(new Movie(record.get("title").asString(), record.get("released").asInt(), record.get("tagline").asString()));
        }
        session.close();
        
        return movies;
    }

    public static final String GRAPH_QUERY = "MATCH (m:Movie)<-[:ACTED_IN]-(a:Person) "
            + " RETURN m.title as movie, collect(a.name) as cast " + " LIMIT {1}";

    @RequestMapping("/graph")
    public Map<String, Object> graph(@RequestParam(value = "limit", required = false) Integer limit) {
        
        Session session = driver.session();
        StatementResult res = session.run(GRAPH_QUERY, Values.parameters("1", limit == null ? 100 : limit));
        Iterator<Record> records = res.list().iterator();
        session.close();
        
        return toD3Format(records);
    }

    private Map<String, Object> toD3Format(Iterator<Record> records) {
        List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> rels = new ArrayList<Map<String, Object>>();
        int i = 0;
        while (records.hasNext()) {
            Record row = records.next();
            nodes.add(map("title", row.get("movie").asString(), "label", "movie"));
            int target = i;
            i++;
            for (Object name : (Collection) row.get("cast").asObject()) {
                Map<String, Object> actor = map("title", name, "label", "actor");
                int source = nodes.indexOf(actor);
                if (source == -1) {
                    nodes.add(actor);
                    source = i++;
                }
                rels.add(map("source", source, "target", target));
            }
        }
        return map("nodes", nodes, "links", rels);
    }

    private Map<String, Object> map(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> result = new HashMap<String, Object>(2);
        result.put(key1, value1);
        result.put(key2, value2);
        return result;
    }

    public static void main(String[] args) throws Exception {
        System.setErr(new PrintStream(System.out) {
            @Override
            public void write(int b) {
                super.write(b);
            }

            @Override
            public void write(byte[] buf, int off, int len) {
                super.write(buf, off, len);
            }
        });
        new SpringApplicationBuilder(CourseApplication.class).run(args);
    }

    @Bean
    public Driver graphDBDriver() {
        Driver driver = GraphDatabase.driver("bolt://sb10.stations.graphenedb.com:24786",
                AuthTokens.basic("demo_prokarma", "v3zbUQ9QQELYEXm8t2XM"));
        return driver;
    }

}
