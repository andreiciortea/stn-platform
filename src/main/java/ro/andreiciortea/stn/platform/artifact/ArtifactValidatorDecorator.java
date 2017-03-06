package ro.andreiciortea.stn.platform.artifact;

import org.apache.jena.rdf.model.Model;

public class ArtifactValidatorDecorator implements ArtifactValidator {

    private ArtifactValidator decoratedValidator;
    
    public ArtifactValidatorDecorator(ArtifactValidator decoratedValidator) {
        this.decoratedValidator = decoratedValidator;
    }
    
    @Override
    public boolean validate(Model model) {
        return decoratedValidator.validate(model);
    }
    
}
