package org.gbif.mapgl.vectortile;

import org.gbif.mapgl.MercatorProjectionUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.*;
import no.ecc.vectortile.VectorTileEncoder;

/**
 * Simple generator of tiles based on tab delimited input data.
 * Expects data in the format lat,lng,count
 */
public class VectorTileGenerator {

  private static final Pattern SPLITTER = Pattern.compile("\t");
  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  /**
   * Expects inpath numZooms and will output into /tmp.
   * Inpath should be a GZipInput file.
   */
  public static void main(String[] args) throws IOException {
    int zooms = Integer.parseInt(args[1]);
    Map<Key, VectorTileEncoder> tiles = Maps.newHashMap();

    try (
      BufferedReader in = new BufferedReader(
        new InputStreamReader(new GZIPInputStream(new FileInputStream(new File(args[0])))))
    ) {
      String line = in.readLine();
      int rowCount = 0;


      while (line != null) {

        String[] fields = SPLITTER.split(line);
        double lat = Double.parseDouble(fields[0]);
        double lng = Double.parseDouble(fields[1]);
        int count = Integer.parseInt(fields[2]);

        for (int zoom = 0; zoom < zooms; zoom++) {

          if (MercatorProjectionUtil.isPlottable(lat, lng)) {
            int x = MercatorProjectionUtil.toTileX(lng, zoom);
            int y = MercatorProjectionUtil.toTileY(lat, zoom);
            Map<String, String> attributes = Maps.newHashMap();
            attributes.put("count", String.valueOf(count));

            Key key = new Key(x,y,zoom);
            VectorTileEncoder encoder = tiles.get(key);
            if (encoder == null) {
              encoder = new VectorTileEncoder(4096, 8, true);
              tiles.put(key, encoder);
            }
            encoder.addFeature("count", attributes, GEOMETRY_FACTORY.createPoint(new Coordinate(x,y)));
          }
        }

        if (++rowCount % 10000 == 0) {
          System.out.println(rowCount);
        }

        line = in.readLine();
      }
    }

    for (Map.Entry<Key, VectorTileEncoder> e : tiles.entrySet()) {
      try (
        OutputStream os = new GZIPOutputStream(new FileOutputStream("/tmp/vt-" + e.getKey().getZ()
                                                                    + "-" + e.getKey().getX() + "-" + e.getKey().getY() + ".pbf.gz"));
      ) {
        os.write(e.getValue().encode());
      }
    }  }

  private static final class Key {
    private final int x;
    private final int y;
    private final int z;


    private Key(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Key that = (Key) o;

      return Objects.equal(this.x, that.x) &&
             Objects.equal(this.y, that.y) &&
             Objects.equal(this.z, that.z);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(x, y, z);
    }

    public int getX() {
      return x;
    }

    public int getY() {
      return y;
    }

    public int getZ() {
      return z;
    }
  }
}
