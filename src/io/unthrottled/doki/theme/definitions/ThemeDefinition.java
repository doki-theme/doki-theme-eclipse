package io.unthrottled.doki.theme.definitions;

public class ThemeDefinition {
  private String id;
  private String displayName;
  private Stickers stickers;
  private boolean isDark;


  public String getId() {
    return id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Stickers getStickers() {
    return stickers;
  }

	public boolean isDark() {
		return isDark;
	}
}

