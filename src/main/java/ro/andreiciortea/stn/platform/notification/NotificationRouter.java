package ro.andreiciortea.stn.platform.notification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * @author andreiciortea
 *
 */
public class NotificationRouter {

    private Map<String, DefaultArtifactObserveHandler> registry;

    public NotificationRouter() {
        registry = new HashMap<String, DefaultArtifactObserveHandler>();
    }
    
    public NotificationRouter add(String artifactType, DefaultArtifactObserveHandler handler) {
        registry.put(artifactType, handler);
        return this;
    }
    
    /**
     * Routes the notification for an artifact
     * 
     * @param message
     * @return
     */
    public List<AgentCard> routeNotification(String artifactIRI, String artifactType, String artifactStr) {
        DefaultArtifactObserveHandler handler = registry.get(artifactType);
        return handler.getObservers(artifactIRI, artifactStr);
    }
    
}
