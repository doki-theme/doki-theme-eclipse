package io.unthrottled.doki.theme.preferences;

import io.unthrottled.doki.theme.Activator;
import io.unthrottled.doki.theme.definitions.ThemeConstants;
import io.unthrottled.doki.theme.themes.ThemeManager;
import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.util.Comparator;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class ThemePreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  public ThemePreferences() {
    super(GRID);
    setPreferenceStore(Activator.getDefault().getPreferenceStore());
    setDescription("Feature configurations for the Doki-Theme");
  }

  /**
   * Creates the field editors. Field editors are abstractions of the common GUI
   * blocks needed to manipulate various types of preferences. Each field editor
   * knows how to save and restore itself.
   */
  public void createFieldEditors() {
    addField(new DirectoryFieldEditor(PreferenceConstants.ASSET_PATH_PREFERENCE, "Asset &Directory preference:",
      getFieldEditorParent()));

    addField(new RadioGroupFieldEditor(
      PreferenceConstants.STICKER_TYPE_PREFERENCE,
      "Sticker Type",
      1,
      new String[][]{{"&Primary", ThemeConstants.Stickers.PRIMARY_STICKER}, {
        "&Secondary", ThemeConstants.Stickers.SECONDARY_STICKER}
      }, getFieldEditorParent()));

    addField(new ComboFieldEditor(PreferenceConstants.CURRENT_THEME_PREFERENCE, "Choose your theme",
      ThemeManager.getInstance().getAvailableThemes()
        .map(dokiTheme -> new String[]{
          dokiTheme.getUniqueName(),
          dokiTheme.getId()
        }).sorted(Comparator.comparing(item -> item[0]))
        .toArray(String[][]::new),
      getFieldEditorParent()));

    addField(new BooleanFieldEditor(PreferenceConstants.AUTO_SET_THEME,
      "Automatically set theme",
      getFieldEditorParent()));
  }

  public void init(IWorkbench workbench) {
  }
}
