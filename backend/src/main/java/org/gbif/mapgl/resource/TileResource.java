package org.gbif.mapgl.resource;

import org.gbif.mapgl.index.TileIndex;
import org.gbif.mapgl.index.XYZ;
import org.gbif.mapgl.v1.DatacubeTileProto;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.sun.jersey.spi.resource.Singleton;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import no.ecc.vectortile.VectorTileEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple resource that returns a tiles rendered in Datacube or VectorTileFormat.
 */
@Path("/")
@Singleton
public final class TileResource {
  private static final Logger LOG = LoggerFactory.getLogger(TileResource.class);
  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
  private static final String LAYER_NAME = "occurrence";
  private static final String COUNT_FEATURE_ = "count";

  private final TileIndex<AtomicInteger> tileIndex;

  public TileResource(TileIndex<AtomicInteger> tileIndex) {
    this.tileIndex = tileIndex;
  }

  // Cache to avoid building the tiles multiple times
  private final LoadingCache<XYZ, byte[]> VT_CACHE =
    CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(
                  new CacheLoader<XYZ, byte[]>() {
                    public byte[] load(XYZ key) {
                      return toVectorTile(tileIndex.getTile(key.getX(), key.getY(), key.getZ()));
                    }
                  }
                );
  private final LoadingCache<XYZ, byte[]> DC_CACHE =
    CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(
                  new CacheLoader<XYZ, byte[]>() {
                    public byte[] load(XYZ key) {
                      return toDataCube(tileIndex.getTile(key.getX(), key.getY(), key.getZ()));
                    }
                  }
                );


  @GET
  @Path("{z}/{x}/{y}/datacube.pbf")
  @Timed
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public byte[] asDatacubeFormat(@PathParam("z") int z, @PathParam("x") int x, @PathParam("y") int y,
                                 @Context HttpServletResponse response) {
    try {
      addAllAccessControlHeader(response);
      return DC_CACHE.get(new XYZ(x,y,z));
    } catch (ExecutionException e) {
      throw Throwables.propagate(e);
    }
  }

  @GET
  @Path("{z}/{x}/{y}/vectortile.pbf")
  @Timed
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public byte[] asVectorTileFormat(@PathParam("z") int z, @PathParam("x") int x, @PathParam("y") int y,
                                   @Context HttpServletResponse response) {
    try {
      addAllAccessControlHeader(response);
      return VT_CACHE.get(new XYZ(x,y,z));
    } catch (ExecutionException e) {
      throw Throwables.propagate(e);
    }
  }

  private byte[] toDataCube(Map<Point, AtomicInteger> tile) {
    DatacubeTileProto.DatacubeTile.Builder b = DatacubeTileProto.DatacubeTile.newBuilder();
    DatacubeTileProto.DatacubeTile.Channel.Builder c = DatacubeTileProto.DatacubeTile.Channel.newBuilder();
    for (Map.Entry<Point, AtomicInteger> e : tile.entrySet()) {
      b.addX(e.getKey().x);
      b.addY(e.getKey().y);
      c.addValue(e.getValue().get());
    }
    b.addChannel(c.build());
    return b.build().toByteArray();
  }

  private byte[] toVectorTile(Map<Point, AtomicInteger> tile) {
    // encoder with edge clipping (0) and addressed in absolute tile-space with no projection
    VectorTileEncoder encoder = new VectorTileEncoder(tileIndex.getTileSize(), 0, false);
    for (Map.Entry<Point, AtomicInteger> e : tile.entrySet()) {
      Map<String, Object> attributes = Maps.newHashMap();
      attributes.put(COUNT_FEATURE_, e.getValue().intValue());
      encoder.addFeature(LAYER_NAME,
                         attributes,
                         GEOMETRY_FACTORY.createPoint(new Coordinate(e.getKey().x, e.getKey().y)));
    }
    return encoder.encode();
  }

  /**
   * Adds the header allowing the tiles be used across domains.
   */
  private static void addAllAccessControlHeader(HttpServletResponse response) {
    response.setHeader("Access-Control-Allow-Origin", "*");
  }
}
