package ro.andreiciortea.stn.platform.artifact;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class AcceptableStatementsValidator extends ArtifactValidatorDecorator {
    
    List<Statement> acceptableStatements;
    
    public AcceptableStatementsValidator(List<Statement> acceptableStatements, ArtifactValidator decoratedValidator) {
        super(decoratedValidator);
        this.acceptableStatements = acceptableStatements;
    }
    
    @Override
    public boolean validate(Model model) {
        if (acceptableStatements == null || acceptableStatements.isEmpty() || model == null) {
            return super.validate(model);
        }
        
        StmtIterator it = model.listStatements();
        
        while (it.hasNext()) {
            Statement s = it.next();
            if (!acceptableStatements.contains(s)) {
                return false;
            }
        }
        
        return super.validate(model);
    }

}
