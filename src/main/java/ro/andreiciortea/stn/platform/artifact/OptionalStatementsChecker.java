package ro.andreiciortea.stn.platform.artifact;

import java.util.List;
import java.util.function.BiPredicate;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class OptionalStatementsChecker implements BiPredicate<Model, List<Statement>> {

    @Override
    public boolean test(Model model, List<Statement> acceptableStatements) {
        if (acceptableStatements == null || acceptableStatements.isEmpty() || model == null) {
            return true;
        }
        
        StmtIterator it = model.listStatements();
        
        while (it.hasNext()) {
            Statement s = it.next();
            if (!acceptableStatements.contains(s)) {
                return false;
            }
        }
        
        return true;
    }

}
