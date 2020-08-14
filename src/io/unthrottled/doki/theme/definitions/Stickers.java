package io.unthrottled.doki.theme.definitions;

import com.google.gson.annotations.SerializedName;

public class Stickers {
  @SerializedName("default")
  private Sticker primary;
  private Sticker secondary;

  public Sticker getPrimary() {
    return primary;
  }

  public void setPrimary(Sticker primary) {
    this.primary = primary;
  }

  public Sticker getSecondary() {
    return secondary;
  }

  public void setSecondary(Sticker secondary) {
    this.secondary = secondary;
  }
}
