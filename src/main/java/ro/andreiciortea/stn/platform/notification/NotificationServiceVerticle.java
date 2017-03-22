package ro.andreiciortea.stn.platform.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import io.vertx.core.AbstractVerticle;
import ro.andreiciortea.stn.platform.eventbus.ArtifactNotification;
import ro.andreiciortea.stn.vocabulary.STNCore;


public class NotificationServiceVerticle extends AbstractVerticle {
    
    private static final String DEFAULT_ARTIFACT_TYPE = "DEFAULT";
    
    private NotificationRouter router;
    private Map<String, List<NotificationDispatcher>> dispatchers;
    
    @Override
    public void start() {
        initNotificationRouter();
        initDispatchers();
        
        vertx.eventBus().consumer(NotificationService.EVENT_BUS_ADDRESS, message -> {
            ArtifactNotification notification = (new Gson()).fromJson(message.body().toString(), ArtifactNotification.class);
            
            List<AgentCard> observers = router.routeNotification(notification.getArtifactIRI(), 
                    notification.getArtifactType(), notification.getArtifactAsString());
            
            dispatchNotification(notification, observers);
        });
    }
    
    private void initNotificationRouter() {
        router = new NotificationRouter();
        
        router.add(STNCore.Message.getURI(), new MessageObserveHandler());
        router.add(STNCore.Relation.getURI(), new RelationObserveHandler());
    }
    
    private void initDispatchers() {
        dispatchers = new HashMap<String, List<NotificationDispatcher>>();
                
        // TODO: use config file to init dispatchers
        addDispatcher(STNCore.Message.getURI(), new EventBusNotificationDispatcher(vertx));
        addDispatcher(STNCore.Relation.getURI(), new HttpNotificationDispatcher(vertx));
        addDispatcher(DEFAULT_ARTIFACT_TYPE, new EventBusNotificationDispatcher(vertx));
    }
    
    private void addDispatcher(String key, NotificationDispatcher dispatcher) {
        List<NotificationDispatcher> dispacherList = dispatchers.get(key);
        
        if (dispacherList == null) {
            dispacherList = new ArrayList<NotificationDispatcher>();
        }
        
        dispacherList.add(dispatcher);
        dispatchers.put(key, dispacherList);
    }
    
    private void dispatchNotification(ArtifactNotification notification, List<AgentCard> observers) {
        String artifactType = notification.getArtifactType();
        
        List<NotificationDispatcher> dispacherList = dispatchers.get(artifactType);
        
        if (dispacherList == null) {
            dispacherList = dispatchers.get(DEFAULT_ARTIFACT_TYPE);
        }
        
        for (NotificationDispatcher d : dispacherList) {
            d.dispatchNotification(notification, observers);
        }
    }
}
