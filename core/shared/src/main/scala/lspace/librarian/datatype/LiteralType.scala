package lspace.librarian.datatype

import lspace.NS
import lspace.librarian.process.traversal.helper.ClassTypeable
import lspace.librarian.structure.Property

trait LiteralType[+T] extends DataType[T]

object LiteralType extends DataTypeDef[LiteralType[Any]] {

  lazy val datatype: LiteralType[Any] = new LiteralType[Any] {
    val iri: String                                             = NS.types.`@literal`
    override val label: Map[String, String]                     = Map("en" -> NS.types.`@literal`)
    override val _extendedClasses: () => List[_ <: DataType[_]] = () => List(DataType.datatype)
  }

  object keys extends DataType.Properties
  override lazy val properties: List[Property] = DataType.properties
  trait Properties extends DataType.Properties

  implicit def clsLiteral[T]: ClassTypeable.Aux[LiteralType[T], T, LiteralType[T]] = new ClassTypeable[LiteralType[T]] {
    type C  = T
    type CT = LiteralType[T]
    def ct: CT = new LiteralType[T] { val iri: String = NS.types.`@literal` }
  }
}
