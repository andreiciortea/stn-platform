package ro.andreiciortea.stn.platform.notification;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import ro.andreiciortea.stn.platform.eventbus.ArtifactNotification;

public class HttpNotificationDispatcher implements NotificationDispatcher {

    private Vertx vertx;
    
    public HttpNotificationDispatcher(Vertx vertx) {
        this.vertx = vertx;
    }
    
    @Override
    public void notifyObserver(AgentCard observer, ArtifactNotification notification) {
        HttpClientOptions options = new HttpClientOptions().setKeepAlive(true);
        HttpClient client = vertx.createHttpClient(options);

        // TODO update notification data model
        client.postAbs(observer.getCallbackIRI(), response -> {
//            System.out.println("Received response with status code " + response.statusCode());
        }).putHeader("Content-type", "text/turtle").end(notification.getArtifactAsString());
    }

}
