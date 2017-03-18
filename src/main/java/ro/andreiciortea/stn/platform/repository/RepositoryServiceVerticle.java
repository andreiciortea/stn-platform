package ro.andreiciortea.stn.platform.repository;

import java.util.Locale;

import org.apache.http.HttpStatus;

import com.google.gson.Gson;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import ro.andreiciortea.stn.platform.RdfUtils;
import ro.andreiciortea.stn.platform.eventbus.ArtifactRequest;
import ro.andreiciortea.stn.platform.eventbus.ArtifactResponse;


public class RepositoryServiceVerticle extends AbstractVerticle {

    public static final String REPOSITORY_BUS_ADDRESS = "ro.andreiciortea.stn.eventbus.repository";
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
        EventBus eventBus = vertx.eventBus();
        
        eventBus.consumer(REPOSITORY_BUS_ADDRESS, message -> {
            ArtifactRequest request = (new Gson()).fromJson(message.body().toString(), ArtifactRequest.class);
            
            String verb = request.getVerb();
            String artifactIri = request.getArtifactIri();
            ArtifactResponse response;
            
            try {
                
                if (verb.equalsIgnoreCase(ArtifactRequest.GET)) {
                    String artifactStr = repo.getArtifact(artifactIri, RdfUtils.TURTLE);
                    response = new ArtifactResponse(HttpStatus.SC_OK, artifactIri, artifactStr);
                } else if (verb.equalsIgnoreCase(ArtifactRequest.POST)) {
                    repo.createArtifact(artifactIri, request.getArtifactAsString(), RdfUtils.TURTLE);
                    response = new ArtifactResponse(HttpStatus.SC_CREATED, artifactIri, request.getArtifactAsString());
                } else if (verb.equalsIgnoreCase(ArtifactRequest.PUT)) {
                    repo.updateArtifact(artifactIri, request.getArtifactAsString(), RdfUtils.TURTLE);
                    response = new ArtifactResponse(HttpStatus.SC_OK, artifactIri, request.getArtifactAsString());
                } else if (verb.equalsIgnoreCase(ArtifactRequest.DELETE)) {
                    repo.deleteArtifact(artifactIri);
                    response = new ArtifactResponse(HttpStatus.SC_OK, artifactIri, request.getArtifactAsString());
                } else {
                    response = new ArtifactResponse(HttpStatus.SC_BAD_REQUEST, artifactIri); 
                }
                
            } catch (ArtifactNotFoundException e) {
                response = new ArtifactResponse(HttpStatus.SC_NOT_FOUND, artifactIri);
            } catch (Exception e) {
                response = new ArtifactResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, artifactIri);
            }
            
            message.reply(response.toJson());
        });
    }
    
}
