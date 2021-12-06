package io.unthrottled.doki.theme.tools;

public class URLTools {

  public static String sanitizeURLSString(String urlString) {
    return urlString
      .replaceAll(" ", "%20")
      .replaceAll("'", "%27");
  }
}
