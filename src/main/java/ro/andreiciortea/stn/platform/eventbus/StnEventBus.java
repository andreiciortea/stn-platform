package ro.andreiciortea.stn.platform.eventbus;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;

public class StnEventBus {

    public static final String REPOSITORY_ADDRESS = "hub.service.repository";
    
    private Vertx vertx;
    
    public StnEventBus() {
        this.vertx = Vertx.currentContext().owner();
    }
    
    public void publishMessage(String topic, StnMessage message) {
        DeliveryOptions options = new DeliveryOptions();
        options.addHeader(StnMessage.HEADER_CONTENT_TYPE, message.getContentType());
        
        if (vertx != null && vertx.eventBus() != null) {
            vertx.eventBus().publish(topic, message.toJson(), options);
        }
    }
    
    public void sendMessage(String topic, StnMessage message, 
            Handler<AsyncResult<Message<String>>> handler) {
        
        DeliveryOptions options = new DeliveryOptions();
        options.addHeader(StnMessage.HEADER_CONTENT_TYPE, message.getContentType());
        
        if (vertx != null && vertx.eventBus() != null) {
            vertx.eventBus().send(topic, message.toJson(), options, handler);
        }
    }
    
}