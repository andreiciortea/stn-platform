package ro.andreiciortea.stn.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class STNCore {

    private static final String STN_PREFIX = "http://purl.org/stn/core#";

    public static Resource resource(String fragment) {
        return ResourceFactory.createResource(STN_PREFIX + fragment);
    }
    
    public static Property property(String fragment) {
        return ResourceFactory.createProperty(STN_PREFIX + fragment);
    }
    
    public static final Resource DigitalArtifact    = resource("DigitalArtifact");
    public static final Resource UserAccount        = resource("UserAccount");
    public static final Resource Relation           = resource("Relation");
    public static final Resource Message            = resource("Message");
    
    public static final Property connectedTo        = property("connectedTo");
    public static final Property source             = property("source");
    public static final Property target             = property("target");
    
    public static final Property callbackUri        = property("callbackUri");
    
    public static final Property heldBy             = property("heldBy");
    public static final Property ownedBy            = property("ownedBy");
    
    public static final Property hasBody            = property("hasBody");
    public static final Property hasSender          = property("hasSender");
    public static final Property hasReceiver        = property("hasReceiver");
}
