package ro.andreiciortea.stn.platform.artifact;

import org.apache.http.HttpStatus;
import org.apache.jena.rdf.model.Model;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import ro.andreiciortea.stn.platform.RdfUtils;
import ro.andreiciortea.stn.platform.api.InvalidArtifactRepresentationException;
import ro.andreiciortea.stn.platform.api.PlatformIRIManager;
import ro.andreiciortea.stn.platform.eventbus.ArtifactNotification;
import ro.andreiciortea.stn.platform.eventbus.ArtifactRequest;
import ro.andreiciortea.stn.platform.eventbus.RepositoryRequest;
import ro.andreiciortea.stn.platform.eventbus.SparqlRequest;
import ro.andreiciortea.stn.platform.notification.NotificationService;
import ro.andreiciortea.stn.platform.repository.RepositoryServiceVerticle;

/**
 * 
 * @author <a href="http://andreiciortea.ro">Andrei Ciortea</a>
 *
 */
public class ArtifactHandler {
    
    public static final String CONTENT_TYPE_TURTLE = "text/turtle"; 
    
    private String artifactClassIRI;
    
    private boolean persistent = true;
    private boolean observable = false;
    
    private Vertx vertx = Vertx.currentContext().owner();
    
    private ArtifactValidator validator;
    private ArtifactProcessor processor;
    
    public ArtifactHandler(String artifactClassIRI) {
        this.artifactClassIRI = artifactClassIRI;
        
        this.validator = new DefaultArtifactValidator(artifactClassIRI);
        this.processor = new DefaultArtifactProcessor();
    }
    
    public ArtifactHandler withoutStorage() {
        persistent = false;
        return this;
    }
    
    public ArtifactHandler withObserve() {
        observable = true;
        return this;
    }
    
    public ArtifactHandler setValidator(ArtifactValidator validator) {
        this.validator = validator;
        return this;
    }
    
    public ArtifactHandler setProcessor(ArtifactProcessor processor) {
        this.processor = processor;
        return this;
    }
    
    public void handleGetFilteredCollection(String query, Handler<AsyncResult<Message<String>>> handler) {
        SparqlRequest request = new SparqlRequest(query);
        sendRepositoryRequest(request, handler);
    }
    
    public void handleGetArtifact(String artifactIRI, Handler<AsyncResult<Message<String>>> handler) {
        ArtifactRequest repositoryRequest = new ArtifactRequest(ArtifactRequest.GET, artifactIRI);
        sendRepositoryRequest(repositoryRequest, handler);
    }
    
    public void handlePutArtifact(String artifactIRI, String artifactRepresentation, Handler<AsyncResult<Message<String>>> handler) {
        try {
            artifactRepresentation = replaceNullRealtiveURIs(artifactIRI, artifactRepresentation, RdfUtils.TURTLE);
            artifactRepresentation = processArtifactRepresentation(artifactIRI, artifactRepresentation, RdfUtils.TURTLE);
            
            if (persistent) {
                persistArtifact(ArtifactRequest.PUT, artifactIRI, artifactRepresentation, HttpStatus.SC_OK, handler);
            }
            
            if (observable) {
                publishNotification(artifactIRI, artifactRepresentation);
            }
        } catch (Exception e) {
            handler.handle(Future.failedFuture(e));
        }
    }
    
    public void handlePostArtifact(String containerIRI, String slug, String artifactRepresentation, Handler<AsyncResult<Message<String>>> handler) {
        try {
            String artifactUri = PlatformIRIManager.generateArtifactIRI(containerIRI, slug);
            artifactRepresentation = replaceNullRealtiveURIs(artifactUri, artifactRepresentation, RdfUtils.TURTLE);
            
            artifactRepresentation = processArtifactRepresentation(artifactUri, artifactRepresentation, RdfUtils.TURTLE);
            
            if (persistent) {
                persistArtifact(ArtifactRequest.POST, artifactUri, artifactRepresentation, HttpStatus.SC_CREATED, handler);
            }
            
            if (observable) {
                publishNotification(artifactUri, artifactRepresentation);
            }
        } catch (Exception e) {
            handler.handle(Future.failedFuture(e));
        }
    }
    
    public void handleDeleteArtifact(String artifactIRI, Handler<AsyncResult<Message<String>>> handler) {
        ArtifactRequest repositoryRequest = new ArtifactRequest(ArtifactRequest.DELETE, artifactIRI);
        
        if (persistent) {
            sendRepositoryRequest(repositoryRequest, handler);
        }
    }
    
    private String replaceNullRealtiveURIs(String artifactUri, String representation, String format) {
        // TODO: this method is not closed for modifications
        if (format.compareToIgnoreCase(RdfUtils.TURTLE) == 0) {
            return representation.replaceAll("<>", "<" + artifactUri + ">");
        }
        
        return representation;
    }
    
    private void persistArtifact(String verb, String artifactUri, 
            String artifactStr, int successStatusCode, Handler<AsyncResult<Message<String>>> handler) {
        
        ArtifactRequest repositoryRequest = new ArtifactRequest(verb, artifactUri, artifactStr);
        
        sendRepositoryRequest(repositoryRequest, handler);
    }
    
    private void sendRepositoryRequest(RepositoryRequest request, Handler<AsyncResult<Message<String>>> handler) {
        vertx.eventBus().send(RepositoryServiceVerticle.REPOSITORY_BUS_ADDRESS, request.toJson(), handler);
    }
    
    private void publishNotification(String artifactIRI, String artifactStr) {
        vertx.eventBus().publish(NotificationService.EVENT_BUS_ADDRESS, 
                (new ArtifactNotification(artifactIRI, this.artifactClassIRI, artifactStr)).toJson());
    }
    
    private String processArtifactRepresentation(String artifactIRI, 
            String representation, String format) throws InvalidArtifactRepresentationException {
        
        try {
            
            Model model = RdfUtils.stringToRdfModel(representation, format);
            
            if (!validator.validate(artifactIRI, model)) {
                throw new InvalidArtifactRepresentationException();
            }
            
            model = processor.process(artifactIRI, model);
            
            return RdfUtils.rdfModelToString(model, RdfUtils.TURTLE);
            
        } catch (Exception e) {
            throw new InvalidArtifactRepresentationException();
        }
    }

}
