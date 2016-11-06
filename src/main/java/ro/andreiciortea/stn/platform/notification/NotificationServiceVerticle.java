package ro.andreiciortea.stn.platform.notification;

import io.vertx.core.AbstractVerticle;

public class NotificationServiceVerticle extends AbstractVerticle {
    
    @Override
    public void start() {
        NotificationService.registerNotificationServices(vertx, 
                NotificationService.EVENT_BUS_ADDRESS
            );
    }
}
