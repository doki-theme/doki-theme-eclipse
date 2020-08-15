package io.unthrottled.doki.theme.assets;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class RestClient {

  private static RestClient instance;

  public static RestClient getInstance() {
    if (instance == null) {
      instance = new RestClient();
    }

    return instance;
  }

  private final HttpClient httpClient = new HttpClient();

  private RestClient() {
    httpClient.getParams().setConnectionManagerTimeout(TimeUnit.MILLISECONDS.convert(5L, TimeUnit.SECONDS));
  }

//  private var log = Logger.getInstance(this::class.java)

  public Optional<String> performGet(String url) {
//    log.info("Attempting to fetch remote asset: $url")
    var request = createGetRequest(url);
    try {
            httpClient.executeMethod(request);
      var statusCode = request.getStatusCode();
//      log.info("Asset has responded for remote asset: $url with status code $statusCode")
      if (statusCode == 200) {
        return Optional.ofNullable(request.getResponseBodyAsString());
      } else {
        return Optional.empty();
      }
    } catch (Throwable e) {
//      log.warn("Unable to get remote asset: $url for raisins", e)
      return Optional.empty();
    }
  }

  private GetMethod createGetRequest(String remoteUrl) {
    var remoteAssetRequest = new GetMethod(remoteUrl);
    remoteAssetRequest.addRequestHeader("User-Agent", "Doki-Theme-Eclipse");
    return remoteAssetRequest;
  }

}
