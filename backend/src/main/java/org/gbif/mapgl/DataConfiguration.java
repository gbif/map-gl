package org.gbif.mapgl;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Application configuration with sensible defaults if applicable.
 */
public class DataConfiguration {

  @JsonProperty
  @NotNull
  public String source;

  @JsonProperty
  @NotNull
  public int zooms;

  @JsonProperty
  @NotNull
  public int rowLimit = Integer.MAX_VALUE;
}
