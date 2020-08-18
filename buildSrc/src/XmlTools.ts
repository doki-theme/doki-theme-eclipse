import xmlParser from "xml2js";

export const xmlBuilder = new xmlParser.Builder();

export const toXml = (xml1: string) =>
    xmlParser.parseStringPromise(xml1)


