package ro.andreiciortea.stn.platform.repository;

import java.util.Locale;

import org.apache.http.HttpStatus;

import com.google.gson.Gson;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import ro.andreiciortea.stn.platform.eventbus.BusMessage;
import ro.andreiciortea.stn.platform.eventbus.RepositoryRequest;
import ro.andreiciortea.stn.platform.eventbus.RepositoryResponse;
import ro.andreiciortea.stn.platform.eventbus.StnEventBus;

public class RepositoryServiceVerticle extends AbstractVerticle {

    public static final String TURTLE = "TURTLE";
    public static final String JSON_LD = "JSON-LD";
    public static final String RDF_XML = "RDF/XML";
    
    public static final String DEFAULT_REPO_ENGINE = "JenaTDB";
    
    
    public ArtifactRepository initRepository() {
        String repoEngine = DEFAULT_REPO_ENGINE;
        
        JsonObject config = vertx.getOrCreateContext().config().getJsonObject("repository");
        
        if (config != null) {
            repoEngine = config.getString("engine", DEFAULT_REPO_ENGINE);
        }
        
        ArtifactRepository repository;
        
        switch (repoEngine.toUpperCase(Locale.ROOT)) {
            case "JENATDB": repository = new JenaTDBRepository();
//            case "RDF4J" : return new Rdf4jRepositoryService(vertx);
            default: repository = new JenaTDBRepository();
        }
        
        repository.init(config);
        
        return repository;
    }
    
    @Override
    public void start() {
//        RepositoryService repository = RepositoryService.create(vertx);
        
//        ProxyHelper.registerService(RepositoryService.class, 
//                vertx, repository, RepositoryService.EVENT_BUS_ADDRESS);
        
        ArtifactRepository repo = initRepository();
        
        EventBus ebus = vertx.eventBus();
        
        ebus.consumer(StnEventBus.REPOSITORY_ADDRESS, message -> {
//            System.out.println(message.headers().get(StnMessage.HEADER_CONTENT_TYPE) + " " + message.body());
            
            String contentType = message.headers().get(BusMessage.HEADER_CONTENT_TYPE);
            
            if (contentType.equalsIgnoreCase(RepositoryRequest.CONTENT_TYPE)) {
//                System.out.println("Got repository request: " + message.body().toString());
                
                RepositoryRequest request = (new Gson()).fromJson(message.body().toString(), RepositoryRequest.class);
                
                String verb = request.getVerb();
                String artifactIri = request.getArtifactUri();
                RepositoryResponse response;
                
                
                // TODO
                
                if (verb.equalsIgnoreCase(RepositoryRequest.GET)) {
                    String artifactStr;
                    
                    try {
                        artifactStr = repo.getArtifact(artifactIri, RepositoryServiceVerticle.TURTLE);
                        response = new RepositoryResponse(HttpStatus.SC_OK, artifactIri, artifactStr);
                    } catch (ArtifactNotFoundException e) {
                        response = new RepositoryResponse(HttpStatus.SC_NOT_FOUND, artifactIri);
                    }
                    
                    message.reply(response.toJson());
                } else if (verb.equalsIgnoreCase(RepositoryRequest.POST)) {
                    try {
                        repo.createArtifact(artifactIri, request.getArtifactStr(), RepositoryServiceVerticle.TURTLE);
                        response = new RepositoryResponse(HttpStatus.SC_CREATED, artifactIri, request.getArtifactStr());
                    } catch (ArtifactRepositoryException e) {
                        response = new RepositoryResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, artifactIri);
                    }
                    
                    message.reply(response.toJson());
                }
            }
        });
    }
}
