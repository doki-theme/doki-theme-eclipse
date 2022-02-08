import {
  BaseAppDokiThemeDefinition, constructNamedColorTemplate,
  DokiThemeDefinitions,
  evaluateTemplates,
  fillInTemplateScript,
  getDisplayName,
  MasterDokiThemeDefinition,
  resolvePaths,
  StringDictionary,
} from 'doki-build-source';

type EclipseDokiThemeDefinition = BaseAppDokiThemeDefinition;

const path = require('path');
const {
  repoDirectory,
  masterThemeDefinitionDirectoryPath,
  appTemplatesDirectoryPath,
} = resolvePaths(__dirname);

const fs = require('fs');

export const buildNamedColors = (
  dokiThemeDefinition: MasterDokiThemeDefinition,
  dokiTemplateDefinitions: DokiThemeDefinitions,
  dokiThemeAppDefinition: BaseAppDokiThemeDefinition,
): StringDictionary<string> => {
  const namedColors = constructNamedColorTemplate(
    dokiThemeDefinition, dokiTemplateDefinitions
  )
  const colorsOverride = dokiThemeAppDefinition.overrides.theme?.colors || {};
  return {
    ...namedColors,
    ...colorsOverride
  };
}

/**
 * Converts an RGB color value to HSL. Conversion formula
 * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
 * Assumes r, g, and b are contained in the set [0, 255] and
 * returns h, s, and l in the set [0, 1].
 *
 * @param   Number  r       The red color value
 * @param   Number  g       The green color value
 * @param   Number  b       The blue color value
 * @return  Array           The HSL representation
 */
function rgbToHsl([r, g, b]: [number, number, number]) {
  r /= 255, g /= 255, b /= 255;

  const max = Math.max(r, g, b), min = Math.min(r, g, b);
  const l = (max + min) / 2;
  let s: number;
  let h = l;

  if (max == min) {
    h = s = 0; // achromatic
  } else {
    var d = max - min;
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min);

    switch (max) {
      case r:
        h = (g - b) / d + (g < b ? 6 : 0);
        break;
      case g:
        h = (b - r) / d + 2;
        break;
      case b:
        h = (r - g) / d + 4;
        break;
    }

    h /= 6;
  }

  return [Math.round(h * 300), Math.round(s * 100), Math.round(l * 100)];
}

function hexToRGB(s: string | [number, number, number]): [number, number, number] {
  if (typeof s === 'string') {
    const hex = parseInt(s.substr(1), 16)
    return [
      (hex & 0xFF0000) >> 16,
      (hex & 0xFF00) >> 8,
      (hex & 0xFF)
    ]
  }
  return s;

}

// todo: dis
type DokiThemeEclipse = {
  [k: string]: any
}

function createDokiTheme(
  dokiFileDefinitionPath: string,
  dokiThemeDefinition: MasterDokiThemeDefinition,
  _: DokiThemeDefinitions,
  dokiThemeEclipseDefinition: EclipseDokiThemeDefinition,
  dokiTemplateDefinitions: DokiThemeDefinitions,
) {
  try {
    return {
      path: dokiFileDefinitionPath,
      definition: dokiThemeDefinition,
      stickers: getStickers(
        dokiThemeDefinition,
        dokiFileDefinitionPath
      ),
      namedColors: buildNamedColors(
        dokiThemeDefinition,
        dokiTemplateDefinitions,
        dokiThemeEclipseDefinition,
      ),
      theme: {},
      eclipseDefinition: dokiThemeEclipseDefinition
    };
  } catch (e) {
    throw new Error(`Unable to build ${dokiThemeDefinition.name}'s theme for reasons ${e}`);
  }
}

function resolveStickerPath(
  themeDefinitionPath: string,
  sticker: string,
) {
  const stickerPath = path.resolve(
    path.resolve(themeDefinitionPath, '..'),
    sticker
  );
  return stickerPath.substr(masterThemeDefinitionDirectoryPath.length + '/definitions'.length);
}


const getStickers = (
  dokiDefinition: MasterDokiThemeDefinition,
  themePath: string,
) => {
  const secondary =
    dokiDefinition.stickers.secondary || dokiDefinition.stickers.normal;
  return {
    default: {
      path: resolveStickerPath(themePath, dokiDefinition.stickers.default),
      name: dokiDefinition.stickers.default,
    },
    ...(secondary
      ? {
        secondary: {
          path: resolveStickerPath(themePath, secondary),
          name: secondary,
        },
      }
      : {}),
  };
};

console.log('Preparing to generate themes.');


function buildThemeId(dokiTheme: { path: string; definition: MasterDokiThemeDefinition; stickers: { default: { path: string; name: string } }; theme: {}; namedColors: DokiThemeEclipse }) {
  return `${dokiTheme.definition.dark ? 'dark_' : ''}${dokiTheme.definition.id}`;
}

const themesDirectory = path.resolve(repoDirectory, 'plugin-source', 'themes');

function writeCssFile(pathSegments: string, templateToFillIn: string, dokiTheme: { path: string; definition: MasterDokiThemeDefinition; stickers: { default: { path: string; name: string } }; theme: {}; namedColors: DokiThemeEclipse }) {
  fs.writeFileSync(
    path.resolve(themesDirectory, 'css', pathSegments),
    fillInTemplateScript(
      templateToFillIn,
      dokiTheme.namedColors
    ),
    {
      encoding: 'utf-8',
    },
  );
}

const devstyleAssetsDirectory = path.resolve(repoDirectory, 'devStyleThemes')

