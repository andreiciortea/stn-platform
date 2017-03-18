package ro.andreiciortea.stn.platform.eventbus;

public class ObserverNotification extends BusMessage {
    
    private String obseverIRI;
    private String callbackIRI;
    private String payload;
    
    public ObserverNotification(String observerIri, String payload) {
        this.obseverIRI = observerIri;
        this.payload = payload;
    }
    
    public String getObserverIRI() {
        return this.obseverIRI;
    }
    
    public String getCallbackIRI() {
        return this.callbackIRI;
    }
    
    public String getPayload() {
        return this.payload;
    }
}
