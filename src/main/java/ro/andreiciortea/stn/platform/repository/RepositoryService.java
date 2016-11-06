package ro.andreiciortea.stn.platform.repository;

import java.util.Locale;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;
import ro.andreiciortea.stn.platform.repository.jena.tdb.JenaTDBRepositoryService;
import ro.andreiciortea.stn.platform.repository.rdf4j.Rdf4jRepositoryService;


@ProxyGen
@VertxGen
public interface RepositoryService {
    
    public static final String TURTLE = "TURTLE";
    public static final String JSON_LD = "JSON-LD";
    public static final String RDF_XML = "RDF/XML";
    
    public static final String EVENT_BUS_ADDRESS = "hub.service.repository";
    
    public static final String DEFAULT_REPO_ENGINE = "JenaTDB";
    
    
    static RepositoryService create(Vertx vertx) {
        String repoEngine = DEFAULT_REPO_ENGINE;
        boolean inMemory = false;
        
        JsonObject repoConfig = vertx.getOrCreateContext().config().getJsonObject("repository");
        
        if (repoConfig != null) {
            repoEngine = repoConfig.getString("engine", DEFAULT_REPO_ENGINE);
            inMemory = repoConfig.getBoolean("in-memory", false);
        }
        
        switch (repoEngine.toUpperCase(Locale.ROOT)) {
            case "JENATDB": return new JenaTDBRepositoryService(vertx, inMemory);
            case "RDF4J" : return new Rdf4jRepositoryService(vertx);
            default: return new JenaTDBRepositoryService(vertx, inMemory);
        }
    }
    
    static RepositoryService createProxy(Vertx vertx, String address) {
        return ProxyHelper.createProxy(RepositoryService.class, vertx, address);
    }
    
    void containsArtifact(String uri, Handler<AsyncResult<Boolean>> result);
    
//    void getArtifact(String uri, Handler<AsyncResult<DigitalArtifactDeprecated>> result);
    
    void getArtifactAsString(String uri, String format, Handler<AsyncResult<String>> result);
    
    void putArtifact(String artifactUri, String rdfData, String format, Handler<AsyncResult<Void>> result);
    
    void deleteArtifact(String uri, Handler<AsyncResult<Boolean>> result);
    
    void runSelectQuery(String queryString, Handler<AsyncResult<String>> result);
    
    void runConstructQuery(String queryString, String format, Handler<AsyncResult<String>> result);
    
}
