package ro.andreiciortea.stn.platform.artifact;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

import ro.andreiciortea.stn.platform.RdfUtils;
import ro.andreiciortea.stn.vocabulary.STNCore;

public class BasicArtifactValidatorTest {
    
    private String artifactUri = "http://example.org#artifact";
    
    private Model model;
    
    List<Statement> requiredOneStatement;
    List<Statement> requiredTwoStatements;
    
    
    public BasicArtifactValidatorTest() {
        model = RdfUtils.stringToRdfModel("@prefix stn: <" + STNCore.STN_PREFIX + "> ."
                + "<" + artifactUri + "> a stn:UserAccount, stn:DigitalArtifact .", RdfUtils.TURTLE);
        
        requiredOneStatement = new ArrayList<Statement>();
        
        requiredOneStatement.add(ResourceFactory.createStatement(
                ResourceFactory.createResource(artifactUri), 
                RDF.type, 
                STNCore.UserAccount
            )
        );
        
        requiredTwoStatements = new ArrayList<Statement>();
        requiredTwoStatements.addAll(requiredOneStatement);
        
        requiredTwoStatements.add(ResourceFactory.createStatement(
                ResourceFactory.createResource(artifactUri), 
                RDF.type, 
                STNCore.DigitalArtifact
            )
        );
    }    
    
    @Test
    public void testEmptyTripleList() {
        BasicArtifactValidator validator = new BasicArtifactValidator(new ArrayList<Statement>());
        assertTrue(validator.validate(model));
    }
    
    @Test
    public void testNullTripleList() {
        BasicArtifactValidator validator = new BasicArtifactValidator(null);
        assertTrue(validator.validate(model));
    }
    
    @Test
    public void testNullModel() {
        BasicArtifactValidator validator = new BasicArtifactValidator(new ArrayList<Statement>());
        assertTrue(validator.validate(null));
    }
    
    @Test
    public void testContainsOneTriple() {
        BasicArtifactValidator validator = new BasicArtifactValidator(requiredOneStatement);
        assertTrue(validator.validate(model));
    }
    
    @Test
    public void testContainsTwoTriples() {
        BasicArtifactValidator validator = new BasicArtifactValidator(requiredTwoStatements);
        assertTrue(validator.validate(model));
    }
    
    @Test
    public void testDoesNotContainTriples() {
        Model model = RdfUtils.stringToRdfModel("<" + artifactUri + "> a <http://example.com> .", RdfUtils.TURTLE);
        
        BasicArtifactValidator validator = new BasicArtifactValidator(requiredTwoStatements);
        assertFalse(validator.validate(model));
    }
    
}
