package org.gbif.mapgl.datacube;

import org.gbif.mapgl.MercatorProjectionUtil;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import org.gbif.mapgl.v1.DatacubeTileProto;

class TileBuilder {
  private final Map<Key, AtomicInteger> tile = Maps.newHashMap();
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

  public DatacubeTileProto.DatacubeTile build() {
    DatacubeTileProto.DatacubeTile.Builder b = DatacubeTileProto.DatacubeTile.newBuilder();
    DatacubeTileProto.DatacubeTile.Channel.Builder c = DatacubeTileProto.DatacubeTile.Channel.newBuilder();
   for (Map.Entry<Key, AtomicInteger> e : tile.entrySet()) {
     b.addX(e.getKey().getX());
     b.addY(e.getKey().getY());
     c.addValue(e.getValue().get());
   }
   b.addChannel(c.build());
   return b.build();
  }
}
