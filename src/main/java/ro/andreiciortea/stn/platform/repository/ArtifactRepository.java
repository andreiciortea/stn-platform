package ro.andreiciortea.stn.platform.repository;

import io.vertx.core.json.JsonObject;


@SuppressWarnings("serial")
class ArtifactNotFoundException extends Exception {}

@SuppressWarnings("serial")
class RepositoryException extends Exception {}


/**
 * 
 * @author <a href="http://andreiciortea.ro">Andrei Ciortea</a>
 *
 */
public interface ArtifactRepository {

    /**
     * Initialize the repository.
     * 
     * @param config A JSON object with configuration options.
     */
    void init(JsonObject config);

    /**
     * Check if the repository contains a given artifact.
     * 
     * @param artifactUri The artifact's IRI.
     * @return True if the artifact was found, false otherwise.
     */
    boolean containsArtifact(String artifactIri);
    
    /**
     * Get a serialization of an artifact in a specified serialization format.
     * 
     * @param artifactUri The artifact's IRI.
     * @param format The serialization format.
     * @return A {@link java.lang.String String} that encodes a serialization of the artifact.
     * @throws ArtifactNotFoundException If the artifact was not found.
     */
    String getArtifact(String artifactIri, String format) throws ArtifactNotFoundException, RepositoryException;
    
    /**
     * Insert an artifact in the repository.
     * 
     * @param artifactIri The artifact's IRI.
     * @param data A {@link java.lang.String String} that encodes a serialization of the artifact.
     * @param format The serialization format.
     * @throws RepositoryException If an exception occurred while accessing the repository.
     */
    void createArtifact(String artifactIri, String data, String format) throws RepositoryException;
    
    /**
     * Update an artifact in the repository.
     * 
     * @param artifactIri The artifact's IRI.
     * @param data A {@link java.lang.String String} that encodes the intended artifact serialization.
     * @param format The serialization format.
     * @throws ArtifactNotFoundException If the artifact does not exist. 
     * @throws RepositoryException If an exception occurred while accessing the repository.
     */
    void updateArtifact(String artifactIri, String data, String format) throws ArtifactNotFoundException, RepositoryException;
    
    /**
     * Delete an artifact from the repository.
     * 
     * @param artifactIri The artifact's IRI.
     * @throws ArtifactNotFoundException If the artifact does not exist.
     * @throws RepositoryException If an exception occurred while accessing the repository.
     */
    void deleteArtifact(String artifactIri) throws ArtifactNotFoundException, RepositoryException;
}
