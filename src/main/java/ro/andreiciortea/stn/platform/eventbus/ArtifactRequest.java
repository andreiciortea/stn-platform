package ro.andreiciortea.stn.platform.eventbus;

public class ArtifactRequest extends RepositoryRequest {
    
    private String artifactIri;
    private String artifactStr;
    
    public ArtifactRequest(String verb, String artifactUri) {
        this(verb, artifactUri, null);
    }
    
    public ArtifactRequest(String verb, String artifactIri, String artifactStr) {
        super(verb);
        
        this.artifactIri = artifactIri;
        this.artifactStr = artifactStr;
    }
    
    public String getArtifactIri() {
        return this.artifactIri;
    }
    
    public String getArtifactAsString() {
        return this.artifactStr;
    }
    
}
