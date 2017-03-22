package ro.andreiciortea.stn.platform.api;

import java.util.function.Function;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;

import com.google.gson.Gson;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import ro.andreiciortea.stn.platform.artifact.ArtifactHandler;
import ro.andreiciortea.stn.platform.eventbus.ArtifactResponse;
import ro.andreiciortea.stn.platform.eventbus.SparqlResponse;

public class HttpArtifactHandlerAdapter {

    private ArtifactHandler artifactHandler;
    private Function<MultiMap, String> filteredCollectionQueryBuilder;
    
    
    public HttpArtifactHandlerAdapter(ArtifactHandler artifactHandler) {
        this.artifactHandler = artifactHandler;
    }
    
    public HttpArtifactHandlerAdapter setCollectionQueryBuilder(Function<MultiMap, String> queryBuilder) {
        this.filteredCollectionQueryBuilder = queryBuilder;
        return this;
    }
    
    public void handleGetArtifact(RoutingContext routingContext) {
        artifactHandler.handleGetArtifact(routingContext.request().absoluteURI(),
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
                                }
                            );
    }

    public void handlePutArtifact(RoutingContext routingContext) {
        artifactHandler.handlePutArtifact(routingContext.request().absoluteURI(),
                                routingContext.getBodyAsString(),
                                getWriteArtifactHandler(routingContext, HttpStatus.SC_OK)
                            );
    }

    public void handlePostArtifact(RoutingContext routingContext) {
        String slug = routingContext.request().getHeader("Slug");
        
        artifactHandler.handlePostArtifact(routingContext.request().absoluteURI(),
                                slug,
                                routingContext.getBodyAsString(),
                                getWriteArtifactHandler(routingContext, HttpStatus.SC_CREATED)
                            );
    }

    public void handleDeleteArtifact(RoutingContext routingContext) {
        artifactHandler.handleDeleteArtifact(routingContext.request().absoluteURI(),
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
                                }
                            );
    }

    public void handleGetFilteredCollection(RoutingContext routingContext) {
        String query = filteredCollectionQueryBuilder.apply(routingContext.request().params());
        
        artifactHandler.handleGetFilteredCollection(query,
                                r -> {
                                    if (r.succeeded()) {
                                        SparqlResponse response = (new Gson()).fromJson(r.result().body(), SparqlResponse.class);
                                        
                                        if (response.getStatusCode() == HttpStatus.SC_OK) {
                                            routingContext
                                                .response()
                                                .setStatusCode(HttpStatus.SC_OK)
                                                .putHeader(HttpHeaders.CONTENT_TYPE, ArtifactHandler.CONTENT_TYPE_TURTLE)
                                                .end(response.getPayload());
                                        } else {
                                            routingContext.fail(response.getStatusCode());
                                        }
                                    } else {
                                        routingContext.fail(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                                    }
                                }
                            );
    }
    
    private Handler<AsyncResult<Message<String>>> getWriteArtifactHandler(RoutingContext routingContext, int successStatusCode) {
        return r -> {
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
        };
    }
    
}
