package lspace.structure

import lspace.datatype.DataType
import lspace.librarian.logic.predicate.P
import lspace.librarian.traversal.{Segment, Step, Traversal, UntypedTraversal, step => _step}
import lspace.librarian.traversal.step.Has
import lspace.structure.index.Index
import lspace.structure.util.IdProvider
import monix.eval.Task
import monix.execution.CancelableFuture
import shapeless.HList

trait IndexGraph extends Graph {
  def graph: Graph
  def ns: NameSpaceGraph = graph.ns

  lazy val idProvider: IdProvider = graph.idProvider

//  protected def `@patternIndex`: Index
  protected def `@typeIndex`: Index

  lazy val init: CancelableFuture[Unit] = CancelableFuture.unit
  def getIndex(traversal: UntypedTraversal): Task[Option[Index]]
  protected def createIndex(traversal: UntypedTraversal): Task[Index]

  implicit def stepListToTraversal(steps: List[Step]): Traversal[ClassType[Any], ClassType[Any], HList] =
    Traversal(steps.toVector)
//  def findIndex(traversal: UntypedTraversal): List[Node] = {
//    stepListToTraversal(
//      g.N
//        .hasLabel(Index.ontology)
//        .steps ::: traversal.segments.zipWithIndex.foldLeft(List[Step]()) {
//        case (stepList, (segment, count)) =>
//          val steps = segment.stepsList.collect { case step: Has => step }
//          import _step._
//          Where(
//            Out(Set(Index.keys.traversal.property)) :: Out(Set(Traversal.keys.segment.property)) ::
//              Range(count, count) :: Out(Set(Segment.keys.step.property)) :: Has(
//              Has.keys.key.property,
//              Some(P.||(steps.map(_.key).map(P.eqv(_)): _*))) :: Nil) :: stepList
//      }).untyped.toTyped.toList.asInstanceOf[List[Node]]
//  }

  def getOrCreateIndex(traversal: UntypedTraversal): Task[Index] = {
    //TODO: log when existing index is returned and no new index is created

    getIndex(traversal).flatMap(_.map(Task.now).getOrElse(createIndex(traversal)))
  }
  def deleteIndex(index: Index): Task[Unit]

//  def find[T](predicates: List[P[T]], property: Property): List[Resource[T]] = {
//    getIndex(Shape(property)).toList
//      .flatMap(_.find(predicates, property))
//  }
//
//  def find(values: Vector[Map[Property, List[P[_]]]]): List[Vector[Resource[_]]] = {
//    getIndex(values.map(_.keySet).map(Shape(_))).toList.flatMap(_.find(values))
//  }

  override protected def deleteNode(node: GNode): Task[Unit] = {
    //    `@typeIndex`.delete()
    super.deleteNode(node)
  }

  abstract override protected def createEdge[S, E](id: Long,
                                                   from: GResource[S],
                                                   key: Property,
                                                   to: GResource[E]): Task[GEdge[S, E]] =
    for {
      edge <- super.createEdge(id, from, key, to)
      u    <- storeEdge(edge.asInstanceOf[GEdge[_, _]])
    } yield edge

  override protected def deleteEdge(edge: GEdge[_, _]): Task[Unit] =
    super.deleteEdge(edge)

  abstract override protected def createValue[T](_id: Long, _value: T, dt: DataType[T]): Task[GValue[T]] =
    for {
      value <- super.createValue(_id, _value, dt)
      u     <- storeValue(value.asInstanceOf[GValue[_]])
    } yield value

  override protected def deleteValue(value: GValue[_]): Task[Unit] =
    super.deleteValue(value)

}
