package ro.andreiciortea.stn.platform.artifact;

import org.apache.jena.rdf.model.Model;

public class ArtifactProcessorDecorator implements ArtifactProcessor {

    private ArtifactProcessor artifactProcessor;
    
    public ArtifactProcessorDecorator(ArtifactProcessor decoratedProcessor) {
        this.artifactProcessor = decoratedProcessor;
    }
    
    @Override
    public Model process(Model model) {
        return artifactProcessor.process(model);
    }

}
