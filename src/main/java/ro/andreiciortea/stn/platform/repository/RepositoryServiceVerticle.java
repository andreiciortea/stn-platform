package ro.andreiciortea.stn.platform.repository;

import java.util.Locale;

import org.apache.http.HttpStatus;

import com.google.gson.Gson;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import ro.andreiciortea.stn.platform.RdfUtils;
import ro.andreiciortea.stn.platform.eventbus.RepositoryRequest;
import ro.andreiciortea.stn.platform.eventbus.RepositoryResponse;
import ro.andreiciortea.stn.platform.eventbus.StnEventBus;

public class RepositoryServiceVerticle extends AbstractVerticle {
    
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
            default: repository = new JenaTDBRepository();
        }
        
        repository.init(config);
        
        return repository;
    }
    
    @Override
    public void start() {
        ArtifactRepository repo = initRepository();
        EventBus ebus = vertx.eventBus();
        
        ebus.consumer(StnEventBus.REPOSITORY_ADDRESS, message -> {
            RepositoryRequest request = (new Gson()).fromJson(message.body().toString(), RepositoryRequest.class);
            
            String verb = request.getVerb();
            String artifactIri = request.getArtifactUri();
            RepositoryResponse response;
            
            try {
                
                if (verb.equalsIgnoreCase(RepositoryRequest.GET)) {
                    String artifactStr = repo.getArtifact(artifactIri, RdfUtils.TURTLE);
                    response = new RepositoryResponse(HttpStatus.SC_OK, artifactIri, artifactStr);
                } else if (verb.equalsIgnoreCase(RepositoryRequest.POST)) {
                    repo.createArtifact(artifactIri, request.getArtifactStr(), RdfUtils.TURTLE);
                    response = new RepositoryResponse(HttpStatus.SC_CREATED, artifactIri, request.getArtifactStr());
                } else if (verb.equalsIgnoreCase(RepositoryRequest.PUT)) {
                    repo.updateArtifact(artifactIri, request.getArtifactStr(), RdfUtils.TURTLE);
                    response = new RepositoryResponse(HttpStatus.SC_OK, artifactIri, request.getArtifactStr());
                } else if (verb.equalsIgnoreCase(RepositoryRequest.DELETE)) {
                    repo.deleteArtifact(artifactIri);
                    response = new RepositoryResponse(HttpStatus.SC_OK, artifactIri, request.getArtifactStr());
                } else {
                    response = new RepositoryResponse(HttpStatus.SC_BAD_REQUEST, artifactIri); 
                }
                
            } catch (ArtifactNotFoundException e) {
                response = new RepositoryResponse(HttpStatus.SC_NOT_FOUND, artifactIri);
            } catch (Exception e) {
                response = new RepositoryResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, artifactIri);
            }
            
            message.reply(response.toJson());
        });
    }
    
}
