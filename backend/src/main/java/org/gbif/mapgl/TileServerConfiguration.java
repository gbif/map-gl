package org.gbif.mapgl;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

/**
 * Application configuration with sensible defaults if applicable.
 */
public class TileServerConfiguration extends Configuration {
  @Valid
  @JsonProperty("data")
  public DataConfiguration dataConfiguration;

  @NotNull
  @JsonProperty()
  public int tileSize;
}
