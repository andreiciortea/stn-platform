package ro.andreiciortea.stn.platform;

import java.util.UUID;

import io.vertx.core.json.JsonObject;

public class Utils {
    
    public static final String DEFAULT_ARTIFACT_URI_SCHEME = "http";
    public static final String DEFAULT_ARTIFACT_URI_HOST = "localhost";
    public static final String DEFAULT_ARTIFACT_URI_PATH = "/";
    
    public static final int DEFAULT_HTTP_PORT = 8080;
    
    
    public static String buildArtifactBaseUri() {
        return new StringBuilder()
                .append(DEFAULT_ARTIFACT_URI_SCHEME)
                .append("://")
                .append(DEFAULT_ARTIFACT_URI_HOST)
                .append(DEFAULT_ARTIFACT_URI_PATH)
                .toString();
    }
    
    public static String buildArtifactBaseUri(JsonObject config) {
        if (config == null) {
            return buildArtifactBaseUri();
        }
        
        StringBuilder builder = new StringBuilder()
                .append(config.getString("artifact.baseUri.scheme", DEFAULT_ARTIFACT_URI_SCHEME))
                .append("://")
                .append(config.getString("artifact.baseUri.host", DEFAULT_ARTIFACT_URI_HOST))
                .append(":" + config.getInteger("http.port", DEFAULT_HTTP_PORT).toString())
//                .append(":8080")
                .append(config.getString("artifact.baseUri.path", DEFAULT_ARTIFACT_URI_PATH));
        
        return builder.toString();
    }
    
    public static String buildArtifactUri(JsonObject config, String relativeUri) {
        return buildArtifactUri(buildArtifactBaseUri(config), relativeUri);
    }
    
    public static String buildArtifactUri(String baseUri, String relativeUri) {
        if (baseUri.endsWith("/")) {
            return baseUri.substring(0, baseUri.length() - 1).concat(relativeUri);
        } else {
            return baseUri.concat(relativeUri);
        }
    }
    
    public static String genArtifactUri(String baseUri, String containerPath) {
        return genArtifactUri(baseUri, containerPath, null);
    }
    
    public static String genArtifactUri(String baseUri, String containerPath, String slug) {
        String containerUri = buildArtifactUri(baseUri, containerPath);
        
        if (!containerUri.endsWith("/")) {
            containerUri = containerUri.concat("/");
        }
        
        return (slug == null || slug.isEmpty()) ? 
                containerUri.concat(UUID.randomUUID().toString()) : 
                containerUri.concat(slug);
    }
    
}
