package ro.andreiciortea.stn.platform.eventbus;

public class RepositoryRequest extends BusMessage {
    
    public static final String HEAD = "HEAD";
    public static final String GET = "GET";
    public static final String PUT = "PUT";
    public static final String POST = "POST";
    public static final String DELETE = "DELETE";
    public static final String QUERY = "QUERY";
    
    private String verb;
    
    public RepositoryRequest(String verb) {
        this.verb = verb;
    }
    
    public String getVerb() {
        return this.verb;
    }
}
