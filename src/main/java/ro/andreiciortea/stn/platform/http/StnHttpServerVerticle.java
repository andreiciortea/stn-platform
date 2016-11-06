package ro.andreiciortea.stn.platform.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import ro.andreiciortea.stn.platform.Utils;
import ro.andreiciortea.stn.platform.artifact.MessageModel;
import ro.andreiciortea.stn.platform.artifact.RelationModel;
import ro.andreiciortea.stn.platform.artifact.UserAccountModel;


public class StnHttpServerVerticle extends AbstractVerticle {
    
    @Override
    public void start(Future<Void> future) {
        HttpServer server = vertx.createHttpServer();
        
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        
        router.route("/assets/*").handler(StaticHandler.create("assets").setCachingEnabled(false));
        
        ArtifactHttpRouterBuilder routerBuilder = new ArtifactHttpRouterBuilder(vertx);
        
        // Mount user account sub-router
        router.mountSubRouter("/", routerBuilder.buildRouter(new UserAccountModel()));
        // Mount relation sub-router
        router.mountSubRouter("/", routerBuilder.buildRouter(new RelationModel().withObserve()));
        // Mount message sub-router
        router.mountSubRouter("/", routerBuilder.buildRouter(new MessageModel().withObserve().withoutStorage()));
        
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
    
}
