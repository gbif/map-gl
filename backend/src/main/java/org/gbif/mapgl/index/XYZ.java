package org.gbif.mapgl.index;

import com.google.common.base.Objects;

public class XYZ {

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getZ() {
    return z;
  }

  private final int x;
  private final int y;
  private final int z;

  public XYZ(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    XYZ that = (XYZ) o;

    return Objects.equal(this.x, that.x) &&
           Objects.equal(this.y, that.y) &&
           Objects.equal(this.z, that.z);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(x, y, z);
  }
}
