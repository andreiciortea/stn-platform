package ro.andreiciortea.stn.platform.repository;

import java.util.Locale;

import com.google.gson.Gson;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import ro.andreiciortea.stn.platform.eventbus.RepositoryRequest;
import ro.andreiciortea.stn.platform.eventbus.RepositoryResponse;
import ro.andreiciortea.stn.platform.eventbus.StnEventBus;
import ro.andreiciortea.stn.platform.eventbus.StnMessage;
import ro.andreiciortea.stn.platform.repository.jena.tdb.JenaTDBRepository;

public class RepositoryServiceVerticle extends AbstractVerticle {

    public static final String TURTLE = "TURTLE";
    public static final String JSON_LD = "JSON-LD";
    public static final String RDF_XML = "RDF/XML";
    
    public static final String DEFAULT_REPO_ENGINE = "JenaTDB";
    
    
    public Repository initRepository() {
        String repoEngine = DEFAULT_REPO_ENGINE;
        boolean inMemory = false;
        
        JsonObject repoConfig = vertx.getOrCreateContext().config().getJsonObject("repository");
        
        if (repoConfig != null) {
            repoEngine = repoConfig.getString("engine", DEFAULT_REPO_ENGINE);
            inMemory = repoConfig.getBoolean("in-memory", false);
        }
        
        switch (repoEngine.toUpperCase(Locale.ROOT)) {
            case "JENATDB": return new JenaTDBRepository(vertx, inMemory);
//            case "RDF4J" : return new Rdf4jRepositoryService(vertx);
            default: return new JenaTDBRepository(vertx, inMemory);
        }
    }
    
    @Override
    public void start() {
//        RepositoryService repository = RepositoryService.create(vertx);
        
//        ProxyHelper.registerService(RepositoryService.class, 
//                vertx, repository, RepositoryService.EVENT_BUS_ADDRESS);
        
        Repository repo = initRepository();
        
        EventBus ebus = vertx.eventBus();
        
        ebus.consumer(StnEventBus.REPOSITORY_ADDRESS, message -> {
//            System.out.println(message.headers().get(StnMessage.HEADER_CONTENT_TYPE) + " " + message.body());
            
            String contentType = message.headers().get(StnMessage.HEADER_CONTENT_TYPE);
            
            if (contentType.equalsIgnoreCase(RepositoryRequest.CONTENT_TYPE)) {
//                System.out.println("Got repository request: " + message.body().toString());
                
                RepositoryRequest request = (new Gson()).fromJson(message.body().toString(), RepositoryRequest.class);
                
                String verb = request.getVerb();
                String artifactUri = request.getArtifactUri();
                RepositoryResponse response;
                
                if (verb.equalsIgnoreCase(RepositoryRequest.GET)) {
                    response = repo.getArtifact(artifactUri, RepositoryServiceVerticle.TURTLE);
                    
                    DeliveryOptions options = new DeliveryOptions();
                    options.addHeader(StnMessage.HEADER_CONTENT_TYPE, response.getContentType());
                    
                    message.reply(response.toJson(), options);
                } else if (verb.equalsIgnoreCase(RepositoryRequest.POST)) {
                    
                    response = repo.putArtifact(artifactUri, 
                            request.getArtifactStr(), RepositoryServiceVerticle.TURTLE);
                    
                    DeliveryOptions options = new DeliveryOptions();
                    options.addHeader(StnMessage.HEADER_CONTENT_TYPE, response.getContentType());
                    
                    message.reply(response.toJson(), options);
                }
            }
        });
    }
}
