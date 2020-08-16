import xmlParser from "xml2js";
import path from "path";
import fs from "fs";

const xmlBuilder = new xmlParser.Builder();

const toXml = (xml1: string) =>
    xmlParser.parseStringPromise(xml1)

const templateDir = path.resolve(__dirname, 'templates');

export const readWriteTemplate = async (
    versionedTemplatePath: string,
    updatePluginDirectory: string,
    pluginVersion: string
) => {
    const versionedTemplate = await toXml(fs.readFileSync(path.resolve(templateDir, versionedTemplatePath), {
        encoding: 'utf8'
    }).replace(/{{version}}/g, pluginVersion));

    const updatePluginXmlAsJson = await toXml(fs.readFileSync(path.resolve(updatePluginDirectory, 'updatePlugins.xml'), {
        encoding: 'utf8'
    }));

    updatePluginXmlAsJson.plugins.plugin = updatePluginXmlAsJson.plugins.plugin
        .filter((p: any) => p.$.id !== versionedTemplate.plugin.$.id)

    updatePluginXmlAsJson.plugins.plugin.push(versionedTemplate.plugin)

    const xml = xmlBuilder.buildObject(updatePluginXmlAsJson);
    fs.writeFileSync(path.resolve(updatePluginDirectory, 'updatePlugins.xml'), xml, 'utf8');
}
