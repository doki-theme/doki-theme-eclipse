package io.unthrottled.doki.theme.themes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.unthrottled.doki.theme.Activator;
import io.unthrottled.doki.theme.definitions.ThemeDefinition;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Stream;

public class ThemeManager {

  private final Gson gson = new Gson();
  private static final ILog logger = Platform.getLog(Activator.getDefault().getBundle());

  private Map<String, ThemeDefinition> themeDefinitions;

  private static ThemeManager instance;

  public static ThemeManager getInstance() {
    if (instance == null) {
      instance = new ThemeManager();
    }

    return instance;
  }

  public Stream<Map.Entry<String, ThemeDefinition>> getThemeDefinitions() {
    return themeDefinitions.entrySet().stream();
}

private ThemeManager() {
    try (var themeDefJson = getClass().getResourceAsStream("/themes/themes.json")) {
      themeDefinitions = gson.fromJson(
          new BufferedReader(new InputStreamReader(themeDefJson)),
          new TypeToken<Map<String, ThemeDefinition>>() {
          }.getType()
      );
    } catch (Throwable e) {
//    	logger.error("Unable to read definitions for reasons", e);
    }
  }
}
