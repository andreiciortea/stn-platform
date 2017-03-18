package ro.andreiciortea.stn.platform.api;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.Gson;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import ro.andreiciortea.stn.platform.eventbus.ArtifactRequest;
import ro.andreiciortea.stn.platform.eventbus.ArtifactResponse;
import ro.andreiciortea.stn.platform.repository.RepositoryServiceVerticle;
import ro.andreiciortea.stn.vocabulary.STNCore;

@RunWith(VertxUnitRunner.class)
public class UserAccountHandlerTest {
    
    private Vertx vertx;
    private Integer port;
    
    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        
        // Choose a random port for testing. This method may fail if the port is 
        // occupied between the "close" method and the start of the HTTP server.
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
        
        // Set the configuration JSON object.
        JsonObject config = new JsonObject()
                .put("http", new JsonObject().put("port", port))
                .put("repository", 
                        new JsonObject()
                            .put("engine", "JenaTDB")
                            .put("in-memory", true)
                    );
        
        DeploymentOptions options = new DeploymentOptions().setConfig(config);
        
        vertx.deployVerticle(HttpServerVerticle.class.getName(),
                options,
                context.asyncAssertSuccess()
            );
        
//        vertx.deployVerticle(RepositoryServiceVerticle.class.getName(),
//                options,
//                context.asyncAssertSuccess()
//            );
    }
    
    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }
    
    @Test
    public void testGetUserAccount(TestContext context) {
        Async async = context.async();
        
        String testUri = "http://localhost:" + port + "/users/test";
        String testRepresentation = "<" + testUri + "> a <" + STNCore.UserAccount.getURI().toString() + "> .";
        
        vertx.eventBus().consumer(RepositoryServiceVerticle.REPOSITORY_BUS_ADDRESS, message -> {
                ArtifactRequest request = (new Gson()).fromJson(message.body().toString(), ArtifactRequest.class);
                
                context.assertEquals(request.getVerb(), ArtifactRequest.GET);
                context.assertEquals(request.getArtifactIri(), testUri);
                
                ArtifactResponse response = new ArtifactResponse(HttpStatus.SC_OK, testUri, testRepresentation);
                
                message.reply(response.toJson());
            });
        
        vertx.createHttpClient().getNow(port, "localhost", "/users/test", 
                response -> {
                    context.assertEquals(response.statusCode(), HttpStatus.SC_OK);
                    context.assertEquals(response.getHeader(HttpHeaders.CONTENT_TYPE), ArtifactHandler.CONTENT_TYPE_TURTLE);
                    response.handler(body -> {
                        context.assertEquals(body.toString(), testRepresentation);
                        async.complete();
                    });
            });
    }
    
    @Test
    public void testUserAccountContainerNotFound(TestContext context) {
        Async async = context.async();
        
        vertx.createHttpClient().getNow(port, "localhost", "/bla",
                response -> {
                    context.assertEquals(response.statusCode(), HttpStatus.SC_NOT_FOUND);
                    async.complete();
            });
    }
    
    @Test
    public void testUserAccountNotFound(TestContext context) {
        Async async = context.async();
        
        vertx.eventBus().consumer(RepositoryServiceVerticle.REPOSITORY_BUS_ADDRESS, message -> {
            ArtifactRequest request = (new Gson()).fromJson(message.body().toString(), ArtifactRequest.class);
            
            context.assertEquals(request.getVerb(), ArtifactRequest.GET);
            
            ArtifactResponse response = new ArtifactResponse(HttpStatus.SC_NOT_FOUND, request.getArtifactIri());
            
            message.reply(response.toJson());
        });
        
        vertx.createHttpClient().getNow(port, "localhost", "/users/foo", 
            response -> {
                context.assertEquals(response.statusCode(), HttpStatus.SC_NOT_FOUND);
                async.complete();
        });
    }
    
    @Test
    public void testCreatePersistentUserAccount(TestContext context) {
        Async async = context.async();
        
        String testUri = "http://localhost:" + port + "/users/myaccount";
        String testRepresentation = "<> a <" + STNCore.UserAccount.getURI().toString() + "> .";
        String processedRepresentation = "<" + testUri + "> a <" + STNCore.UserAccount.getURI().toString() + "> .";
        
        vertx.eventBus().consumer(RepositoryServiceVerticle.REPOSITORY_BUS_ADDRESS, message -> {
                ArtifactRequest request = (new Gson()).fromJson(message.body().toString(), ArtifactRequest.class);
                
                context.assertEquals(request.getVerb(), ArtifactRequest.POST);
                context.assertEquals(request.getArtifactIri(), testUri);
                context.assertEquals(request.getArtifactAsString(), processedRepresentation);
                
                ArtifactResponse response = new ArtifactResponse(HttpStatus.SC_CREATED, 
                        request.getArtifactIri(), request.getArtifactAsString());
                
                message.reply(response.toJson());
            });
        
        vertx.createHttpClient().post(port, "localhost", "/users/")
            .putHeader("Slug", "myaccount")
            .putHeader(HttpHeaders.CONTENT_TYPE, ArtifactHandler.CONTENT_TYPE_TURTLE)
            .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(testRepresentation.length()))
            .handler(response -> {
                    context.assertEquals(response.statusCode(), HttpStatus.SC_CREATED);
                    response.handler(body -> {
                        context.assertNotNull(response.getHeader(HttpHeaders.LOCATION));
                        context.assertEquals(response.getHeader(HttpHeaders.LOCATION), testUri);
                        context.assertEquals(response.getHeader(HttpHeaders.CONTENT_TYPE), ArtifactHandler.CONTENT_TYPE_TURTLE);
                        context.assertEquals(body.toString(), processedRepresentation);
                        async.complete();
                    });
                })
            .write(testRepresentation)
            .end();
    }
    
    @Test
    public void testCreateUserAccountBadRequest(TestContext context) {
        Async async = context.async();
        
        String testRepresentation = "bla";
        
        vertx.createHttpClient().post(port, "localhost", "/users/")
            .putHeader("Slug", "myaccount")
            .putHeader(HttpHeaders.CONTENT_TYPE, ArtifactHandler.CONTENT_TYPE_TURTLE)
            .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(testRepresentation.length()))
            .handler(response -> {
                    context.assertEquals(response.statusCode(), HttpStatus.SC_BAD_REQUEST);
                    async.complete();
                })
            .write(testRepresentation)
            .end();
    }
    
    @Test
    public void testUpdatePersistentUserAccount(TestContext context) {
        Async async = context.async();
        
        String testUri = "http://localhost:" + port + "/users/myaccount";
        String testRepresentation = "<" + testUri + "> a <" + STNCore.UserAccount.getURI().toString() + "> .";
        
        vertx.eventBus().consumer(RepositoryServiceVerticle.REPOSITORY_BUS_ADDRESS, message -> {
                ArtifactRequest request = (new Gson()).fromJson(message.body().toString(), ArtifactRequest.class);
                
                context.assertEquals(request.getVerb(), ArtifactRequest.PUT);
                context.assertEquals(request.getArtifactIri(), testUri);
                context.assertEquals(request.getArtifactAsString(), testRepresentation);
                
                ArtifactResponse response = new ArtifactResponse(HttpStatus.SC_OK, 
                        request.getArtifactIri(), request.getArtifactAsString());
                
                message.reply(response.toJson());
            });
        
        vertx.createHttpClient().put(port, "localhost", "/users/myaccount")
            .putHeader(HttpHeaders.CONTENT_TYPE, ArtifactHandler.CONTENT_TYPE_TURTLE)
            .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(testRepresentation.length()))
            .handler(response -> {
                    context.assertEquals(response.statusCode(), HttpStatus.SC_OK);
                    response.handler(body -> {
                        context.assertNull(response.getHeader(HttpHeaders.LOCATION));
                        context.assertEquals(response.getHeader(HttpHeaders.CONTENT_TYPE), ArtifactHandler.CONTENT_TYPE_TURTLE);
                        context.assertEquals(body.toString(), testRepresentation);
                        async.complete();
                    });
                })
            .write(testRepresentation)
            .end();
    }
    
    @Test
    public void testUpdateUserAccountBadRequest(TestContext context) {
        Async async = context.async();
        
        String testRepresentation = "bla";
        
        vertx.createHttpClient().put(port, "localhost", "/users/test")
            .putHeader(HttpHeaders.CONTENT_TYPE, ArtifactHandler.CONTENT_TYPE_TURTLE)
            .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(testRepresentation.length()))
            .handler(response -> {
                    context.assertEquals(response.statusCode(), HttpStatus.SC_BAD_REQUEST);
                    async.complete();
                })
            .write(testRepresentation)
            .end();
    }
    
    @Test
    public void testDeleteUserAccount(TestContext context) {
        Async async = context.async();
        
        String testUri = "http://localhost:" + port + "/users/test";
        String testRepresentation = "<" + testUri + "> a <" + STNCore.UserAccount.getURI().toString() + "> .";
        
        vertx.eventBus().consumer(RepositoryServiceVerticle.REPOSITORY_BUS_ADDRESS, message -> {
                ArtifactRequest request = (new Gson()).fromJson(message.body().toString(), ArtifactRequest.class);
                
                context.assertEquals(request.getVerb(), ArtifactRequest.DELETE);
                context.assertEquals(request.getArtifactIri(), testUri);
                
                ArtifactResponse response = new ArtifactResponse(HttpStatus.SC_OK, 
                        request.getArtifactIri(), testRepresentation);
                
                message.reply(response.toJson());
            });
        
        vertx.createHttpClient().delete(port, "localhost", "/users/test", response -> {
                context.assertEquals(response.statusCode(), HttpStatus.SC_OK);
                context.assertNotNull(response.getHeader(HttpHeaders.CONTENT_TYPE));
                context.assertEquals(response.getHeader(HttpHeaders.CONTENT_TYPE), ArtifactHandler.CONTENT_TYPE_TURTLE);
                response.handler(body -> {
                    context.assertEquals(body.toString(), testRepresentation);
                    async.complete();
                });
            }).end();
    }
    
    @Test
    public void testDeleteUserAccountNotFound(TestContext context) {
        Async async = context.async();
        
        String testUri = "http://localhost:" + port + "/users/test";
        
        vertx.eventBus().consumer(RepositoryServiceVerticle.REPOSITORY_BUS_ADDRESS, message -> {
                ArtifactRequest request = (new Gson()).fromJson(message.body().toString(), ArtifactRequest.class);
                
                context.assertEquals(request.getVerb(), ArtifactRequest.DELETE);
                context.assertEquals(request.getArtifactIri(), testUri);
                
                ArtifactResponse response = new ArtifactResponse(HttpStatus.SC_NOT_FOUND, request.getArtifactIri());
                
                message.reply(response.toJson());
            });
        
        vertx.createHttpClient().delete(port, "localhost", "/users/test", response -> {
                context.assertEquals(response.statusCode(), HttpStatus.SC_NOT_FOUND);
                async.complete();
            }).end();
    }
}
