package lspace

import lspace.types.string.Prefix

object NS {
  object vocab {
    val Lspace = Prefix("https://ns.l-space.eu/")
    val foaf   = Prefix("http://xmlns.com/foaf/0.1/")
    val dc     = Prefix("http://purl.org/dc/terms/")
    val rdf    = Prefix("http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    val owl    = Prefix("http://www.w3.org/2002/07/owl#")
    val skos   = Prefix("http://www.w3.org/2009/08/skos-reference/skos.html#")
    val dcam   = Prefix("http://dublincore.org/2012/06/14/dcam#")
    val rdfs   = Prefix("http://www.w3.org/2000/01/rdf-schema#")
    val xsd    = Prefix("http://www.w3.org/TR/xmlschema11-2/#")
    val schema = Prefix("https://schema.org/")
    val sioc   = Prefix("http://rdfs.org/sioc/spec/#")
  }

  object types {
    val `@literal` = "@literal"
    val `@string`  = "@string"
    val `@iri`     = "@iri"
    val `@number`  = "@number"
    val `@int`     = "@int"
    val `@double`  = "@double"
    val `@long`    = "@long"
    val `@boolean` = "@boolean"
    //    val enum = "enum"
    val `@temporal`         = "@temporal"
    val `@date`             = "@date"
    val `@time`             = "@time"
    val `@quantity`         = "@quantity"
    val `@duration`         = "@duration"
    val `@datetime`         = "@datetime"
    val `@localdatetime`    = "@localdatetime"
    val `@epoch`            = "@epoch"
    val `@color`            = "@color"
    val `@geo`              = "@geo"
    val `@geojson`          = "@geojson"
    val `@geopoint`         = "@geopoint"
    val `@geomultipoint`    = "@geomultipoint"
    val `@geoline`          = "@geoline"
    val `@geopolygon`       = "@geopolygon"
    val `@geomultipolygon`  = "@geomultipolygon"
    val `@geomultigeometry` = "@geomultigeometry"
    val `@geomultiline`     = "@geomultiline"
    val geoshape            = NS.vocab.schema + "GeoShape"
    val geocircle           = NS.vocab.schema + "GeoCircle"
    val address             = NS.vocab.schema + "PostalAddress"
    //    val literals = Seq(string, int, double, long, boolean, /*enum, */date, time, datetime, epochtime,
    //      color, geojson, geoshape, geocircle, address, URL)

