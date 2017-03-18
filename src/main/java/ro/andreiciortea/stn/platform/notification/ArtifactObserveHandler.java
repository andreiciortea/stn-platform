package ro.andreiciortea.stn.platform.notification;

import java.util.List;

public interface ArtifactObserveHandler {
    
    List<AgentCard> getObservers(String artifactIRI, String artifactStr);
    
}
