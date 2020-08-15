package io.unthrottled.doki.theme.themes;

public class StickerService {

  private static StickerService instance;

  public static StickerService getInstance() {
    if (instance == null) {
      instance = new StickerService();
    }

    return instance;
  }

  private StickerService() {
  }


  public String getCurrentSticker() {
    return null;
  }
}
