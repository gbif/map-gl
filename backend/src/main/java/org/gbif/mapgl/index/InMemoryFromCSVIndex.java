package org.gbif.mapgl.index;

import org.gbif.mapgl.TileServerConfiguration;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds an in memory index of data that is delimited and in the format of latitude, longitude, value, where value
 * is expected to be an integer.
 */
public class InMemoryFromCSVIndex implements TileIndex<AtomicInteger> {

  private static final Logger LOG = LoggerFactory.getLogger(InMemoryFromCSVIndex.class);
  private static final Pattern TAB = Pattern.compile("\t");
  private static final Pattern COMMA = Pattern.compile(",");

  // the in-memory index
  private static final Map<XYZ, Map<Point, AtomicInteger>> index = Maps.newHashMap();

  private final int tileSize;

  public InMemoryFromCSVIndex(TileServerConfiguration configuration) {
    String source = configuration.dataConfiguration.source;
    LOG.info("Building in memory tile index from {}", source);

    try (
      BufferedReader in = source.contains(".gz")
        ? new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(source)))) :
        new BufferedReader(new InputStreamReader(new FileInputStream(source)));
    ) {

      // default as TAB but if the file says .csv use a comma
      Pattern splitter = source.contains(".csv") ? COMMA : TAB;
      int rowCount = 0;
      String line = in.readLine();

      while (line != null && rowCount < configuration.dataConfiguration.rowLimit) {
        String[] fields = splitter.split(line);
        double lat = Double.parseDouble(fields[0]);
        double lng = Double.parseDouble(fields[1]);
        int count = Integer.parseInt(fields[2]);

        for (int zoom = 0; zoom < configuration.dataConfiguration.zooms; zoom++) {

          if (MercatorProjectionUtil.isPlottable(lat, lng)) {
            int x = MercatorProjectionUtil.toTileX(lng, zoom);
            int y = MercatorProjectionUtil.toTileY(lat, zoom);
            XYZ key = new XYZ(x, y, zoom);

            // find the tile in the index, creating if necessary
            Map<Point, AtomicInteger> tile = index.get(key);
            if (tile == null) {
              tile = Maps.newHashMap();
              index.put(key, tile);
            }

            // project the coordinate onto the tile, updating the count and creating if needing
            Point pixel = new Point(
              MercatorProjectionUtil.getOffsetX(lat, lng, zoom, configuration.tileSize),
              MercatorProjectionUtil.getOffsetY(lat, lng, zoom, configuration.tileSize));
            AtomicInteger value = tile.get(pixel);
            if (value == null) {
              tile.put(pixel, new AtomicInteger(count));
            } else {
              value.addAndGet(count);
            }
          }
        }

        if (++rowCount % 10000 == 0) {
          LOG.info("Read {} rows", rowCount);
        }
        line = in.readLine();
      }
      LOG.info("Finished building index from {} rows", rowCount);

    } catch (IOException e) {
      LOG.error("Unexpected error building in memory index of source data", e);
      throw Throwables.propagate(e); // deliberate log and throw
    }
    this.tileSize = configuration.tileSize;
  }

  @Override
  public Map<Point, AtomicInteger> getTile(int x, int y, int z) {
    return index.get(new XYZ(x,y,z));
  }

  @Override
  public int getTileSize() {
    return tileSize;
  }
}
