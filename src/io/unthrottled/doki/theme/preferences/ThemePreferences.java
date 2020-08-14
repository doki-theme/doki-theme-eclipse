package io.unthrottled.doki.theme.preferences;

import io.unthrottled.doki.theme.ThemeConstants;
import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import io.unthrottled.doki.theme.Activator;

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
				new String[][] { { "&Primary", ThemeConstants.Stickers.PRIMARY_STICKER}, {
						"&Secondary", ThemeConstants.Stickers.PRIMARY_STICKER }
				}, getFieldEditorParent()));

		// TODO: need to read all the themes! 
		addField(new ComboFieldEditor(PreferenceConstants.CURRENT_THEME_PREFERENCE, "Choose your theme",
				new String[][] { { "Ryuko", "19b65ec8-133c-4655-a77b-13623d8e97d3" },
						{ "Satsuki", "3a78b13e-dbf2-410f-bb20-12b57bff7735" } },
				getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {}
}

