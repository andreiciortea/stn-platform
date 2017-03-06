package ro.andreiciortea.stn.platform.api;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import ro.andreiciortea.stn.platform.api.HttpServerVerticle;
import ro.andreiciortea.stn.platform.repository.RepositoryServiceVerticle;

@RunWith(VertxUnitRunner.class)
public class HttpServerVerticleTest {
    
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
        
        vertx.deployVerticle(RepositoryServiceVerticle.class.getName(),
                options,
                context.asyncAssertSuccess()
            );
    }
    
    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }
    
/*    @Test
    public void testDummyResourceNotFound(TestContext context) {
        final Async async = context.async();
        
        vertx.createHttpClient().getNow(port, "localhost", "/foo/bar", 
            response -> {
                context.assertTrue(response.statusCode() == HttpStatus.SC_NOT_FOUND);
                response.handler(body -> {
                  context.assertTrue(body.toString().contains("Hello"));
                  async.complete();
                });
        });
    }*/
    
    @Test
    public void testUserAccountNotFound(TestContext context) {
        final Async async = context.async();
        
        vertx.createHttpClient().getNow(port, "localhost", "/users/test", 
            response -> {
                context.assertTrue(response.statusCode() == HttpStatus.SC_NOT_FOUND);
                async.complete();
//                response.handler(body -> {
//                    context.assertTrue(body.toString().contains("Hello"));
//                    async.complete();
//                });
        });
    }
}
