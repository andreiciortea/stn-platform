package ro.andreiciortea.stn.platform.eventbus;

public class AgentNotification extends BusMessage {
    
    private Iterable<String> agentIRIs;
    private String callbackIRI;
    private String payload;
    
    public AgentNotification(Iterable<String> agentIRIs, String payload) {
        this.agentIRIs = agentIRIs;
        this.payload = payload;
    }
    
    public Iterable<String> getObserverIRI() {
        return this.agentIRIs;
    }
    
    public String getCallbackIRI() {
        return this.callbackIRI;
    }
    
    public String getPayload() {
        return this.payload;
    }
}
