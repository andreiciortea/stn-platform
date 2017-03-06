package ro.andreiciortea.stn.platform.api;

import java.util.UUID;


@SuppressWarnings("serial")
class InvalidContainerUriException extends Exception {}

public class ArtifactUriGenerator {

    public static String generateArtifactUri(String containerUri) throws InvalidContainerUriException {
        return generateArtifactUri(containerUri, null);
    }
    
    public static String generateArtifactUri(String containerUri, String slug) throws InvalidContainerUriException {
        if (containerUri == null || containerUri.isEmpty()) {
            throw new InvalidContainerUriException();
        }
        
        if (!containerUri.endsWith("/")) {
            containerUri = containerUri.concat("/");
        }
        
        // TODO: Additional slug validation (e.g., not already in use)
        
        return (slug == null || slug.isEmpty()) ? 
                containerUri.concat(UUID.randomUUID().toString()) : 
                containerUri.concat(slug);
    }
}
