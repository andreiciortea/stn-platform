package ro.andreiciortea.stn.platform.api;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;

import com.google.gson.Gson;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import ro.andreiciortea.stn.platform.RdfUtils;
import ro.andreiciortea.stn.platform.eventbus.RepositoryRequest;
import ro.andreiciortea.stn.platform.eventbus.RepositoryResponse;
import ro.andreiciortea.stn.platform.eventbus.StnEventBus;


@SuppressWarnings("serial")
class InvalidArtifactRepresentationException extends Exception {}


public abstract class ArtifactHandler {
    
    public static final String CONTENT_TYPE_TURTLE = "text/turtle"; 
    
    private boolean persistent;
    private boolean observable; // TODO
    
    
    public ArtifactHandler withoutStorage() {
        persistent = false;
        return this;
    }
    
    public ArtifactHandler withObserve() {
        observable = true;
        return this;
    }
    
    public void handleGetArtifact(RoutingContext routingContext) {
        String artifactUri = routingContext.request().absoluteURI();
        
        RepositoryRequest repositoryRequest = new RepositoryRequest(RepositoryRequest.GET, artifactUri);
        
        sendRepositoryRequest(repositoryRequest,
                r -> {
                    if (r.succeeded()) {
                        RepositoryResponse response = (new Gson()).fromJson(r.result().body(), RepositoryResponse.class);
                        
                        if (response.getStatusCode() == RepositoryResponse.SC_OK) {
                            routingContext
                                .response()
                                .putHeader(HttpHeaders.CONTENT_TYPE, ArtifactHandler.CONTENT_TYPE_TURTLE)
                                .end(response.getArtifactAsString());
                        } else {
                            routingContext.fail(response.getStatusCode());
                        }
                    } else {
                        routingContext.fail(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    }
                });
    }
    
    public void handlePutArtifact(RoutingContext routingContext) {
        String artifactUri = routingContext.request().absoluteURI();
        
        try {
            String artifactStr = replaceNullRealtiveURIs(artifactUri, routingContext.getBodyAsString(), RdfUtils.TURTLE);
            artifactStr = processArtifactRepresentation(artifactUri, artifactStr, RdfUtils.TURTLE);
            
            if (persistent) {
                persistArtifact(routingContext, RepositoryRequest.PUT, artifactUri, "", HttpStatus.SC_OK);
            }
        } catch (InvalidArtifactRepresentationException e) {
            routingContext.fail(HttpStatus.SC_BAD_REQUEST);
        }
    }
    
    public void handlePostArtifact(RoutingContext routingContext) {
        String containerUri = routingContext.request().absoluteURI();
        
        String slug = routingContext.request().getHeader("Slug");
        
        try {
            String artifactUri = ArtifactUriGenerator.generateArtifactUri(containerUri, slug);
            String artifactStr = replaceNullRealtiveURIs(artifactUri, routingContext.getBodyAsString(), RdfUtils.TURTLE);
            
            artifactStr = processArtifactRepresentation(artifactUri, artifactStr, RdfUtils.TURTLE);
            
            if (persistent) {
                persistArtifact(routingContext, RepositoryRequest.POST, artifactUri, artifactStr, HttpStatus.SC_CREATED);
            }
        } catch (InvalidContainerUriException e) {
            routingContext.fail(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        } catch (InvalidArtifactRepresentationException e) {
            routingContext.fail(HttpStatus.SC_BAD_REQUEST);
        }
    }
    
    public void handleDeleteArtifact(RoutingContext routingContext) {
        String artifactUri = routingContext.request().absoluteURI();
        
        RepositoryRequest repositoryRequest = new RepositoryRequest(RepositoryRequest.DELETE, artifactUri);
        
        sendRepositoryRequest(repositoryRequest,
                r -> {
                    if (r.succeeded()) {
                        RepositoryResponse response = (new Gson()).fromJson(r.result().body(), RepositoryResponse.class);
                        
                        if (response.getStatusCode() == HttpStatus.SC_OK) {
                            routingContext
                                .response()
                                .putHeader(HttpHeaders.CONTENT_TYPE, ArtifactHandler.CONTENT_TYPE_TURTLE)
                                .end(response.getArtifactAsString());
                        } else {
                            routingContext.fail(response.getStatusCode());
                        }
                    } else {
                        routingContext.fail(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    }
                });
    }
    
    private String replaceNullRealtiveURIs(String artifactUri, String representation, String format) {
        // TODO: this method is not closed for modifications
        if (format.compareToIgnoreCase(RdfUtils.TURTLE) == 0) {
            return representation.replaceAll("<>", "<" + artifactUri + ">");
        }
        
        return representation;
    }
    
    private void persistArtifact(RoutingContext routingContext, String verb, 
            String artifactUri, String artifactStr, int successStatusCode) {
        
        RepositoryRequest repositoryRequest = new RepositoryRequest(verb, artifactUri, artifactStr);
        
        sendRepositoryRequest(repositoryRequest,
                r -> {
                    if (r.succeeded()) {
                        RepositoryResponse response = (new Gson()).fromJson(r.result().body(), RepositoryResponse.class);
                        
                        if (response.getStatusCode() == successStatusCode) {
                            routingContext
                                .response()
                                .putHeader(HttpHeaders.CONTENT_TYPE, ArtifactHandler.CONTENT_TYPE_TURTLE)
                                .end(response.getArtifactAsString());
                        } else {
                            routingContext.fail(response.getStatusCode());
                        }
                    } else {
                        routingContext.fail(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    }
                });
    }
    
    private void sendRepositoryRequest(RepositoryRequest request, Handler<AsyncResult<Message<String>>> handler) {
        StnEventBus eventBus = new StnEventBus();
        
        eventBus.sendMessage(StnEventBus.REPOSITORY_ADDRESS, request, handler);
    }
    
    public abstract String processArtifactRepresentation(String artifactUri, 
            String representation, String format) throws InvalidArtifactRepresentationException;
    
}
