package ro.andreiciortea.stn.platform.eventbus;

public class RepositoryResponse extends BusMessage {
    
    public static final String CONTENT_TYPE = "repository-response";
    
    private int statusCode;
    private String artifactUri;
    private String artifactStr;
    
    
    public RepositoryResponse() {
        this (-1, null, null);
    }
    
    public RepositoryResponse(int code, String artifactUri) {
        this(code, artifactUri, null);
    }
    
    public RepositoryResponse(int code, String artifactUri, String artifactStr) {
        this.statusCode = code;
        this.artifactUri = artifactUri;
        this.artifactStr = artifactStr;
    }
    
    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }
    
    public int getStatusCode() {
        return this.statusCode;
    }
    
    public String getArtifactUri() {
        return this.artifactUri;
    }
    
    public String getArtifactAsString() {
        return this.artifactStr;
    }

}
