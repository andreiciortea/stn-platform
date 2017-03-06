package ro.andreiciortea.stn.platform;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class RdfUtils {

    public static final String TURTLE = "turtle";
    
    
    public static Model stringToRdfModel(String representation, String format) {
        if (representation == null) {
            throw new IllegalArgumentException("Representation cannot be null.");
        }
        
        InputStream stream = new ByteArrayInputStream(representation.getBytes(StandardCharsets.UTF_8));
        
        return ModelFactory.createDefaultModel().read(stream, null, format);
    }
    
    public static String rdfModelToString(Model model, String format) {
        StringWriter sw = new StringWriter();
        model.write(sw, format);
        
        return sw.toString();
    }
}
