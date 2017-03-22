package ro.andreiciortea.stn.platform.artifact;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;

import ro.andreiciortea.stn.platform.RdfUtils;

public class OptionalStatementsCheckerTest {

    private Model model;
    
    public OptionalStatementsCheckerTest() {
        model = RdfUtils.stringToRdfModel(
                        "<http://example.org> a <http://1.example.org>, <http://2.example.org> .", 
                        RdfUtils.TURTLE
                    );
        
        
        
    }
    
    @Test
    public void testEmptyStatementsList() {
/*        ArtifactValidator validator = new AcceptableStatementsValidator(
                new ArrayList<Statement>(),
                new BasicArtifactValidator(new ArrayList<Statement>())
            );
        
        assertTrue(validator.validate(model));*/
    }
    
/*    @Test
    public void testNullStatementsList() {
        ArtifactValidator validator = new AcceptableStatementsValidator(
                null,
                new BasicArtifactValidator(new ArrayList<Statement>())
            );
        
        assertTrue(validator.validate(model));
    }
    
    @Test
    public void testNullModel() {
        ArtifactValidator validator = new AcceptableStatementsValidator(
                new ArrayList<Statement>(),
                new BasicArtifactValidator(new ArrayList<Statement>())
            );
        
        assertTrue(validator.validate(null));
    }
    
    @Test
    public void testsAcceptableStatements() {
        List<Statement> list = new ArrayList<Statement>();
        
        list.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://example.org"), 
                RDF.type, 
                ResourceFactory.createResource("http://1.example.org")
            )
        );
        
        list.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://example.org"), 
                RDF.type, 
                ResourceFactory.createResource("http://2.example.org")
            )
        );
        
        ArtifactValidator validator = new AcceptableStatementsValidator(
                list,
                new BasicArtifactValidator(new ArrayList<Statement>())
            );
        
        assertTrue(validator.validate(model));
    }
    
    @Test
    public void testNotAcceptableStatements() {
        List<Statement> list = new ArrayList<Statement>();
        
        list.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://example.org"), 
                RDF.type, 
                ResourceFactory.createResource("http://1.example.org")
            )
        );
        
        ArtifactValidator validator = new AcceptableStatementsValidator(
                list,
                new BasicArtifactValidator(new ArrayList<Statement>())
            );
        
        assertFalse(validator.validate(model));
    }
    */
}
