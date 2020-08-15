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
}
