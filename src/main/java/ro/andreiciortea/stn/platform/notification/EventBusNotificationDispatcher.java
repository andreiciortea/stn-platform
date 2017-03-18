package ro.andreiciortea.stn.platform.notification;

import io.vertx.core.Vertx;
import ro.andreiciortea.stn.platform.eventbus.ArtifactNotification;
import ro.andreiciortea.stn.platform.eventbus.ObserverNotification;


public class EventBusNotificationDispatcher implements NotificationDispatcher {

    public static final String OBSERVER_NOTIFICATION = "ro.andreiciortea.stn.notification.observer";
    
    private Vertx vertx;
    
    public EventBusNotificationDispatcher(Vertx vertx) {
        this.vertx = vertx;
    }
    
    @Override
    public void notifyObserver(AgentCard agentCard, ArtifactNotification message) {
        if (agentCard != null && agentCard.getAgentIRI() != null) {
            ObserverNotification notification = new ObserverNotification(agentCard.getAgentIRI(), message.getArtifactAsString());
            vertx.eventBus().publish(OBSERVER_NOTIFICATION, notification.toJson());
        }
    }
}
