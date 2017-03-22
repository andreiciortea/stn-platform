package ro.andreiciortea.stn.platform.artifact;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

/**
 * 
 * @author <a href="http://andreiciortea.ro">Andrei Ciortea</a>
 *
 */
public class DefaultArtifactValidator implements ArtifactValidator {

    private String[] artifactClassIRIs;
    
    public DefaultArtifactValidator(String... artifactClassIRIs) {
        this.artifactClassIRIs = artifactClassIRIs;
    }
    
    @Override
    public boolean validate(String artifactIRI, Model model) {
        List<Statement> requiredStatements = new ArrayList<Statement>();
        
        for (String iri : artifactClassIRIs) {
            requiredStatements.add(ResourceFactory.createStatement(
                    ResourceFactory.createResource(artifactIRI), 
                    RDF.type, 
                    ResourceFactory.createResource(iri)
                )
            );
        }
        
        RequiredStatementsChecker validator = new RequiredStatementsChecker();
        
        return validator.test(model, requiredStatements);
    }

}
