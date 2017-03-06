package ro.andreiciortea.stn.platform.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import ro.andreiciortea.stn.platform.RdfUtils;
import ro.andreiciortea.stn.platform.artifact.ArtifactValidator;
import ro.andreiciortea.stn.platform.artifact.BasicArtifactValidator;
import ro.andreiciortea.stn.vocabulary.STNCore;


public class UserAccountHandler extends ArtifactHandler {
    
    @Override
    public String processArtifactRepresentation(String artifactUri, 
            String representation, String format) throws InvalidArtifactRepresentationException {
        
        List<Statement> requiredStatements = new ArrayList<Statement>();
        requiredStatements.add(ResourceFactory.createStatement(
                ResourceFactory.createResource(artifactUri), 
                RDF.type, 
                STNCore.UserAccount
            )
        );
        
        ArtifactValidator validator = new BasicArtifactValidator(requiredStatements);
        
        try {
            if (validator.validate(RdfUtils.stringToRdfModel(representation, format))) {
                return representation;
            } else {
                throw new InvalidArtifactRepresentationException();
            }
        } catch (Exception e) {
            throw new InvalidArtifactRepresentationException();
        }
    }
    
}
