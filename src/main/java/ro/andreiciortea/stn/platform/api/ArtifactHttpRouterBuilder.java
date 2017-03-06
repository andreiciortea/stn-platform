package ro.andreiciortea.stn.platform.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import ro.andreiciortea.stn.platform.Utils;
import ro.andreiciortea.stn.platform.artifact.DigitalArtifactModel;
import ro.andreiciortea.stn.platform.artifact.DigitalArtifactModelValidator;
import ro.andreiciortea.stn.platform.notification.NotificationService;
import ro.andreiciortea.stn.platform.repository.RepositoryService;


public class ArtifactHttpRouterBuilder {
    
    private Vertx vertx;
    
    public ArtifactHttpRouterBuilder(Vertx vertx) {
        this.vertx = vertx;
    }
    
    // TODO: The builder could allow for a more fine-grained configuration
    // of the router.
    
    public Router buildRouter(DigitalArtifactModel artifactModel) {
        Router router = Router.router(vertx);
        
        router.get(artifactModel.getContainerPath())
                .handler(getQueryHandler(artifactModel));
        
        router.get(artifactModel.getContainerPath() + ":artifactid")
                .handler(getArtifactHandler());
        
        router.post(artifactModel.getContainerPath()).consumes("text/turtle")
                .handler(createArtifactHandler(artifactModel.getContainerPath(), artifactModel));
        
        router.put(artifactModel.getContainerPath() + ":artifactid").consumes("text/turtle")
                .blockingHandler(updateArtifactHandler(artifactModel));
        
        router.delete(artifactModel.getContainerPath() + ":artifactid")
                .blockingHandler(deleteArtifactHandler());
        
        return router;
    }
    
    
    private Handler<RoutingContext> getQueryHandler(DigitalArtifactModel artifactModel) {
        RepositoryService repository = 
                RepositoryService.createProxy(vertx, RepositoryService.EVENT_BUS_ADDRESS);
        
        return requestingContext -> {
            MultiMap params = requestingContext.request().params();
            String queryString = artifactModel.getConstructCollectionQuery(params);
            
            repository.runConstructQuery(queryString,
                    RepositoryService.TURTLE,
                    r -> {
                        if (r.succeeded()) {
                            requestingContext
                                .response()
                                .putHeader(HttpHeaders.CONTENT_TYPE, "text/turtle")
                                .end(r.result());
                        } else {
                            requestingContext.fail(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                        }
                    });
        };
    }
    
    // TODO: if artifact is not persistent, return SC_GONE 
    private Handler<RoutingContext> getArtifactHandler() {
        String artifactBaseUri = Utils.buildArtifactBaseUri(vertx.getOrCreateContext().config());
        
        RepositoryService repository = RepositoryService.createProxy(vertx, 
                RepositoryService.EVENT_BUS_ADDRESS
            );
        
        return requestingContext -> {
            repository.getArtifactAsString(
                    Utils.buildArtifactUri(artifactBaseUri, requestingContext.request().path()), 
                    RepositoryService.TURTLE, 
                    r -> {
                        if (r.succeeded()) {
                            if (r.result() == null || r.result().isEmpty()) {
                                requestingContext.fail(HttpStatus.SC_NOT_FOUND);
                            } else {
                                requestingContext
                                    .response()
                                    .putHeader(HttpHeaders.CONTENT_TYPE, "text/turtle")
                                    .end(r.result());
                            }
                        } else {
                            requestingContext.fail(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                        }
                    }
                );
        };
    }
    
    private Handler<RoutingContext> createArtifactHandler(String containerPath, DigitalArtifactModel artifactModel) {
        String artifactBaseUri = Utils.buildArtifactBaseUri(vertx.getOrCreateContext().config());
        
        return requestingContext -> {
            String slug = requestingContext.request().getHeader("Slug");
            String artifactUri = Utils.genArtifactUri(artifactBaseUri, containerPath, slug);
            
            processAndStoreArtifact(artifactUri,
                    requestingContext.getBodyAsString(),
                    artifactModel,
                    null,
                    r -> {
                        if (r.succeeded()) {
                            requestingContext.response()
                                .setStatusCode(HttpStatus.SC_CREATED)
                                .putHeader(HttpHeaders.LOCATION, artifactUri)
                                .end();
                        } else {
                            if (r.cause().getMessage().equalsIgnoreCase("" + HttpStatus.SC_BAD_REQUEST)) {
                                requestingContext.fail(HttpStatus.SC_BAD_REQUEST);
                            } else {
                                requestingContext.fail(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                            }
                        }
                });
        };
    }
    
    private Handler<RoutingContext> updateArtifactHandler(DigitalArtifactModel artifactModel) {
        return context -> {
            processAndStoreArtifact(context,
                    artifactModel,
                    r -> {
                        if (r.succeeded()) {
                            context.response().end();
                        } else {
                            context.fail(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                        }
                });
        };
    }
    
    private Handler<RoutingContext> deleteArtifactHandler() {
        return requestingContext -> {
            String artifactUri = requestingContext.request().absoluteURI();
            RepositoryService service = 
                    RepositoryService.createProxy(vertx, RepositoryService.EVENT_BUS_ADDRESS);
            
            service.containsArtifact(artifactUri, r -> {
                if (r.succeeded()) {
                    if (r.result()) {
                        service.deleteArtifact(artifactUri, result -> {
                            if (result.succeeded()) {
                                requestingContext.response().end();
                            } else {
                                requestingContext.fail(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                            }
                        });
                    } else {
                        requestingContext.fail(HttpStatus.SC_NOT_FOUND);
                    }
                } else {
                    requestingContext.fail(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                }
            });
        };
    }
    
    
    private void processAndStoreArtifact(RoutingContext context, 
            DigitalArtifactModel artifactModel, Handler<AsyncResult<Void>> resultHandler) {
        
        processAndStoreArtifact(context.request().absoluteURI(), 
                context.getBodyAsString(), 
                artifactModel, 
                null,
                resultHandler
            );
    }
    
    public void processAndStoreArtifact(String payload, DigitalArtifactModel artifactModel, 
            ServerWebSocket websocket, Handler<AsyncResult<Void>> resultHandler) {
        
        String artifactBaseUri = Utils.buildArtifactBaseUri(vertx.getOrCreateContext().config());
        String artifactUri = Utils.genArtifactUri(artifactBaseUri, artifactModel.getContainerPath());
        
        processAndStoreArtifact(artifactUri, payload, artifactModel, websocket, resultHandler);
    }
    
    public void processAndStoreArtifact(String artifactUri, String payload,
            DigitalArtifactModel artifactModel, ServerWebSocket websocket, Handler<AsyncResult<Void>> resultHandler) {
        
//        String payload = context.getBodyAsString();
        
        try {
            String artifactStr = DigitalArtifactModelValidator.cleanseModel(artifactModel,
                                        artifactUri, 
                                        payload, 
                                        RepositoryService.TURTLE 
                                    );
            
            if (artifactStr == null) {
//                context.fail(HttpStatus.SC_BAD_REQUEST);
                resultHandler.handle(Future.failedFuture("" + HttpStatus.SC_BAD_REQUEST));
            } else {
                RepositoryService repository = 
                        RepositoryService.createProxy(vertx, RepositoryService.EVENT_BUS_ADDRESS);
                
                if (artifactModel.isPersistent()) {
                    repository.putArtifact(artifactUri, 
                            artifactStr, 
                            RepositoryService.TURTLE, 
                            resultHandler
                        );
                    
//                    RepositoryRequest repoReq = new RepositoryRequest(RepositoryRequest.PUT, artifactUri, artifactStr);
//                    StnEventBus.publishMessage(vertx, StnEventBus.REPOSITORY_ADDRESS, repoReq);
                }
                
                if (artifactModel.isObservable()) {
                    InputStream stream = new ByteArrayInputStream(artifactStr.getBytes(StandardCharsets.UTF_8));
                    Model model = ModelFactory.createDefaultModel().read(stream, null, RepositoryService.TURTLE);
                    
                    LocalMap<String, String> cache = vertx.sharedData().getLocalMap("cache");
                    
                    artifactModel.getObservers(artifactUri, model, repository, cache, r -> {
                        if (r.succeeded()) {
                            List<String> observers = r.result();
                            
//                            if (observers.size() > 200) 
//                            System.out.println("Found #" + observers.size() + " observers for: " + artifactUri);
                            
                            if (websocket == null) {
                                NotificationService notificationService = 
                                        NotificationService.createProxy(vertx, NotificationService.EVENT_BUS_ADDRESS);
                                
                                for (String o : observers) {
//                                    System.out.println("Notifying observer via notification service: " + o);
//                                    System.out.println("Artifact URI: " + artifactUri);
                                    notificationService.notifyObserver(artifactUri, artifactStr, o);
                                }
                            } else {
//                                int counter = 0;
                                for (String o : observers) {
//                                    counter ++;
//                                    System.out.println("Notifying observer via websocket: " + o);
//                                    System.out.println("Artifact URI: " + artifactUri);
//                                    System.out.println("Artifact str: " + artifactStr);
                                    JsonObject json = new JsonObject();
                                    json.put("receiver", o);
                                    json.put("body", artifactStr);
                                    websocket.writeFinalTextFrame(json.encode());
                                }
                            }
                        }
                    });
                    
/*                    getArtifactObservers(artifactUri, artifactModel, repository, r -> {
                        if (r.succeeded()) {
                            NotificationService notificationService = 
                                    NotificationService.createProxy(vertx, NotificationService.EVENT_BUS_ADDRESS);
                            
                            List<String> observers = r.result();
                            
                            for (String o : observers) {
                                notificationService.notifyObserver(artifactUri, artifactStr, o);
                            }
                        }
                    });*/
                }
            }
        } catch (Exception e) {
            resultHandler.handle(Future.failedFuture(e));
        }
    }
    
    public void getArtifactObservers(String artifactUri, DigitalArtifactModel artifactModel, 
            RepositoryService repository, Handler<AsyncResult<List<String>>> hObservers) {
        
        String queryString = artifactModel.getSelectObserversQuery(artifactUri);
        
        repository.runSelectQuery(queryString, r -> {
            // If querying the repository fails, just fail gracefully.
            if (r.succeeded()) {
                ResultSet results = 
                        ResultSetFactory.fromJSON(new ByteArrayInputStream(r.result().getBytes(StandardCharsets.UTF_8)));
                
//                System.out.println("Query result set: " + r.result());
                
                List<String> observers = new ArrayList<String>();
                
                while (results.hasNext()) {
                    observers.addAll(artifactModel.parseObserversQuerySolution(results.next()));
                }
                
                hObservers.handle(Future.succeededFuture(observers));
            } else {
                hObservers.handle(Future.failedFuture(r.cause().getMessage()));
            }
        });
    }
}
