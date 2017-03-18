package ro.andreiciortea.stn.platform.eventbus;

public class SparqlResponse extends RepositoryResponse {
    
    private String payload;
    
    public SparqlResponse(int statusCode) {
        super(statusCode);
    }
    
    public String getPayload() {
        return this.payload;
    }
}