function writeSyntaxFile(pathSegments: string,
                         templateToFillIn: string, dokiTheme: { path: string; definition: MasterDokiThemeDefinition; stickers: { default: { path: string; name: string } }; theme: {}; namedColors: DokiThemeEclipse, eclipseDefinition: EclipseDokiThemeDefinition },
                         themeId: number) {
  const themeDirectory = path.resolve(devstyleAssetsDirectory, getDisplayName(dokiTheme).replace(/:/g, '-'))
  const devstyleSyntaxXml = path.resolve(themeDirectory, pathSegments.replace(/:/g, '-'));
  fs.mkdirSync(path.dirname(devstyleSyntaxXml), {recursive: true})
  const hsl = rgbToHsl(hexToRGB(dokiTheme.namedColors.baseBackground)).join(' ')
  const themeToCustomize = dokiTheme.definition.dark ? `Dark` : `Light`
  fs.writeFileSync(
    path.resolve(themeDirectory, `${themeToCustomize} Custom HSL ${hsl}`),
    hsl
  )
  fs.writeFileSync(
    devstyleSyntaxXml,
    fillInTemplateScript(
      templateToFillIn,
      {
        ...dokiTheme.namedColors,
        ...(dokiTheme.definition.overrides?.editorScheme?.colors || {}),
        ...(dokiTheme.eclipseDefinition.overrides.editorScheme?.colors || {}),
        ...dokiTheme.definition,
        themeId: themeId.toString(),
        modifiedDate: new Date().toISOString()
      }
    ),
    {
      encoding: 'utf-8',
    },
  );
}

evaluateTemplates(
  {
    appName: 'eclipse',
    currentWorkingDirectory: __dirname,
  },
  createDokiTheme
)
  .then(dokiThemes => {
    const devStyleSyntaxXml = fs.readFileSync(
      path.resolve(appTemplatesDirectoryPath, 'syntax.xml'), {encoding: 'utf-8'}
    );
    const themeIdsToDumbEclipseIds = dokiThemes.map(dokiTheme => dokiTheme.definition.id)
      .sort((a, b) => a.localeCompare(b))
      .reduce((accum, themeId, index) => ({
        ...accum,
        [themeId]: index
      }), {} as StringDictionary<number>);

    fs.rmdirSync(devstyleAssetsDirectory, {recursive: true})
    dokiThemes.forEach(dokiTheme => {
      writeSyntaxFile(
        `${dokiTheme.definition.name}.xml`,
        devStyleSyntaxXml,
        dokiTheme,
        (themeIdsToDumbEclipseIds[dokiTheme.definition.id] || 0) + 6969
      )
    });
    // const pluginXmlPath = path.resolve(repoDirectory, 'plugin.xml');
    // return toXml(fs.readFileSync(pluginXmlPath, {
    //   encoding: 'utf8'
    // }))
    // .then(pluginXml => {
    //   const cssXMLExtension = pluginXml.plugin.extension.find(
    //     (extension: any) => extension.$.point === 'org.eclipse.e4.ui.css.swt.theme'
    //   );
    //
    //   const createLafCssFileName = (dokiTheme: DokiTheme) => `${dokiTheme.definition.name}.css`;
    //   const createSyntaxCssFileName = (dokiTheme: DokiTheme) => `${dokiTheme.definition.name}.syntax.css`;
    //
    //   cssXMLExtension.theme =
    //     dokiThemes.map(dokiTheme => ({
    //       '$': {
    //         'basestylesheeturi': `themes/css/${createLafCssFileName(dokiTheme)}`,
    //         'id': buildThemeId(dokiTheme),
    //         'label': getDisplayName(dokiTheme),
    //       }
    //     }));
    //
    //   cssXMLExtension.stylesheet =
    //     dokiThemes.map(dokiTheme => ({
    //       '$': {
    //         'uri': `themes/css/${createSyntaxCssFileName(dokiTheme)}`,
    //       },
    //       themeId: {
    //         '$': {
    //           refid: buildThemeId(dokiTheme)
    //         },
    //       }
    //     }));
    //   const xml = xmlBuilder.buildObject(pluginXml);
    //
    //   fs.writeFileSync(path.resolve(pluginXmlPath), xml, 'utf8');
    //
    //   const lafCSSTemplate = fs.readFileSync(
    //     path.resolve(appTemplatesDirectoryPath, 'theme.template.css'),
    //     {
    //       encoding: 'utf-8',
    //     });
    //
    //   const syntaxCSSTemplate = fs.readFileSync(
    //     path.resolve(appTemplatesDirectoryPath,
    //       'syntax.coloring.template.css'),
    //     {
    //       encoding: 'utf-8',
    //     });
    //
    //   dokiThemes.forEach(dokiTheme => {
    //     writeCssFile(createLafCssFileName(dokiTheme), lafCSSTemplate, dokiTheme);
    //     writeCssFile(createSyntaxCssFileName(dokiTheme), syntaxCSSTemplate, dokiTheme);
    //   });
    // })
    // .then(() => {
    fs.writeFileSync(path.resolve(themesDirectory, 'themes.json'),
      JSON.stringify(dokiThemes.reduce((accum, dokiTheme) => ({
        ...accum,
        [dokiTheme.definition.id]: {
          id: dokiTheme.definition.id,
          displayName: getDisplayName(dokiTheme),
          stickers: dokiTheme.stickers,
          isDark: dokiTheme.definition.dark,
        }
      }), {}), null, 2), {
        encoding: 'utf-8',
      })
    // });
  })
  .then(() => {
    console.log('Theme Generation Complete!');
  });