    val `@context`            = "@context"
    val `@id`                 = "@id"
    val `@ids`                = "@ids"
    val `@value`              = "@value"
    val `@from`               = "@from"
    val `@to`                 = "@to"
    val `@pvalue`             = "p@value"
    val `@name`               = "@name"
    val `@language`           = "@language"
    val `@type`               = "@type"
    val `@container`          = "@container"
    val `@structured`         = "@structured"
    val `@collection`         = "@collection"
    val `@map`                = "@map"
    val `@tuple`              = "@tuple"
    val `@tuple2`             = "@tuple2"
    val `@tuple3`             = "@tuple3"
    val `@tuple4`             = "@tuple4"
    val `@option`             = "@option"
    val `@list`               = "@list"
    val `@set`                = "@set"
    val `@listset`            = "@listset"
    val `@vector`             = "@vector"
    val `@single`             = "@single"
    val `@entry`              = "@entry"
    val `@reverse`            = "@reverse"
    val `@index`              = "@index"
    val `@base`               = "@base"
    val `@vocab`              = "@vocab"
    val `@graph`              = "@graph"
    val `@nest`               = "@nest"
    val `@prefix`             = "@prefix"
    val `@version`            = "@version"
    val `@label`              = "@label"
    val `@comment`            = "@comment"
    val `@resource`           = "@resource"
    val `@class`              = "@class"
    val `@property`           = "@property"
    val `@properties`         = "@properties"
    val `@extends`            = "@extends"
    val `@datatype`           = "@datatype"
    val rdfsClass             = NS.vocab.rdfs + "Class"
    val rdfsSubClassOf        = NS.vocab.rdfs + "subClassOf"
    val rdfsSubPropertyOf     = NS.vocab.rdfs + "subPropertyOf"
    val rdfsDomain            = NS.vocab.rdfs + "domain"
    val rdfsIsDefinedBy       = NS.vocab.rdfs + "isDefinedBy"
    val rdfsLabel             = NS.vocab.rdfs + "label"
    val rdfsComment           = NS.vocab.rdfs + "comment"
    val rdfType               = NS.vocab.rdf + "type"
    val rdfProperty           = NS.vocab.rdf + "Property"
    val schemaClass           = NS.vocab.schema + "Class"
    val schemaDomainIncludes  = NS.vocab.schema + "domainIncludes"
    val schemaSupersededBy    = NS.vocab.schema + "supersededBy"
    val schemaSameAs          = NS.vocab.schema + "sameAs"
    val schemaInverseOf       = NS.vocab.schema + "inverseOf"
    val schemaRange           = NS.vocab.schema + "rangeIncludes"
    val schemaDataType        = NS.vocab.schema + "DataType"
    val schemaTime            = NS.vocab.schema + "Time"
    val schemaDate            = NS.vocab.schema + "Date"
    val schemaText            = NS.vocab.schema + "Text"
    val schemaNumber          = NS.vocab.schema + "Number"
    val schemaFloat           = NS.vocab.schema + "Float"
    val schemaInteger         = NS.vocab.schema + "Integer"
    val schemaDateTime        = NS.vocab.schema + "DateTime"
    val schemaBoolean         = NS.vocab.schema + "Boolean"
    val schemaURL             = NS.vocab.schema + "URL"
    val schemaDuration        = NS.vocab.schema + "Duration"
    val owlEquivalentProperty = NS.vocab.owl + "equivalentProperty"
    val xsdAnyURI             = NS.vocab.xsd + "anyURI"
    val xsdLanguage           = NS.vocab.xsd + "language"
    val xsdHexBinary          = NS.vocab.xsd + "hexBinary"
    val xsdBase64Binary       = NS.vocab.xsd + "base64Binary"
    val xsdBoolean            = NS.vocab.xsd + "boolean"
    val xsdString             = NS.vocab.xsd + "string"
    val xsdShort              = NS.vocab.xsd + "short"
    val xsdByte               = NS.vocab.xsd + "byte"
    val xsdInt                = NS.vocab.xsd + "int"
    val xsdDouble             = NS.vocab.xsd + "double"
    val xsdLong               = NS.vocab.xsd + "long"
    val xsdTime               = NS.vocab.xsd + "time"
    val xsdDate               = NS.vocab.xsd + "date"
    val xsdDateTime           = NS.vocab.xsd + "dateTime"
    val xsdDateTimeStamp      = NS.vocab.xsd + "dateTimeStamp"
    val xsdGYear              = NS.vocab.xsd + "gYear"
    val xsdGMonth             = NS.vocab.xsd + "gMonth"
    val xsdGDay               = NS.vocab.xsd + "gDay"
    val xsdGYearMonth         = NS.vocab.xsd + "gYearMonth"
    val xsdGMonthDay          = NS.vocab.xsd + "gMonthDay"
    val xsdDuration           = NS.vocab.xsd + "duration"
    val xsdYearMonthDuration  = NS.vocab.xsd + "yearMonthDuration"
    val xsdDayTimeDuration    = NS.vocab.xsd + "dayTimeDuration"
    val `@nodeURL`            = "@nodeURL"
    val `@edgeURL`            = "@edgeURL"
    val `@valueURL`           = "@valueURL"
    val `@url`                = "@url"

    val `@range`         = "@range"
    val `@start`         = "@start"
    val `@end`           = "@end"
    val `@createdon`     = "@createdon"
    val `@deletedon`     = "@deletedon"
    val `@modifiedon`    = "@modifiedon"
    val `@transcendedon` = "@transcendedon"

    //custom
    val min = "min"
    val max = "max"

    //    val reservedWords = Seq(context, id, ids, value, language, TYPE, container, list, set, reverse, index, base,
    //      vocab, graph, nest, prefix, version, label, comment)
  }

}
