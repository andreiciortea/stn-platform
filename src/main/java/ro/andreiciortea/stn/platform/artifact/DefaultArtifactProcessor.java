package ro.andreiciortea.stn.platform.artifact;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;

import ro.andreiciortea.stn.platform.api.PlatformIRIManager;
import ro.andreiciortea.stn.vocabulary.STNCore;

public class DefaultArtifactProcessor implements ArtifactProcessor {

    @Override
    public Model process(String artifactIRI, Model model) {
        model.add(ResourceFactory.createResource(artifactIRI),
                STNCore.hostedBy,
                ResourceFactory.createResource(PlatformIRIManager.getPlatformIRI())
            );
        
        return model;
    }

}
