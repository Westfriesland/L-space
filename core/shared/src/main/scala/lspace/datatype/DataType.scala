package lspace.datatype

import java.util.concurrent.ConcurrentHashMap

import lspace.NS
import lspace.NS.types
import lspace.structure.util.ClassTypeable
import lspace.structure.OntologyDef
import lspace.structure._
import monix.eval.{Coeval, Task}

import scala.collection.concurrent
import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration

object DataType
    extends OntologyDef(NS.types.`@datatype`,
                        Set(NS.types.`@datatype`, NS.types.schemaDataType),
                        NS.types.`@datatype`,
                        `@extends` = () => Ontology.ontology :: Nil) {

  object keys

  lazy val datatype: DataType[Any] = new DataType[Any] {
    val iri: String                = NS.types.`@datatype`
    override val iris: Set[String] = Set(NS.types.schemaDataType)
    labelMap = Map("en" -> NS.types.`@datatype`)
  }

  def urlType[T]: IriType[T] = new IriType[T] {
    val iri: String = NS.types.`@datatype`
  }

  /*private def build(node: Node): Coeval[DataType[_]] = {
    if (node.hasLabel(ontology).nonEmpty) {
          node
            .out(Property.default.`@extends`)
            .headOption
            .collect {
              case nodes: List[_] =>
                nodes.collect {
                  case node: Node if node.hasLabel(DataType.ontology).isDefined =>
                    datatypes
                      .get(node.iri)
                      .getOrElse {
                        datatypes.getAndUpdate(node)
                      } //orElse???
                  case iri: String =>
                    datatypes
                      .get(iri)
                      .getOrElse(throw new Exception("@extends looks like an iri but cannot be wrapped by a property"))
                }
              case node: Node if node.hasLabel(DataType.ontology).isDefined =>
                List(datatypes.get(node.iri).getOrElse(datatypes.getAndUpdate(node)))
            }
            .toList
            .flatten
        .flatMap { extended =>
          extended.head match {
            case dt: CollectionType[_] =>
              dt match {
                case dt: ListType[_] =>
                  Coeval
                    .sequence(
                      node
                        .out(ListType.keys.valueRangeClassType)
                        .map(types => Coeval.sequence(types.map(ClassType.classtypes.getOrBuild))))
                    .map { types =>
                      ListType(types.flatten)
                    }
                case dt: ListSetType[_] =>
                  Coeval
                    .sequence(
                      node
                        .out(ListSetType.keys.valueRangeClassType)
                        .map(types => Coeval.sequence(types.map(ClassType.classtypes.getOrBuild))))
                    .map { types =>
                      ListSetType(types.flatten)
                    }
                case dt: SetType[_] =>
                  Coeval
                    .sequence(
                      node
                        .out(SetType.keys.valueRangeClassType)
                        .map(types => Coeval.sequence(types.map(ClassType.classtypes.getOrBuild))))
                    .map { types =>
                      SetType(types.flatten)
                    }
                case dt: VectorType[_] =>
                  Coeval
                    .sequence(
                      node
                        .out(VectorType.keys.valueRangeClassType)
                        .map(types => Coeval.sequence(types.map(ClassType.classtypes.getOrBuild))))
                    .map { types =>
                      VectorType(types.flatten)
                    }
                case dt: MapType[_, _] =>
                  for {
                    keyRange <- Coeval
                      .sequence(
                        node
                          .out(MapType.keys.keyRangeClassType)
                          .map(types => Coeval.sequence(types.map(ClassType.classtypes.getOrBuild))))
                    valueRange <- Coeval
                      .sequence(
                        node
                          .out(MapType.keys.valueRangeClassType)
                          .map(types => Coeval.sequence(types.map(ClassType.classtypes.getOrBuild))))
                  } yield {
                    MapType(keyRange.flatten, keyRange.flatten)
                  }
                case dt: TupleType[_] =>
                  node
                    .out(TupleType.keys._rangeClassType)
                    .map(list =>
                      Coeval.sequence(list.map(types => Coeval.sequence(types.map(ClassType.classtypes.getOrBuild)))))
                    .head
                    .map(TupleType(_))
              }
            case _ => Coeval.raiseError(new Exception(""))
          }
        }
    } else {
      //      new Exception(s"${node.iri} with id ${node.id} is not an ontology, labels: ${node.labels.map(_.iri)}")
      //        .printStackTrace()
      Coeval.raiseError(
        new Exception(s"${node.iri} with id ${node.id} ${node.outE(Property.default.`@id`).head.to.id} " +
          s"${node.graph.values.hasId(node.outE(Property.default.`@id`).head.to.id).isDefined} is not an ontology, labels: ${node.labels
            .map(_.iri)}"))
    }
  }*/

  object datatypes {

    /**
      * imcomplete...
      */
    object default {
      import DataType.default._
      val datatypes = List(
        `@literal`,
        `@string`,
        `@number`,
        `@int`,
        `@double`,
        `@long`,
        `@temporal`,
        `@date`,
        `@datetime`,
        `@localdatetime`,
        `@time`,
        `@duration`,
        `@boolean`,
        `@geo`,
        `@geopoint`,
        `@graph`,
        `@url`,
        `@nodeURL`,
        `@edgeURL`,
        `@valueURL`,
        ListType.datatype,
        ListSetType.datatype,
        SetType.datatype,
        VectorType.datatype,
        MapType.datatype,
        TupleType.datatype
        //      `@class`,
        //      `@property`,
        //      `@datatype`
      )
      if (datatypes.size > 99) throw new Exception("extend default-datatype-id range!")
      val byId    = (0l to datatypes.size - 1 toList).zip(datatypes).toMap
      val byIri   = byId.toList.flatMap { case (id, dt) => dt.iri :: dt.iris.toList map (_ -> dt) }.toMap
      val idByIri = byId.toList.flatMap { case (id, dt) => dt.iri :: dt.iris.toList map (_ -> id) }.toMap
    }
    private[lspace] val byIri: concurrent.Map[String, DataType[_]] =
      new ConcurrentHashMap[String, DataType[_]]().asScala
    private[lspace] val building: concurrent.Map[String, Coeval[DataType[_]]] =
      new ConcurrentHashMap[String, Coeval[DataType[_]]]().asScala

    def all: List[DataType[_]] = byIri.values.toList.distinct
//    def get(iri: String): Option[Coeval[DataType[_]]] =
//      default.byIri
//        .get(iri)
//        .orElse(byIri.get(iri))
//        .map(o => Coeval.now(o))
//        .orElse(building.get(iri))
    def get(iri: String, iris: Set[String] = Set()): Option[DataType[_]] = {
      val allIris = (iris + iri)
      allIris.flatMap(iri => default.byIri.get(iri).orElse(byIri.get(iri))).toList match {
        case List(datatype) => Some(datatype)
        case Nil            => None
        case datatypes =>
          scribe.warn(
            "It looks like multiple datatypes which have some @id's in common are found, this should not happen...")
          datatypes.headOption
      }
    }
    def getOrCreate(iri: String, iris: Set[String]): DataType[_] = get(iri, iris).getOrElse {
      synchronized {
        get(iri, iris).getOrElse {
          val datatype = (iris + iri).flatMap(CollectionType.get).toList match {
            case List(datatype) => datatype
            case Nil            => throw new Exception(s"could not build collectiontype for @id's ${iris + iri}")
            case datatypes =>
              scribe.warn(
                "It looks like multiple datatypes which have some @id's in common are found, this should not happen...")
              datatypes.head
          }
          datatype.iris.foreach(byIri.update(_, datatype))
          datatype
        }
      }
    }
    def getAndUpdate(node: Node): DataType[_] = {
      val datatype = getOrCreate(node.iri, node.iris)

      datatype.label ++ node
        .outE(Property.default.typed.labelString)
        .flatMap { edge =>
          val l = edge.out(Property.default.typed.languageString)
          if (l.nonEmpty) l.map(_ -> edge.to.value)
          else List("en"          -> edge.to.value)
        }
        .toMap
      datatype.comment ++ node
        .outE(Property.default.typed.commentString)
        .flatMap { edge =>
          val l = edge.out(Property.default.typed.commentString)
          if (l.nonEmpty) l.map(_ -> edge.to.value)
          else List("en"          -> edge.to.value)
        }
        .toMap

      datatype.properties ++ (node
        .out(Property.default.typed.propertyProperty) ++ node
        .in(lspace.NS.types.schemaDomainIncludes)
        .collect { case node: Node => node })
        .filter(_.labels.contains(Property.ontology))
        .map(Property.properties.getAndUpdate)

      datatype.extendedClasses ++ node
        .out(Property.default.`@extends`)
        .headOption
        .collect {
          case nodes: List[_] =>
            nodes.collect {
              case node: Node if node.hasLabel(DataType.ontology).isDefined =>
                datatypes
                  .get(node.iri)
                  .getOrElse {
                    datatypes.getAndUpdate(node)
                  } //orElse???
              case iri: String =>
                datatypes
                  .get(iri)
                  .getOrElse(throw new Exception("@extends looks like an iri but cannot be wrapped by a property"))
            }
          case node: Node if node.hasLabel(DataType.ontology).isDefined =>
            List(datatypes.get(node.iri).getOrElse(datatypes.getAndUpdate(node)))
        }
        .toList
        .flatten

      datatype
    }

//    def cache(datatype: DataType[_]): Unit = {
//      byIri += datatype.iri -> datatype
//      datatype.iris.foreach { iri =>
//        datatypes.byIri += iri -> datatype
//      }
//    }
    def cached(long: Long): Option[DataType[_]] = default.byId.get(long)
//    def cached(iri: String): Option[DataType[_]] = default.byIri.get(iri).orElse(byIri.get(iri))

//    def remove(iri: String): Unit = byIri.remove(iri)
  }

  implicit def clsDatatype[T]: ClassTypeable.Aux[DataType[T], T, DataType[T]] = new ClassTypeable[DataType[T]] {
    type C  = T
    type CT = DataType[T]
    def ct: CT = new DataType[T] { val iri: String = "" }
  }

  private lazy val _this = this
  object default {
    val `@url` = IriType.datatype

    val `@nodeURL`  = NodeURLType.datatype
    val `@edgeURL`  = EdgeURLType.datatype
    val `@valueURL` = ValueURLType.datatype
    val `@class`    = Ontology.urlType
    val `@property` = Property.urlType
    val `@datatype` = DataType.urlType[DataType[Any]]

    val `@literal`: LiteralType[Any]   = LiteralType.datatype
    val `@string`                      = TextType.datatype
    val `@number`: NumericType[AnyVal] = NumericType.datatype
    val `@int`                         = IntType.datatype
    val `@double`                      = DoubleType.datatype
    val `@long`                        = LongType.datatype
    val `@date`                        = LocalDateType.datatype
    val `@datetime`                    = DateTimeType.datatype
    val `@localdatetime`               = LocalDateTimeType.datatype
    val `@time`                        = LocalTimeType.datatype
    val `@temporal`: CalendarType[Any] = CalendarType.datatype
    val `@duration`: DurationType      = DurationType.datatype
    val `@quantity`: QuantityType[Any] = QuantityType.datatype
    //  val epochType: EpochType = EpochType
    val `@boolean` = BoolType.datatype

    val `@geo`                   = GeometricType.datatype
    val `@geopoint`              = GeopointType.datatype
    val `@geomultipoint`         = GeoMultipointType.datatype
    val `@geoline`               = GeoLineType.datatype
    val `@geomultiline`          = GeoMultiLineType.datatype
    val `@geopolygon`            = GeoPolygonType.datatype
    val `@geomultipolygon`       = GeoMultiPolygonType.datatype
    val `@geomultigeo`           = GeoMultiGeometryType.datatype
    val `@color`: ColorType[Any] = ColorType.datatype
    val `@graph`                 = GraphType.datatype

    val `@structured` = StructuredType.datatype

    def vectorType[V](ct: ClassType[V]): VectorType[V] =
      VectorType(List(ct.asInstanceOf[ClassType[V]]))
    def vectorType[T](implicit tpe: ClassTypeable[T]): VectorType[T] =
      VectorType(List(tpe.ct.asInstanceOf[ClassType[T]])).asInstanceOf[VectorType[T]]
    def vectorType(): VectorType[Any] = VectorType(List[ClassType[Any]]())
    def listType[V](ct: ClassType[V]): ListType[V] =
      ListType(List(ct.asInstanceOf[ClassType[V]]))
    def listType[T](implicit tpe: ClassTypeable[T]): ListType[T] =
      ListType(List(tpe.ct.asInstanceOf[ClassType[T]])).asInstanceOf[ListType[T]]
    def listType(): ListType[Any] = ListType(Nil)
    def listsetType[V](ct: ClassType[V]): ListSetType[V] =
      ListSetType(List(ct.asInstanceOf[ClassType[V]]))
    def listsetType[T](implicit tpe: ClassTypeable[T]): ListSetType[T] =
      ListSetType(List(tpe.ct.asInstanceOf[ClassType[T]])).asInstanceOf[ListSetType[T]]
    def listsetType() = ListSetType(List[ClassType[Any]]())
    def setType[V](ct: ClassType[V]): SetType[V] =
      SetType(List(ct.asInstanceOf[ClassType[V]]))
    def setType[T](implicit tpe: ClassTypeable[T]): SetType[T] =
      SetType(List(tpe.ct.asInstanceOf[ClassType[T]])).asInstanceOf[SetType[T]]
    def setType(): SetType[Any] = SetType(List[ClassType[Any]]())
    def mapType[K, V](kct: ClassType[K], vct: ClassType[V]): MapType[K, V] =
      MapType(List(kct.asInstanceOf[ClassType[K]]), List(vct.asInstanceOf[ClassType[V]]))
    def mapType[K, V](implicit ktpe: ClassTypeable[K], vtpe: ClassTypeable[V]): MapType[K, V] =
      MapType(List(ktpe.ct.asInstanceOf[ClassType[K]]), List(vtpe.ct.asInstanceOf[ClassType[V]])) //.asInstanceOf[MapType[K, V]]
    def mapType(): MapType[Any, Any]                  = MapType(List[ClassType[Any]](), List[ClassType[Any]]())
    def tupleType[T](ct: ClassType[_]*): TupleType[T] = TupleType[T](ct.toList.map(List(_)))
//    def tupleType[A, AT[+Z] <: ClassType[Z], ATOut <: ClassType[_], B, BT[+Z] <: ClassType[Z], BTOut <: ClassType[_]](
//                                                                                                                        act: AT[A],
//                                                                                                                        bct: BT[B]) = TupleType(List(act), List(bct))
//    def tupleType[A, B](implicit cta: ClassTypeable[A], ctb: ClassTypeable[B]) =
//      TupleType(List(cta.ct), List(ctb.ct)).asInstanceOf[TupleType[A, B]]
//    def tupleType() = TupleType(List(), List())

    val default = new DataType[Any] {
      type Out = Any
      val iri: String = ""
    }
  }
}

