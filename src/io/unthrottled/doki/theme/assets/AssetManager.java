package io.unthrottled.doki.theme.assets;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;

import io.unthrottled.doki.theme.Activator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


public class AssetManager {

  private static final ILog log = Platform.getLog(Activator.getDefault().getBundle());
  
  private static AssetManager instance;

  public static AssetManager getInstance() {
    if (instance == null) {
      instance = new AssetManager();
    }

    return instance;
  }

  private final HttpClient httpClient = new HttpClient();

  private AssetManager() {
    httpClient.getParams().setConnectionManagerTimeout(TimeUnit.MILLISECONDS.convert(5L, TimeUnit.SECONDS));
  }

  private static final String ASSETS_SOURCE = "https://doki.assets.unthrottled.io";


  /**
   * Will return a resolvable URL that can be used to reference an asset.
   * If the asset was able to be downloaded on the local machine it will return a
   * file:// url to the local asset. If it was not able to get the asset then it
   * will return an https:// url to the remote asset.
   */
  public String resolveAssetUrl(String assetPath) {
    var remoteAssetUrl = constructRemoteAssetUrl(
        assetPath
    );
    return constructLocalAssetPath(assetPath)
        .flatMap(it -> resolveTheAssetUrl(it, remoteAssetUrl))
        .orElse(remoteAssetUrl);
  }

  private String constructRemoteAssetUrl(
      String assetPath
  ) {
    return ASSETS_SOURCE + assetPath;
  }

  private Optional<String> resolveTheAssetUrl(Path localAssetPath,
                                              String remoteAssetUrl) {
    if (LocalAssetService.getInstance().hasAssetChanged(localAssetPath, remoteAssetUrl)) {
      return downloadAndGetAssetUrl(localAssetPath, remoteAssetUrl);
    } else {
      return Optional.ofNullable(localAssetPath.toString());
    }
  }

  private Optional<Path> constructLocalAssetPath(
      String assetPath
  ) {
    return Optional.ofNullable(LocalStorageService.getInstance().getAssetPath())
        .map(Path::toAbsolutePath)
        .map(Path::toString)
        .map(localInstallDirectory -> Paths.get(localInstallDirectory, assetPath).normalize().toAbsolutePath());
  }

  private Optional<String> downloadAndGetAssetUrl(
      Path localAssetPath,
      String remoteAssetUrl
  ) {
    LocalStorageService.getInstance().createDirectoriesIfNeeded(localAssetPath.getParent());
    try {
      log.warn("Attempting to download asset " + remoteAssetUrl);
      var remoteAssetRequest = createGetRequest(remoteAssetUrl);
      httpClient.executeMethod(remoteAssetRequest);

      if (remoteAssetRequest.getStatusCode() == 200) {
        try (var inputStream = remoteAssetRequest.getResponseBodyAsStream();
             var bufferedWriter = Files.newOutputStream(
                 localAssetPath,
                 StandardOpenOption.CREATE,
                 StandardOpenOption.TRUNCATE_EXISTING
             )
        ) {
          IOUtils.copy(inputStream, bufferedWriter);
          return Optional.ofNullable(localAssetPath.toString());
        } catch (IOException e) {
        	log.warn("Unable to get remote asset " + remoteAssetUrl, e);
        }
      }
      log.warn("Asset request for "+ remoteAssetUrl + " responded with "+ 
      String.valueOf(remoteAssetRequest.getStatusCode()));
      // TODO: investigate fallback
      return getFallbackURL(localAssetPath, remoteAssetUrl);
    } catch (Throwable e) {
      log.error("Unable to get remote remote asset "+remoteAssetUrl + " for raisins", e);
      return getFallbackURL(localAssetPath, remoteAssetUrl);
    }
  }

  private Optional<String> getFallbackURL(Path localAssetPath,
                                          String remoteAssetPath) {
    if (Files.exists(localAssetPath)) {
      return Optional.of(localAssetPath.toUri().toString());
    }
    return Optional.of(remoteAssetPath);
  }

  private GetMethod createGetRequest(String remoteUrl) {
    GetMethod assetRequest = new GetMethod(remoteUrl);
    assetRequest.addRequestHeader("User-Agent", "Doki-Theme-Eclipse");
		return assetRequest;
  }

}
