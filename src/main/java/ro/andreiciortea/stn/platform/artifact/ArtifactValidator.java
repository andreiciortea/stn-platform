package ro.andreiciortea.stn.platform.artifact;

import org.apache.jena.rdf.model.Model;

public interface ArtifactValidator {

    boolean validate(Model model);
    
}
