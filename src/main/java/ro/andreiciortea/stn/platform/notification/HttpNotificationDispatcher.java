package ro.andreiciortea.stn.platform.notification;

import java.util.List;

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
    public void dispatchNotification(ArtifactNotification notification, List<AgentCard> observers) {
        HttpClientOptions options = new HttpClientOptions().setKeepAlive(true);
        HttpClient client = vertx.createHttpClient(options);
        
        for (AgentCard o : observers) {
            // TODO update notification data model
            client.postAbs(o.getCallbackIRI())
                    .putHeader("Content-type", "text/turtle")
                    .end(notification.getArtifactAsString());
        }
    }

}
