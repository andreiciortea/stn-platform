package ro.andreiciortea.stn.platform.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import ro.andreiciortea.stn.platform.Utils;
import ro.andreiciortea.stn.platform.artifact.DigitalArtifactModel;
import ro.andreiciortea.stn.platform.artifact.UserAccountModel;
import ro.andreiciortea.stn.platform.eventbus.ArtifactRequest;


public class StnHttpServerVerticle extends AbstractVerticle {
    
    @Override
    public void start(Future<Void> future) {
        HttpServer server = vertx.createHttpServer();
        
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        
        router.route("/assets/*").handler(StaticHandler.create("assets").setCachingEnabled(false));
        
//        ArtifactHttpRouterBuilder routerBuilder = new ArtifactHttpRouterBuilder(vertx);
        
        // Mount user account sub-router
//        router.mountSubRouter("/", routerBuilder.buildRouter(new UserAccountModel()));
        // Mount relation sub-router
//        router.mountSubRouter("/", routerBuilder.buildRouter(new RelationModel().withObserve()));
        // Mount message sub-router
//        router.mountSubRouter("/", routerBuilder.buildRouter(new MessageModel().withObserve().withoutStorage()));
        
        router = routeArtifactModel(router, new UserAccountModel());
        
        server.requestHandler(router::accept).listen(
            config().getInteger("http.port", Utils.DEFAULT_HTTP_PORT),
            config().getString("host", "localhost"),
            result -> {
                if (result.succeeded()) {
                    future.complete();
                } else {
                    future.fail(result.cause());
                }
            }
        );
        
    }
    
    public Router routeArtifactModel(Router router, DigitalArtifactModel artifactModel) {
//        router.get(artifactModel.getContainerPath())
//                .handler(getQueryHandler(artifactModel));
        
        router.get(artifactModel.getContainerPath() + ":artifactid")
                .handler(getArtifactHandler());
        
//        router.post(artifactModel.getContainerPath()).consumes("text/turtle")
//                .handler(createArtifactHandler(artifactModel.getContainerPath(), artifactModel));
        
//        router.put(artifactModel.getContainerPath() + ":artifactid").consumes("text/turtle")
//                .blockingHandler(updateArtifactHandler(artifactModel));
        
//        router.delete(artifactModel.getContainerPath() + ":artifactid")
//                .blockingHandler(deleteArtifactHandler());
        
        return router;
    }
    
    private Handler<RoutingContext> getArtifactHandler() {
        String artifactBaseUri = Utils.buildArtifactBaseUri(vertx.getOrCreateContext().config());
        
//        RepositoryService repository = RepositoryService.createProxy(vertx, 
//                RepositoryService.EVENT_BUS_ADDRESS
//            );
        
        return requestingContext -> {
            
            String artifactUri = Utils.buildArtifactUri(artifactBaseUri, requestingContext.request().path());
            
            ArtifactRequest repoReq = new ArtifactRequest(ArtifactRequest.GET, artifactUri);
/*            (new StnEventBus()).sendMessage(StnEventBus.REPOSITORY_ADDRESS, repoReq,
            
//            repository.getArtifactAsString(
//                    Utils.buildArtifactUri(artifactBaseUri, requestingContext.request().path()), 
//                    RepositoryService.TURTLE, 
                    r -> {
                        if (r.succeeded()) {
//                            if (r.result() == null || r.result().isEmpty()) {
                            String atrifactStr = r.result().body();
                            if (atrifactStr == null || atrifactStr.isEmpty()) {
                                requestingContext.fail(HttpStatus.SC_NOT_FOUND);
                            } else {
                                requestingContext
                                    .response()
                                    .putHeader(HttpHeaders.CONTENT_TYPE, "text/turtle")
                                    .end(atrifactStr);
                            }
                        } else {
                            requestingContext.fail(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                        }
                    }
//                );
//        };
                    
                    
                    );*/
        };
    }
    
}
