package io.unthrottled.doki.theme.themes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.unthrottled.doki.theme.Activator;
import io.unthrottled.doki.theme.definitions.DokiTheme;
import io.unthrottled.doki.theme.definitions.ThemeDefinition;
import io.unthrottled.doki.theme.preferences.PreferenceConstants;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ThemeManager {

  private final Gson gson = new Gson();
  private static final ILog logger = Platform.getLog(Activator.getDefault().getBundle());

  private Map<String, DokiTheme> themeDefinitions;

  private static ThemeManager instance;

  public static ThemeManager getInstance() {
    if (instance == null) {
      instance = new ThemeManager();
    }

    return instance;
  }

  public Stream<DokiTheme> getAvailableThemes() {
    return themeDefinitions.values().stream();
  }

  public DokiTheme getCurrentTheme() {
    return themeDefinitions.get(
        Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.CURRENT_THEME_PREFERENCE)
    );
  }

  private ThemeManager() {
    try (var themeDefJson = getClass().getResourceAsStream("/themes/themes.json")) {
      themeDefinitions = gson.<Map<String, ThemeDefinition>>fromJson(
          new BufferedReader(new InputStreamReader(themeDefJson)),
          new TypeToken<Map<String, ThemeDefinition>>() {
          }.getType()
      ).values().stream()
          .map(DokiTheme::new)
          .collect(Collectors.toMap(DokiTheme::getId, Function.identity()));

    } catch (Throwable e) {
    	logger.error("Unable to read definitions for reasons", e);
    }
  }
}
