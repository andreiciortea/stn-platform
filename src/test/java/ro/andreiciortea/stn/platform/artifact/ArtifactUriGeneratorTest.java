package ro.andreiciortea.stn.platform.artifact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ro.andreiciortea.stn.platform.api.ArtifactUriGenerator;
import ro.andreiciortea.stn.platform.api.InvalidContainerUriException;

public class ArtifactUriGeneratorTest {

    @Test
    public void testWithSlug() throws InvalidContainerUriException {
        String uri = ArtifactUriGenerator.generateArtifactUri("http://example.org/test/", "myresource");
        
        assertEquals(uri, "http://example.org/test/myresource");
    }
    
    @Test
    public void testNoEndingSlash() throws InvalidContainerUriException {
        String uri = ArtifactUriGenerator.generateArtifactUri("http://example.org/test", "myresource");
        
        assertEquals(uri, "http://example.org/test/myresource");
    }
    
    @Test(expected=InvalidContainerUriException.class)
    public void testNullContainerUri() throws InvalidContainerUriException {
        ArtifactUriGenerator.generateArtifactUri(null, "myresource");
    }
    
    @Test(expected=InvalidContainerUriException.class)
    public void testEmptyContainerUri() throws InvalidContainerUriException {
        ArtifactUriGenerator.generateArtifactUri("", "myresource");
    }
    
    @Test
    public void testNullSlug() throws InvalidContainerUriException {
        String uri = ArtifactUriGenerator.generateArtifactUri("http://example.org/test", null);
        
        assertNotNull(uri);
        assertTrue(uri.startsWith("http://example.org/test/"));
    }
    
    @Test
    public void testEmptySlug() throws InvalidContainerUriException {
        String uri = ArtifactUriGenerator.generateArtifactUri("http://example.org/test", "");
        
        assertNotNull(uri);
        assertTrue(uri.startsWith("http://example.org/test/"));
    }
}
