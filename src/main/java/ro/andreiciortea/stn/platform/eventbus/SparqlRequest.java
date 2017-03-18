package ro.andreiciortea.stn.platform.eventbus;

public class SparqlRequest extends RepositoryRequest {
    
    private String query;
    
    public SparqlRequest(String query) {
        super(RepositoryRequest.QUERY);
        
        this.query = query;
    }
    
    public String getQuery() {
        return this.query;
    }
}
