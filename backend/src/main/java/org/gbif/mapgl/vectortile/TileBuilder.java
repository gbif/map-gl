package org.gbif.mapgl.vectortile;

import org.gbif.mapgl.MercatorProjectionUtil;
import org.gbif.mapgl.v1.DatacubeTileProto;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import no.ecc.vectortile.VectorTileEncoder;

class TileBuilder {
  private final Map<Key, AtomicInteger> tile = Maps.newHashMap();
  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
  private static final class Key {
    private final int x,y;

    private Key(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Key that = (Key) o;

      return Objects.equal(this.x, that.x) &&
             Objects.equal(this.y, that.y);
    }

    public int getX() {
      return x;
    }

    public int getY() {
      return y;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(x, y);
    }
  }


  public void collect(int zoom, double lat, double lng, int count) {
    if (MercatorProjectionUtil.isPlottable(lat, lng)) {
      int x = MercatorProjectionUtil.getOffsetX(lat, lng, zoom);
      int y = MercatorProjectionUtil.getOffsetY(lat, lng, zoom);
      Key key = new Key(x,y);
      if (tile.containsKey(key)) {
        tile.get(key).addAndGet(count);
      } else {
        tile.put(key, new AtomicInteger(count));
      }
    }
  }

  public byte[] build() {
    VectorTileEncoder encoder = new VectorTileEncoder(4096, 8, true);

     for (Map.Entry<Key, AtomicInteger> e : tile.entrySet()) {
       Map<String, String> attributes = Maps.newHashMap();
       attributes.put("count", String.valueOf(e.getValue()));

       encoder.addFeature("occurrence", attributes, GEOMETRY_FACTORY.createPoint(new Coordinate(e.getKey().getX(),e.getKey().getY())));
   }
   return encoder.encode();
  }
}
