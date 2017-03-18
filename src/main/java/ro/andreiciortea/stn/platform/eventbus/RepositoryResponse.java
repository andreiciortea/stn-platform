package ro.andreiciortea.stn.platform.eventbus;

public class RepositoryResponse extends BusMessage {

    private int statusCode;
    
    public RepositoryResponse(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public int getStatusCode() {
        return this.statusCode;
    }
}