/**
  *
  * @tparam T
  */
trait DataType[+T] extends ClassType[T] { self =>
//  type CT = DataType[_]

  def iris: Set[String]                           = Set() + iri
  def _extendedClasses: () => List[DataType[Any]] = () => List()
  def _properties: () => List[Property]           = () => List()

  protected var extendedClassesList: Coeval[List[DataType[Any]]] = Coeval.delay(_extendedClasses()).memoizeOnSuccess
//  override def extendedClasses: List[DataType[Any]]              = extendedClassesList.value()
  object extendedClasses {
    def apply(): List[DataType[Any]] = extendedClassesList()
    def all(): Set[DataType[Any]]    = extendedClasses().toSet ++ extendedClasses().flatMap(_.extendedClasses.all())
    def apply(iri: String): Boolean =
      extendedClassesList().exists(_.iris.contains(iri)) || extendedClassesList().exists(_.extendedClasses(iri))

    def +(parent: DataType[Any]): this.type = this.synchronized {
      if (!parent.`@extends`(self))
        extendedClassesList = extendedClassesList.map(_ :+ parent).map(_.distinct).memoizeOnSuccess
      else scribe.warn(s"$iri cannot extend ${parent.iri} as ${parent.iri} already extends $iri direct or indirect")
      this
    }
    def ++(parent: Iterable[DataType[Any]]): this.type = this.synchronized {
      parent.foreach(this.+)
      this
    }
    def -(parent: DataType[Any]): this.type = this.synchronized {
      extendedClassesList = extendedClassesList.map(_.filterNot(_ == parent)).memoizeOnSuccess
      this
    }
    def --(parent: Iterable[DataType[Any]]): this.type = this.synchronized {
      extendedClassesList = extendedClassesList.map(_.filterNot(parent.toList.contains)).memoizeOnSuccess
      this
    }
  }

  override def toString: String = s"datatype:$iri"
}