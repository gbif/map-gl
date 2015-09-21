package org.gbif.mapgl.index;

import java.awt.*;
import java.util.Map;

/**
 * An API to look up tiles.
 */
public interface TileIndex<T> {
  Map<Point, T> getTile(int x, int y, int z);

  int getTileSize();
}
