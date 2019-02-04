package lspace.librarian.process.traversal.p

import lspace.librarian.process.traversal.P.CollectionHelper
import lspace.librarian.process.traversal._
import lspace.librarian.provider.detached.DetachedGraph
import lspace.librarian.structure._

//object Without
//    extends PredicateDef("Without", `@extends` = () => List(CollectionP.ontology))
//    with PredicateWrapper[Without[_]] {
//
//  def toP(node: Node): Without[_] = Without(node.out(EqP.keys.value))
//
//  object keys extends CollectionP.Properties
//  override lazy val properties: List[Property] = CollectionP.properties
//  trait Properties extends CollectionP.Properties
//
//  implicit def toNode[T](without: Without[T]): Node = {
//    val node = DetachedGraph.nodes.create(ontology)
//    without.pvalue.foreach(pvalue => node.addOut(EqP.keys.value, ClassType.valueToOntologyResource(pvalue), pvalue))
//    node
//  }
//}
//
//case class Without[T](pvalue: T)(implicit helper: CollectionHelper[T]) extends CollectionP[T] {
//  def assert(avalue: Any): Boolean = !helper.within(avalue, pvalue)
//
//  lazy val toNode: Node            = this
//  override def prettyPrint: String = s"without(${pvalue.mkString(", ")})"
//}