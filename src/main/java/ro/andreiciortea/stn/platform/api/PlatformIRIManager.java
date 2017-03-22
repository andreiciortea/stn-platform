package ro.andreiciortea.stn.platform.api;

import java.util.UUID;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;


@SuppressWarnings("serial")
class InvalidContainerUriException extends Exception {}


public class PlatformIRIManager {
    
    public static String getPlatformIRI() {
        JsonObject config = Vertx.currentContext().config();
        
        return config.getString("platformIRI", "http://example.org/.well-known/stn#platform");
    }
    
    public static String generateArtifactIRI(String containerIRI) throws InvalidContainerUriException {
        return generateArtifactIRI(containerIRI, null);
    }
    
    public static String generateArtifactIRI(String containerIRI, String slug) throws InvalidContainerUriException {
        if (containerIRI == null || containerIRI.isEmpty()) {
            throw new InvalidContainerUriException();
        }
        
        if (!containerIRI.endsWith("/")) {
            containerIRI = containerIRI.concat("/");
        }
        
        // TODO: Additional slug validation (e.g., not already in use)
        
        return (slug == null || slug.isEmpty()) ? 
                containerIRI.concat(UUID.randomUUID().toString()) : 
                containerIRI.concat(slug);
    }
}
