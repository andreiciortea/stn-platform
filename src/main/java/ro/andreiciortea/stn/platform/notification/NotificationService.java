package ro.andreiciortea.stn.platform.notification;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

@ProxyGen
@VertxGen
public interface NotificationService {
    
    public static final String EVENT_BUS_ADDRESS = "hub.service.notification";
    
    static void registerNotificationServices(Vertx vertx, String address) {
        // TODO: use external config to register appropriate services
        ProxyHelper.registerService(NotificationService.class, vertx, 
                new HttpNotificationService(vertx), address);
//        ProxyHelper.registerService(NotificationService.class, vertx, 
//                new EventBusNotificationService(vertx), address);
    }
    
    static NotificationService createProxy(Vertx vertx, String address) {
        return ProxyHelper.createProxy(NotificationService.class, vertx, address);
    }
    
    void notifyObserver(String artifactUri, String artifactStr, String observerUri);
}
