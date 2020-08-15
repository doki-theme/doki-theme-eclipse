package io.unthrottled.doki.theme.definitions;

import com.google.gson.annotations.SerializedName;

public class Stickers {
  @SerializedName("default")
  private Sticker primary;
  private Sticker secondary;

  public Sticker getPrimary() {
    return primary;
  }

  public Sticker getSecondary() {
    return secondary;
  }

}
