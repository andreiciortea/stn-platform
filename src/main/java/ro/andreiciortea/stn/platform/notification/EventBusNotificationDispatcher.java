package ro.andreiciortea.stn.platform.notification;

import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.Vertx;
import ro.andreiciortea.stn.platform.eventbus.ArtifactNotification;
import ro.andreiciortea.stn.platform.eventbus.AgentNotification;


public class EventBusNotificationDispatcher implements NotificationDispatcher {

    public static final String OBSERVER_NOTIFICATION = "ro.andreiciortea.stn.notification.observer";
    
    private Vertx vertx;
    
    public EventBusNotificationDispatcher(Vertx vertx) {
        this.vertx = vertx;
    }
    
    @Override
    public void dispatchNotification(ArtifactNotification message, List<AgentCard> observers) {
        List<String> observerIRIs = observers.stream()
                                             .map(card -> { return card.getAgentIRI(); })
                                             .collect(Collectors.toList());
        
        AgentNotification notification = new AgentNotification(observerIRIs, message.getArtifactAsString());
        
        vertx.eventBus().publish(OBSERVER_NOTIFICATION, notification.toJson());
    }
}
