package ro.andreiciortea.stn.platform.artifact;

import java.util.function.BiFunction;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;

import ro.andreiciortea.stn.platform.api.PlatformIRIManager;
import ro.andreiciortea.stn.vocabulary.STNCore;


public class AddHostingPlatformProcessor implements BiFunction<String, Model, Model> {

    @Override
    public Model apply(String artifactIri, Model model) {
        model.add(ResourceFactory.createResource(artifactIri),
                STNCore.hostedBy,
                ResourceFactory.createResource(PlatformIRIManager.getPlatformIRI())
            );
        
        return model;
    }

}
