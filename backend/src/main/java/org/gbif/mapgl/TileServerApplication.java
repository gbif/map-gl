package org.gbif.mapgl;

import org.gbif.mapgl.index.InMemoryFromCSVIndex;
import org.gbif.mapgl.resource.TileResource;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * The main entry point for running the member node.
 */
public class TileServerApplication extends Application<TileServerConfiguration> {

  private static final String APPLICATION_NAME = "Datacube Tile Server";

  public static void main(String[] args) throws Exception {
    new TileServerApplication().run(args);
  }

  @Override
  public String getName() {
    return APPLICATION_NAME;
  }

  @Override
  public final void initialize(Bootstrap<TileServerConfiguration> bootstrap) {
    bootstrap.addBundle(new AssetsBundle("/assets", "/", "index.html"));
  }

  @Override
  public final void run(TileServerConfiguration configuration, Environment environment) {
    environment.jersey().setUrlPattern("/api/*");
    environment.jersey().register(new TileResource(new InMemoryFromCSVIndex(configuration)));
  }
}
