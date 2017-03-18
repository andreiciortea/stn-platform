package ro.andreiciortea.stn.platform.eventbus;

public class ArtifactNotification extends BusMessage {
    
    String artifactIRI;
    String artifactType;
    String artifactStr;
    
    public ArtifactNotification(String artifactIri, String artifactType, String artifactStr) {
        this.artifactIRI = artifactIri;
        this.artifactType = artifactType;
        this.artifactStr = artifactStr;
    }
    
    public String getArtifactIRI() {
        return this.artifactIRI;
    }
    
    public String getArtifactType() {
        return this.artifactType;
    }
    
    public String getArtifactAsString() {
        return this.artifactStr;
    }
    
}
