package ro.andreiciortea.stn.platform.agent;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import ro.andreiciortea.stn.platform.artifact.DigitalArtifactModel;
import ro.andreiciortea.stn.platform.artifact.MessageModel;
import ro.andreiciortea.stn.platform.http.ArtifactHttpRouterBuilder;


public class JaCaMoProxyVerticle extends AbstractVerticle {
    
    private HttpServer server;
    
    @Override
    public void start(Future<Void> future) {
        server = vertx.createHttpServer();
        
        server.websocketHandler(websocket -> {
            DigitalArtifactModel artifactModel = new MessageModel().withObserve();//.withoutStorage();
            
            websocket.handler(buffer -> {
                String message = new String(buffer.getBytes());
                
                (new ArtifactHttpRouterBuilder(vertx))
                    .processAndStoreArtifact(message, artifactModel, websocket, r -> {  });
            });
        }).listen(8090, "localhost", result -> {
            if (result.succeeded()) {
                future.complete();
            } else {
                future.fail(result.cause());
            }
        });
    }
    
}
