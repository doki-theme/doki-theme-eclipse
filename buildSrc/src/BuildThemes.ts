// @ts-ignore
import {DokiThemeDefinitions, EclipseDokiThemeDefinition, MasterDokiThemeDefinition, StringDictonary} from './types';
import GroupToNameMapping from "./GroupMappings";
import {toXml, xmlBuilder} from "./XmlTools";

const path = require('path');

const repoDirectory = path.resolve(__dirname, '..', '..');

const fs = require('fs');

const masterThemeDefinitionDirectoryPath =
  path.resolve(repoDirectory, 'masterThemes');

const eclipseTemplateDefinitionDirectoryPath = path.resolve(
  repoDirectory,
  "buildAssets",
  "templates"
);

const eclipseDefinitionDirectoryPath = path.resolve(
  repoDirectory,
  "buildAssets",
  "themes",
  "definitions"
);


function walkDir(dir: string): Promise<string[]> {
  const values: Promise<string[]>[] = fs.readdirSync(dir)
    .map((file: string) => {
      const dirPath: string = path.join(dir, file);
      const isDirectory = fs.statSync(dirPath).isDirectory();
      if (isDirectory) {
        return walkDir(dirPath);
      } else {
        return Promise.resolve([path.join(dir, file)]);
      }
    });
  return Promise.all(values)
    .then((scannedDirectories) => scannedDirectories
      .reduce((accum, files) => accum.concat(files), []));
}

const LAF_TYPE = 'laf';
const SYNTAX_TYPE = 'syntax';
const NAMED_COLOR_TYPE = 'colorz';

function getTemplateType(templatePath: string) {
  if (templatePath.endsWith('laf.template.json')) {
    return LAF_TYPE;
  } else if (templatePath.endsWith('syntax.template.json')) {
    return SYNTAX_TYPE;
  } else if (templatePath.endsWith('colors.template.json')) {
    return NAMED_COLOR_TYPE;
  }
  return undefined;
}


function resolveTemplate<T, R>(
  childTemplate: T,
  templateNameToTemplate: StringDictonary<T>,
  attributeResolver: (t: T) => R,
  parentResolver: (t: T) => string,
): R {
  if (!parentResolver(childTemplate)) {
    return attributeResolver(childTemplate);
  } else {
    const parent = templateNameToTemplate[parentResolver(childTemplate)];
    const resolvedParent = resolveTemplate(
      parent,
      templateNameToTemplate,
      attributeResolver,
      parentResolver
    );
    return {
      ...resolvedParent,
      ...attributeResolver(childTemplate)
    };
  }
}


function resolveColor(
  color: string,
  namedColors: StringDictonary<string>
): string {
  const startingTemplateIndex = color.indexOf('&');
  if (startingTemplateIndex > -1) {
    const lastDelimiterIndex = color.lastIndexOf('&');
    const namedColor =
      color.substring(startingTemplateIndex + 1, lastDelimiterIndex);
    const namedColorValue = namedColors[namedColor];
    if (!namedColorValue) {
      throw new Error(`Named color: '${namedColor}' is not present!`);
    }

    // todo: check for cyclic references
    if (color === namedColorValue) {
      throw new Error(`Very Cheeky, you set ${namedColor} to resolve to itself 😒`);
    }

    const resolvedNamedColor = resolveColor(namedColorValue, namedColors);
    if (!resolvedNamedColor) {
      throw new Error(`Cannot find named color '${namedColor}'.`);
    }
    return resolvedNamedColor + color.substring(lastDelimiterIndex + 1) || '';
  }

  return color;
}

function applyNamedColors(
  objectWithNamedColors: StringDictonary<string>,
  namedColors: StringDictonary<string>,
): StringDictonary<string> {
  return Object.keys(objectWithNamedColors)
    .map(key => {
      const color = objectWithNamedColors[key];
      const resolvedColor = resolveColor(
        color,
        namedColors
      );
      return {
        key,
        value: resolvedColor
      };
    }).reduce((accum: StringDictonary<string>, kv) => {
      accum[kv.key] = kv.value;
      return accum;
    }, {});
}

