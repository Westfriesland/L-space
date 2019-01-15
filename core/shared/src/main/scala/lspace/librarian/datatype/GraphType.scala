package lspace.librarian.datatype

import lspace.NS
import lspace.librarian.structure.{Graph, Property}

object GraphType extends DataTypeDef[GraphType[Graph]] {

  lazy val datatype: GraphType[Graph] = new GraphType[Graph] {
    val iri: String                                             = NS.types.`@graph`
    override val label: Map[String, String]                     = Map("en" -> NS.types.`@graph`)
    override val _extendedClasses: () => List[_ <: DataType[_]] = () => List(DataType.datatype)
  }

  object keys extends DataType.Properties
  override lazy val properties: List[Property] = DataType.properties
  trait Properties extends DataType.Properties

  def apply[T <: Graph] = new GraphType[T] {
    val iri: String                                             = NS.types.`@graph`
    override val label: Map[String, String]                     = Map("en" -> NS.types.`@graph`)
    override val _extendedClasses: () => List[_ <: DataType[_]] = () => List(DataType.datatype)
  }
}

trait GraphType[+T <: Graph] extends DataType[T]
