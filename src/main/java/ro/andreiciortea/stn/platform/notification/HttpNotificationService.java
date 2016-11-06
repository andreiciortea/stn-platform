package ro.andreiciortea.stn.platform.notification;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

public class HttpNotificationService implements NotificationService {

    private Vertx vertx;
    
    public HttpNotificationService(Vertx vertx) {
        this.vertx = vertx;
    }
    
    @Override
    public void notifyObserver(String artifactUri, String artifactStr, String observerUri) {
//        System.out.println("Sedning HTTP notification!");
        
        HttpClientOptions options = new HttpClientOptions().setKeepAlive(true);
        HttpClient client = vertx.createHttpClient(options);
        
        client.postAbs(observerUri, response -> {
//            System.out.println("Received response with status code " + response.statusCode());
        }).putHeader("Content-type", "text/turtle").end(artifactStr);
    }
    
}
