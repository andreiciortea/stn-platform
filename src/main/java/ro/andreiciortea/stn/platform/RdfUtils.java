package ro.andreiciortea.stn.platform;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class RdfUtils {
    
    public static final String TURTLE = "TURTLE";
    public static final String JSON_LD = "JSON-LD";
    public static final String RDF_XML = "RDF/XML";
    
    public enum RdfFormat {
        TURTLE(0),
        JSON_LD(1),
        RFX_XML(2);
        
        public final int value;
        
        RdfFormat(int value) {
            this.value = value;
        }
        
        public static RdfFormat valueOf(final int value) {
            switch (value) {
                case 0: return TURTLE;
                case 1: return JSON_LD;
                case 2: return RFX_XML;
                default: throw new IllegalArgumentException("RDF format for value " + value);
            }
        }
    }
    
    
    private RdfUtils() { }
    
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
