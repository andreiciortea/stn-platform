package ro.andreiciortea.stn.platform.artifact;

import java.util.List;
import java.util.function.BiPredicate;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;


public class RequiredStatementsChecker implements BiPredicate<Model, List<Statement>> {
    
    @Override
    public boolean test(Model model, List<Statement> requiredStatements) {
        if (requiredStatements == null || requiredStatements.isEmpty()) {
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
