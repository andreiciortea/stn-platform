package ro.andreiciortea.stn.platform.artifact;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;


public class BasicArtifactValidator implements ArtifactValidator {
    
    List<Statement> requiredStatements;
    
    public BasicArtifactValidator(List<Statement> requiredStatements) {
        this.requiredStatements = requiredStatements;
    }
    
    @Override
    public boolean validate(Model model) {
        if (requiredStatements == null || model == null) {
            return true;
        }
        
        for (Statement s : requiredStatements) {
            if (!model.contains(s)) {
                return false;
            }
        }
        
        return true;
    }
}
