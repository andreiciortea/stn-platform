package ro.andreiciortea.stn.platform.artifact;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;

import ro.andreiciortea.stn.platform.repository.RepositoryService;


public class DigitalArtifactModelValidator {
    
    /**
     * Process an RDF serialization of a digital artifact in a given 
     * RDF serialization format and using a given data model validator.
     * 
     * @param representation
     * @param serializationFormat
     * @param validator
     * @return
     */
    public static String cleanseModel(DigitalArtifactModel artifactModel, String artifactUri, String representation, String serializationFormat) {
        
        InputStream stream = new ByteArrayInputStream(
                representation
                    .replaceAll("<>", "<" + artifactUri + ">")
                    .getBytes(StandardCharsets.UTF_8)
            );

        Model model = ModelFactory.createDefaultModel().read(stream, null, serializationFormat);
        
        // TODO: deal with platform-managed properties
        
        Model cleansedModel = validate(artifactUri, model, artifactModel);
        
        return (cleansedModel == null) ? null : modelToString(cleansedModel);
    }
    
    private static Model validate(String artifactUri, Model model, DigitalArtifactModel artifactModel) {
        List<Statement> requiredStatements = artifactModel.getRequiredStatements(artifactUri);
        
        boolean valid = true;
        
        for (Statement s : requiredStatements) {
            if (!model.contains(s)) {
                valid = false;
            }
        }
        
        return (valid) ? model : null;
    }
    
    private static String modelToString(Model model) {
        return modelToString(model, RepositoryService.TURTLE);
    }
    
    public static String modelToString(Model model, String serializationFormat) {
        StringWriter sw = new StringWriter();
        model.write(sw, serializationFormat);
        
        return sw.toString();
    }
}
