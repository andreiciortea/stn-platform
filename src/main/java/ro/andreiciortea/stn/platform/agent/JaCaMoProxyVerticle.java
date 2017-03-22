package ro.andreiciortea.stn.platform.agent;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;


public class JaCaMoProxyVerticle extends AbstractVerticle {
    
    @Override
    public void start(Future<Void> future) {
        JsonObject config = config().getJsonObject("websocket");
        
        int websocket_port = config.getInteger("port", 8090);
        String websocket_host = config.getString("host", "localhost"); 
        
        HttpServer server = vertx.createHttpServer();
        
        server.websocketHandler(websocket -> {
//            DigitalArtifactModel artifactModel = new MessageModel().withObserve();//.withoutStorage();
            
            websocket.handler(buffer -> {
                String message = new String(buffer.getBytes());
                
//                (new ArtifactHttpRouterBuilder(vertx))
//                    .processAndStoreArtifact(message, artifactModel, websocket, r -> {  });
            });
        }).listen(websocket_port, 
                        websocket_host, 
                        result -> {
                            if (result.succeeded()) {
                                future.complete();
                            } else {
                                future.fail(result.cause());
                            }
                    });
    }
    
}
