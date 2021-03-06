package lspace.codec.json

import java.time.{Instant, LocalDate, LocalDateTime, LocalTime}

import lspace.codec.json.geojson.GeoJsonDecoder
import lspace.types.geo.{Geometry, Line, MultiGeometry, MultiLine, MultiPoint, MultiPolygon, Point, Polygon}
import monix.eval.Task

import scala.collection.immutable.Map
import scala.util.Try

object Decoder {
//  type Aux[Json0] = Decoder[Json0] { type Json = Json0 }
}
trait Decoder[Json] extends lspace.codec.Decoder {
//  type Json
  lazy val geoJsonDecoder: GeoJsonDecoder[Json] = GeoJsonDecoder(this)

  def parse(string: String): Task[Json]

  def jsonIsNull(json: Json): Boolean

  implicit def jsonToList(json: Json): Option[List[Json]]
  implicit def jsonToMap(json: Json): Option[Map[String, Json]]
  implicit def jsonToString(json: Json): Option[String]
  implicit def jsonToBoolean(json: Json): Option[Boolean]
  implicit def jsonToInt(json: Json): Option[Int]
  implicit def jsonToDouble(json: Json): Option[Double]
  implicit def jsonToLong(json: Json): Option[Long]
  implicit def jsonToDateTime(json: Json): Option[Instant] = json.string.flatMap(s => Try(Instant.parse(s)).toOption)
  implicit def jsonToLocalDateTime(json: Json): Option[LocalDateTime] =
    json.string.flatMap(s => Try(LocalDateTime.parse(s)).toOption)
  implicit def jsonToDate(json: Json): Option[LocalDate] = json.string.flatMap(s => Try(LocalDate.parse(s)).toOption)
  implicit def jsonToTime(json: Json): Option[LocalTime] = json.string.flatMap(s => Try(LocalTime.parse(s)).toOption)
//  def jsonToGeo(json: Json): Option[Geometry]
  implicit def jsonToGeopoint(json: Json): Option[Geometry] = geoJsonDecoder.decodeGeometryOption(json)
//  implicit def jsonToGeopolygon(json: Json): Option[Polygon]

  implicit class WithJson(json: Json) {
    def isNull: Boolean                         = jsonIsNull(json)
    def int: Option[Int]                        = jsonToInt(json)
    def double: Option[Double]                  = jsonToDouble(json)
    def long: Option[Long]                      = jsonToLong(json)
    def localdatetime: Option[LocalDateTime]    = jsonToLocalDateTime(json)
    def datetime: Option[Instant]               = jsonToDateTime(json)
    def time: Option[LocalTime]                 = jsonToTime(json)
    def date: Option[LocalDate]                 = jsonToDate(json)
    def string: Option[String]                  = jsonToString(json)
    def list: Option[List[Json]]                = jsonToList(json)
    def obj: Option[Map[String, Json]]          = jsonToMap(json)
    def boolean: Option[Boolean]                = jsonToBoolean(json)
    def geo: Option[Geometry]                   = geoJsonDecoder.decodeGeometryOption(json)
    def geoPoint: Option[Point]                 = geoJsonDecoder.jsonToPoint(json)
    def geoMultiPoint: Option[MultiPoint]       = geoJsonDecoder.jsonToMultiPoint(json)
    def geoLine: Option[Line]                   = geoJsonDecoder.jsonToLine(json)
    def geoMultiLine: Option[MultiLine]         = geoJsonDecoder.jsonToMultiLine(json)
    def geoPolygon: Option[Polygon]             = geoJsonDecoder.jsonToPolygon(json)
    def geoMultiPolygon: Option[MultiPolygon]   = geoJsonDecoder.jsonToMultiPolygon(json)
    def geoMultiGeometry: Option[MultiGeometry] = geoJsonDecoder.jsonToMultiGeometry(json)
  }
}
