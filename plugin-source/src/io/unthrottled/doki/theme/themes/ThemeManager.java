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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.unthrottled.doki.theme.tools.URLTools.sanitizeURLSString;
import static java.nio.file.Paths.get;
import static java.util.Optional.ofNullable;
import static org.eclipse.core.runtime.FileLocator.toFileURL;

public class ThemeManager {

  private static final ILog logger = Platform.getLog(Activator.getDefault().getBundle());
  private static ThemeManager instance;
  private final Gson gson = new Gson();
  private Map<String, DokiTheme> themeDefinitions;

  private ThemeManager() {
    try (var themeDefJson = getThemeDefinitionsAsInputStream()) {
      themeDefinitions = gson.<Map<String, ThemeDefinition>>fromJson(
          new BufferedReader(new InputStreamReader(
            Objects.requireNonNull(
              themeDefJson,
              "Expected to have theme definitions!"
            ))),
          new TypeToken<Map<String, ThemeDefinition>>() {
          }.getType()
        ).values().stream()
        .map(DokiTheme::new)
        .collect(Collectors.toMap(DokiTheme::getId, Function.identity()));

    } catch (Throwable e) {
      logger.error("Unable to read definitions for reasons", e);
    }
  }

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

  private InputStream getThemeDefinitionsAsInputStream() {
    try {
      return Files.newInputStream(get(getThemeDefinitionsURI()));
    } catch (IOException | URISyntaxException e) {
      logger.error("Unable to open theme resource for reasons", e);
      return null;
    }
  }

  private URI getThemeDefinitionsURI() throws IOException, URISyntaxException {
    var fileUrl = toFileURL(new URI("platform:/plugin/doki-theme-eclipse/themes/themes.json").toURL());
    return new URI(cleanURL(fileUrl));
  }

  private String cleanURL(URL fileUrl) {
    return sanitizeURLSString(fileUrl.toString());
  }

  public Optional<DokiTheme> getTheme(String themeId) {
    return ofNullable(themeDefinitions.get(themeId));
  }
}
