package ro.andreiciortea.stn.platform;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import ro.andreiciortea.stn.platform.agent.JaCaMoProxyVerticle;
import ro.andreiciortea.stn.platform.api.HttpServerVerticle;
import ro.andreiciortea.stn.platform.notification.NotificationServiceVerticle;
import ro.andreiciortea.stn.platform.repository.RepositoryServiceVerticle;

public class HubStarterVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.deployVerticle(new RepositoryServiceVerticle(), 
                new DeploymentOptions().setWorker(true).setConfig(config())
            );

        vertx.deployVerticle(new NotificationServiceVerticle(),
                new DeploymentOptions().setConfig(config())
            );
        
//        vertx.deployVerticle(new StnHttpServerVerticle(),
//                new DeploymentOptions().setConfig(config())
//            );
        
        vertx.deployVerticle(new HttpServerVerticle(),
                new DeploymentOptions().setConfig(config())
            );
        
        vertx.deployVerticle(new JaCaMoProxyVerticle(),
                new DeploymentOptions().setConfig(config())
            );
    }
    
}
