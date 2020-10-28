package io.unthrottled.doki.theme.definitions;

public class DokiTheme {

  private final ThemeDefinition themeDefinition;

  public DokiTheme(ThemeDefinition themeDefinition) {
    this.themeDefinition = themeDefinition;
  }

  public String getId() {
    return themeDefinition.getId();
  }

  public String getUniqueName() {
    return themeDefinition.getDisplayName();
  }

  public Stickers getStickers() {
    return themeDefinition.getStickers();
  }

  public String getCSSId() {
    return (themeDefinition.isDark() ? "dark_" : "") + getId();
  }
}
