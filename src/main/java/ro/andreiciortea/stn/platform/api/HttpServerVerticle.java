package ro.andreiciortea.stn.platform.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;


public class HttpServerVerticle extends AbstractVerticle {
    
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8080;
    
    private HttpServer server;
    private Router router;
    
    @Override
    public void start() {
        server = vertx.createHttpServer();
        router = Router.router(vertx);
        
        router.route().handler(BodyHandler.create());
        
        mountArtifactDefaultRoutes("/users/", new UserAccountHandler());
        
        startServer();
    }
    
    private void mountArtifactDefaultRoutes(String containerRelativeUri, ArtifactHandler handler) {
        router.get(containerRelativeUri + ":artifactId").handler(handler::handleGetArtifact);
        
        router.put(containerRelativeUri + ":artifactId")
                .consumes(ArtifactHandler.CONTENT_TYPE_TURTLE).handler(handler::handlePutArtifact);
        
        router.post(containerRelativeUri)
                .consumes(ArtifactHandler.CONTENT_TYPE_TURTLE).handler(handler::handlePostArtifact);
        
        router.delete(containerRelativeUri + ":artifactId").handler(handler::handleDeleteArtifact);
    }
    
    private void startServer() {
        int port = DEFAULT_PORT;
        String host = DEFAULT_HOST;
        
        JsonObject httpConfig = config().getJsonObject("http");
        
        if (httpConfig != null) {
            port = httpConfig.getInteger("port", DEFAULT_PORT);
            httpConfig.getString("host", DEFAULT_HOST);
        }
        
        server.requestHandler(router::accept).listen(port, host);
    }
}
