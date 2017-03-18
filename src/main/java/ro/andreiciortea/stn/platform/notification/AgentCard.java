package ro.andreiciortea.stn.platform.notification;

public class AgentCard {

    private String agentIRI;
    private String callbackIRI;
    
    public AgentCard(String agentIRI, String callbackIRI) {
        this.agentIRI = agentIRI;
        this.callbackIRI = callbackIRI;
    }
    
    public String getAgentIRI() {
        return this.agentIRI;
    }
    
    public String getCallbackIRI() {
        return this.callbackIRI;
    }
}