function constructNamedColorTemplate(
  dokiThemeTemplateJson: MasterDokiThemeDefinition,
  dokiTemplateDefinitions: DokiThemeDefinitions
) {
  const lafTemplates = dokiTemplateDefinitions[NAMED_COLOR_TYPE];
  const lafTemplate =
    (dokiThemeTemplateJson.dark ?
      lafTemplates.dark : lafTemplates.light);

  const resolvedColorTemplate =
    resolveTemplate(
      lafTemplate, lafTemplates,
      template => template.colors,
      template => template.extends
    );

  const resolvedNameColors = resolveNamedColors(
    dokiTemplateDefinitions,
    dokiThemeTemplateJson
  );

  // do not really need to resolve, as there are no
  // &someName& colors, but what ever.
  const resolvedColors =
    applyNamedColors(resolvedColorTemplate, resolvedNameColors);
  return {
    ...resolvedColors,
    ...resolvedColorTemplate,
    ...resolvedNameColors,
  };
}

function resolveNamedColors(
  dokiTemplateDefinitions: DokiThemeDefinitions,
  dokiThemeTemplateJson: MasterDokiThemeDefinition
) {
  const colorTemplates = dokiTemplateDefinitions[NAMED_COLOR_TYPE];
  return resolveTemplate(
    dokiThemeTemplateJson,
    colorTemplates,
    template => template.colors,
    // @ts-ignore
    template => template.extends ||
      template.dark !== undefined && (dokiThemeTemplateJson.dark ?
        'dark' : 'light'));
}

const toPairs = require('lodash/toPairs');

export interface StringDictionary<T> {
  [key: string]: T;
}

export const dictionaryReducer = <T>(
  accum: StringDictionary<T>,
  [key, value]: [string, T],
) => {
  accum[key] = value;
  return accum;
};


