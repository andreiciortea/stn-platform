package ro.andreiciortea.stn.platform.api;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;

import com.google.gson.Gson;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import ro.andreiciortea.stn.platform.RdfUtils;
import ro.andreiciortea.stn.platform.eventbus.ArtifactNotification;
import ro.andreiciortea.stn.platform.eventbus.ArtifactRequest;
import ro.andreiciortea.stn.platform.eventbus.ArtifactResponse;
import ro.andreiciortea.stn.platform.notification.NotificationService;
import ro.andreiciortea.stn.platform.repository.RepositoryServiceVerticle;


@SuppressWarnings("serial")
class InvalidArtifactRepresentationException extends Exception {}


public abstract class ArtifactHandler {
    
    public static final String CONTENT_TYPE_TURTLE = "text/turtle"; 
    
    private boolean persistent = true;
    private boolean observable = false;
    
    private Vertx vertx = Vertx.currentContext().owner();
    
    
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
        
        ArtifactRequest repositoryRequest = new ArtifactRequest(ArtifactRequest.GET, artifactUri);
        
        sendRepositoryRequest(repositoryRequest,
                r -> {
                    if (r.succeeded()) {
                        ArtifactResponse response = (new Gson()).fromJson(r.result().body(), ArtifactResponse.class);
                        
                        if (response.getStatusCode() == HttpStatus.SC_OK) {
                            routingContext
                                .response()
                                .setStatusCode(HttpStatus.SC_OK)
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
                persistArtifact(routingContext, ArtifactRequest.PUT, artifactUri, artifactStr, HttpStatus.SC_OK);
            }
            
            if (observable) {
                publishNotification(artifactUri, artifactStr);
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
                persistArtifact(routingContext, ArtifactRequest.POST, artifactUri, artifactStr, HttpStatus.SC_CREATED);
            }
            
            if (observable) {
                publishNotification(artifactUri, artifactStr);
            }
        } catch (InvalidContainerUriException e) {
            routingContext.fail(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        } catch (InvalidArtifactRepresentationException e) {
            routingContext.fail(HttpStatus.SC_BAD_REQUEST);
        }
    }
    
    public void handleDeleteArtifact(RoutingContext routingContext) {
        String artifactUri = routingContext.request().absoluteURI();
        
        ArtifactRequest repositoryRequest = new ArtifactRequest(ArtifactRequest.DELETE, artifactUri);
        
        if (persistent) {
            System.out.println("Sending delete request to repository");
            
            sendRepositoryRequest(repositoryRequest,
                    r -> {
                        if (r.succeeded()) {
                            ArtifactResponse response = (new Gson()).fromJson(r.result().body(), ArtifactResponse.class);
                            
                            if (response.getStatusCode() == HttpStatus.SC_OK) {
                                routingContext
                                    .response()
                                    .setStatusCode(HttpStatus.SC_OK)
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
        
        ArtifactRequest repositoryRequest = new ArtifactRequest(verb, artifactUri, artifactStr);
        
        sendRepositoryRequest(repositoryRequest,
                r -> {
                    if (r.succeeded()) {
                        ArtifactResponse response = (new Gson()).fromJson(r.result().body(), ArtifactResponse.class);
                        
                        if (response.getStatusCode() == successStatusCode) {
                            routingContext
                                .response()
                                .setStatusCode(successStatusCode)
                                .putHeader(HttpHeaders.CONTENT_TYPE, ArtifactHandler.CONTENT_TYPE_TURTLE);
                            
                            if (successStatusCode == HttpStatus.SC_CREATED) {
                                routingContext
                                    .response()
                                    .putHeader(HttpHeaders.LOCATION, response.getArtifactIri());
                            }
                            
                            routingContext
                                .response().end(response.getArtifactAsString());
                        } else {
                            routingContext.fail(response.getStatusCode());
                        }
                    } else {
                        routingContext.fail(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    }
                });
    }
    
    private void sendRepositoryRequest(ArtifactRequest request, Handler<AsyncResult<Message<String>>> handler) {
        vertx.eventBus().send(RepositoryServiceVerticle.REPOSITORY_BUS_ADDRESS, request.toJson(), handler);
    }
    
    private void publishNotification(String artifactUri, String artifactStr) {
        vertx.eventBus().publish(NotificationService.EVENT_BUS_ADDRESS, 
                (new ArtifactNotification(artifactUri, getArtifactType(), artifactStr)).toJson());
    }
    
    
    public abstract String getArtifactType();
    
    public abstract String processArtifactRepresentation(String artifactUri, 
            String representation, String format) throws InvalidArtifactRepresentationException;
    
}
