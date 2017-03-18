package ro.andreiciortea.stn.platform.eventbus;

public class ArtifactResponse extends RepositoryResponse {
    
    private String artifactIri;
    private String artifactStr;
    
    public ArtifactResponse(int code, String artifactUri) {
        this(code, artifactUri, null);
    }
    
    public ArtifactResponse(int statusCode, String artifactIri, String artifactStr) {
        super(statusCode);
        
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
