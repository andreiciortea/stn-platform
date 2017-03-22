package ro.andreiciortea.stn.platform.artifact;

import org.apache.jena.rdf.model.Model;

public interface ArtifactProcessor {
    
    Model process(String artifactIRI, Model model);
    
}