function replaceValues<T, R>(itemToReplace: T, valueConstructor: (key: string, value: string) => R): T {
  return toPairs(itemToReplace)
    .map(([key, value]: [string, string]) => ([key, valueConstructor(key, value)]))
    .reduce(dictionaryReducer, {});
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

function buildEclipseThemeManifest(
  dokiThemeDefinition: MasterDokiThemeDefinition,
  dokiTemplateDefinitions: DokiThemeDefinitions,
  dokiThemeEclipseDefinition: EclipseDokiThemeDefinition,
): DokiThemeEclipse {
  const namedColors = constructNamedColorTemplate(
    dokiThemeDefinition, dokiTemplateDefinitions
  )
  const colorsOverride = dokiThemeEclipseDefinition.overrides.theme &&
    dokiThemeEclipseDefinition.overrides.theme.colors || {};
  return {
    name: `Doki Theme: ${dokiThemeDefinition.name}`,
    description: `A ${dokiThemeDefinition.dark ? 'dark' : 'light'} theme modeled after ${dokiThemeDefinition.displayName} from ${dokiThemeDefinition.group}`,
    theme: {}
  };
}

function createDokiTheme(
  dokiFileDefinitionPath: string,
  dokiThemeDefinition: MasterDokiThemeDefinition,
  dokiTemplateDefinitions: DokiThemeDefinitions,
  dokiThemeEclipseDefinition: EclipseDokiThemeDefinition,
) {
  try {
    return {
      path: dokiFileDefinitionPath,
      definition: dokiThemeDefinition,
      stickers: getStickers(
        dokiThemeDefinition,
        dokiFileDefinitionPath
      ),
      manifest: buildEclipseThemeManifest(
        dokiThemeDefinition,
        dokiTemplateDefinitions,
        dokiThemeEclipseDefinition,
      ),
      theme: {}
    };
  } catch (e) {
    throw new Error(`Unable to build ${dokiThemeDefinition.name}'s theme for reasons ${e}`);
  }
}

const readJson = <T>(jsonPath: string): T =>
  JSON.parse(fs.readFileSync(jsonPath, 'utf-8'));

type TemplateTypes = StringDictonary<StringDictonary<string>>;

const isTemplate = (filePath: string): boolean =>
  !!getTemplateType(filePath);

const readTemplates = (templatePaths: string[]): TemplateTypes => {
  return templatePaths
    .filter(isTemplate)
    .map(templatePath => {
      return {
        type: getTemplateType(templatePath)!!,
        template: readJson<any>(templatePath)
      };
    })
    .reduce((accum: TemplateTypes, templateRepresentation) => {
      accum[templateRepresentation.type][templateRepresentation.template.name] =
        templateRepresentation.template;
      return accum;
    }, {
      [SYNTAX_TYPE]: {},
      [LAF_TYPE]: {},
      [NAMED_COLOR_TYPE]: {},
    });
};

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


walkDir(eclipseDefinitionDirectoryPath)
  .then((files) =>
    files.filter((file) => file.endsWith("eclipse.definition.json"))
  )
  .then((dokiThemeEclipseDefinitionPaths) => {
    return {
      dokiThemeEclipseDefinitions: dokiThemeEclipseDefinitionPaths
        .map((dokiThemeEclipseDefinitionPath) =>
          readJson<EclipseDokiThemeDefinition>(dokiThemeEclipseDefinitionPath)
        )
        .reduce(
          (accum: StringDictonary<EclipseDokiThemeDefinition>, def) => {
            accum[def.id] = def;
            return accum;
          },
          {}
        ),
    };
  }).then(({dokiThemeEclipseDefinitions}) =>
  walkDir(path.resolve(masterThemeDefinitionDirectoryPath, 'templates'))
    .then(readTemplates)
    .then(dokiTemplateDefinitions => {
      return walkDir(path.resolve(masterThemeDefinitionDirectoryPath, 'definitions'))
        .then(files => files.filter(file => file.endsWith('master.definition.json')))
        .then(dokiFileDefinitionPaths => {
          return {
            dokiThemeEclipseDefinitions,
            dokiTemplateDefinitions,
            dokiFileDefinitionPaths
          };
        });
    }))
  .then(templatesAndDefinitions => {
    const {
      dokiTemplateDefinitions,
      dokiThemeEclipseDefinitions,
      dokiFileDefinitionPaths
    } = templatesAndDefinitions;

    return dokiFileDefinitionPaths
      .map(dokiFileDefinitionPath => {
        const dokiThemeDefinition = readJson<MasterDokiThemeDefinition>(dokiFileDefinitionPath);
        const dokiThemeEclipseDefinition =
          dokiThemeEclipseDefinitions[dokiThemeDefinition.id];
        if (!dokiThemeEclipseDefinition) {
          throw new Error(
            `${dokiThemeDefinition.displayName}'s theme does not have a Eclipse Definition!!`
          );
        }
        return ({
          dokiFileDefinitionPath,
          dokiThemeDefinition,
          dokiThemeEclipseDefinition,
        });
      })
      .filter(pathAndDefinition =>
        (pathAndDefinition.dokiThemeDefinition.product === 'ultimate' &&
          process.env.PRODUCT === 'ultimate') ||
        pathAndDefinition.dokiThemeDefinition.product !== 'ultimate'
      )
      .map(({
              dokiFileDefinitionPath,
              dokiThemeDefinition,
              dokiThemeEclipseDefinition,
            }) =>
        createDokiTheme(
          dokiFileDefinitionPath,
          dokiThemeDefinition,
          dokiTemplateDefinitions,
          dokiThemeEclipseDefinition,
        )
      );
  })
  .then(dokiThemes => {
    const pluginXmlPath = path.resolve(repoDirectory, 'plugin.xml');
    return toXml(fs.readFileSync(pluginXmlPath, {
      encoding: 'utf8'
    }))
      .then(pluginXml => {
        const cssExtension = pluginXml.plugin.extension.find(
          (extension: any) => extension.$.point === 'org.eclipse.e4.ui.css.swt.theme'
        );
        console.log(JSON.stringify(cssExtension, null, 2));
        // cssExtension.theme.push({
        //   '$':{
        //     "to":"the window",
        //     "skeet":"skeet"
        //   }
        // })
        const xml = xmlBuilder.buildObject(pluginXml);
        fs.writeFileSync(path.resolve(pluginXmlPath), xml, 'utf8');
      }).then(()=>{
        fs.writeFileSync(path.resolve(repoDirectory, 'themes', 'themes.json'),
          JSON.stringify(dokiThemes.reduce((accum, dokiTheme) => ({
            ...accum,
            [dokiTheme.definition.id]: {
              id: dokiTheme.definition.id,
              displayName: `${GroupToNameMapping[dokiTheme.definition.group]}${dokiTheme.definition.name}`,
              stickers: dokiTheme.stickers
            }
          }), {}), null, 2), {
            encoding: 'utf-8',
          })
      });
  })
  .then(() => {
    console.log('Theme Generation Complete!');
  });
