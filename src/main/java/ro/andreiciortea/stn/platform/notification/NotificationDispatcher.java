package ro.andreiciortea.stn.platform.notification;

import java.util.List;

import ro.andreiciortea.stn.platform.eventbus.ArtifactNotification;

public interface NotificationDispatcher {

    void dispatchNotification(ArtifactNotification notification, List<AgentCard> observers);
    
}
