package ro.andreiciortea.stn.platform.api;

import org.apache.jena.vocabulary.RDF;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import ro.andreiciortea.stn.platform.artifact.ArtifactHandler;
import ro.andreiciortea.stn.vocabulary.STNCore;


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
        
        HttpArtifactHandlerAdapter userAccountHandler = 
                new HttpArtifactHandlerAdapter(new ArtifactHandler(STNCore.UserAccount.getURI()))
                        .setCollectionQueryBuilder(params -> {
                            String ownerIRI = params.get("ownedBy");
                            
                            String query = "CONSTRUCT {"
                                  + "?accountUri <" + RDF.type + "> <" + STNCore.UserAccount + "> ."
                                  + "}"
                                  + "WHERE {"
                                  + "?accountUri <" + RDF.type + "> <" + STNCore.UserAccount + "> ."
                                  + "?accountUri <" + STNCore.heldBy + "> ?holderUri ."
                                  + "?holderUri <" + STNCore.ownedBy + "> <" + ownerIRI + "> ."
                                  + "}";
                              
                            return query;
                        });
        
        mountArtifactDefaultRoutes("/users/", userAccountHandler);
        
        
        HttpArtifactHandlerAdapter relationHandler = 
                new HttpArtifactHandlerAdapter(new ArtifactHandler(STNCore.Relation.getURI())
                                            .withObserve())
                        .setCollectionQueryBuilder(params -> {
                            String sourceUri = params.get("source");
                            String targetUri = params.get("target");
                            
                            if (sourceUri == null && targetUri == null) return null;
                            
                            if (sourceUri == null) {
                                return "CONSTRUCT {"
                                        + "?relationUri <" + RDF.type + "> <" + STNCore.Relation + "> ."
                                        + "?relationUri <" + STNCore.source + "> ?sourceUri ."
                                        + "?relationUri <" + STNCore.target + "> <" + targetUri + "> ."
                                        + "}"
                                        + "WHERE {"
                                        + "?relationUri <" + STNCore.source + "> ?sourceUri ."
                                        + "?relationUri <" + STNCore.target + "> <" + targetUri + "> ."
                                        + "}";
                            }
                            
                            if (targetUri == null) {
                                return "CONSTRUCT {"
                                        + "?relationUri <" + RDF.type + "> <" + STNCore.Relation + "> ."
                                        + "?relationUri <" + STNCore.source + "> <" + sourceUri + "> ."
                                        + "?relationUri <" + STNCore.target + "> ?targetUri ."
                                        + "}"
                                        + "WHERE {"
                                        + "?relationUri <" + STNCore.source + "> <" + sourceUri + "> ."
                                        + "?relationUri <" + STNCore.target + "> ?targetUri ."
                                        + "}";
                            }
                            
                            return "CONSTRUCT {"
                                    + "?relationUri <" + RDF.type + "> <" + STNCore.Relation + "> ."
                                    + "?relationUri <" + STNCore.source + "> <" + sourceUri + "> ."
                                    + "?relationUri <" + STNCore.target + "> <" + targetUri + "> ."
                                    + "}"
                                    + "WHERE {"
                                    + "?relationUri <" + STNCore.source + "> <" + sourceUri + "> ."
                                    + "?relationUri <" + STNCore.target + "> <" + targetUri + "> ."
                                    + "}";
                        });
        
        mountArtifactDefaultRoutes("/connections/", relationHandler);
        
        mountArtifactDefaultRoutes("/messages/",
                new HttpArtifactHandlerAdapter(new ArtifactHandler(STNCore.Message.getURI())
                                                .withObserve()
                                                .withoutStorage()
                        )
            );
        
        startServer();
    }
    
    private void mountArtifactDefaultRoutes(String containerRelativeUri, HttpArtifactHandlerAdapter handler) {
        router.get(containerRelativeUri + ":artifactId").handler(handler::handleGetArtifact);
        
        router.get(containerRelativeUri).handler(handler::handleGetFilteredCollection);
        
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
