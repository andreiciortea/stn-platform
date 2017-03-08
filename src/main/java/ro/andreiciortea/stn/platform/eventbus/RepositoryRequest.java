package ro.andreiciortea.stn.platform.eventbus;

public class RepositoryRequest extends BusMessage {
    
    public static final String CONTENT_TYPE = "repository-request";
    
    public static final String HEAD = "HEAD";
    public static final String GET = "GET";
    public static final String PUT = "PUT";
    public static final String POST = "POST";
    public static final String DELETE = "DELETE";
    public static final String QUERY = "QUERY";
    
    private String verb;
    private String artifactUri;
    private String artifactStr;
    
    
    public RepositoryRequest() {
        this(null, null, null);
    }
    
    public RepositoryRequest(String verb, String artifactUri) {
        this(verb, artifactUri, null);
    }
    
    public RepositoryRequest(String verb, String artifactUri, String artifactStr) {
        this.verb = verb;
        this.artifactUri = artifactUri;
        this.artifactStr = artifactStr;
    }
    
    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }
    
    public String getVerb() {
        return this.verb;
    }
    
    public String getArtifactUri() {
        return this.artifactUri;
    }
    
    public String getArtifactStr() {
        return this.artifactStr;
    }
}
