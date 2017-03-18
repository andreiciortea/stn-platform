package ro.andreiciortea.stn.platform.notification;

import ro.andreiciortea.stn.platform.eventbus.ArtifactNotification;

public interface NotificationDispatcher {

    void notifyObserver(AgentCard observer, ArtifactNotification notification);
}
