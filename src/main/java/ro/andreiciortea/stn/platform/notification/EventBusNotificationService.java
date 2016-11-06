package ro.andreiciortea.stn.platform.notification;

import io.vertx.core.Vertx;


public class EventBusNotificationService implements NotificationService {

//    private AgentService agentService;
    
    public EventBusNotificationService(Vertx vertx) {
//        agentService = AgentService.createProxy(vertx, AgentService.EVENT_BUS_ADDRESS);
    }
    
    @Override
    public void notifyObserver(String artifactUri, String artifactStr, String observerUri) {
        // TODO
//        System.out.println("[NOTIFICATION SERVICE] Passing on notification for: " + observerUri);
//        agentService.sendMessageToAgent(observerUri, artifactStr, res -> { });
    }

}
