package lspace.librarian.traversal.step

import lspace.librarian.traversal._
import lspace.provider.detached.DetachedGraph
import lspace.provider.wrapped.WrappedNode
import lspace.structure._
import lspace.NS.types
import monix.eval.Task
import shapeless.{HList, HNil, LUBConstraint}

object Project
    extends StepDef("Project", "A project-step ..", () => TraverseStep.ontology :: Nil)
    with StepWrapper[Project[HList]] {

  def toStep(node: Node): Project[HList] =
    Project[HList](
      node
        .out(Project.keys.byTraversal)
        .map(Traversal.toTraversal(_))
        .foldLeft[HList](HNil) { case (hlist, traversal) => traversal :: hlist })

  object keys extends TraverseStep.Properties {
    object by
        extends PropertyDef(
          lspace.NS.vocab.Lspace + "librarian/step/Project/by",
          "by",
          "A traversal ..",
          container = lspace.NS.types.`@list` :: Nil,
          `@range` = () => Traversal.ontology :: Nil
        )
    val byTraversal: TypedProperty[Node] = by.property + Traversal.ontology
  }
  override lazy val properties: List[Property] = keys.by :: TraverseStep.properties
  trait Properties extends TraverseStep.Properties

  implicit def toNode[Traversals <: HList](project: Project[Traversals])
//                                          (
//      implicit
//      lub: LUBConstraint[Traversals, Traversal[_ <: ClassType[_], _ <: ClassType[_], _ <: HList]])
    : Task[Node] = {

    for {
      node <- DetachedGraph.nodes.create(ontology)
      traversals <- Task.gather(
        project.by.runtimeList
          .map(_.asInstanceOf[Traversal[_ <: ClassType[_], _ <: ClassType[_], _ <: HList]])
          .map(_.toNode))
      _ <- Task.gather(traversals.map(node.addOut(keys.byTraversal, _)))
    } yield node
  }

}

case class Project[Traversals <: HList](by: Traversals)
//                                       (
//    implicit
//    lub: LUBConstraint[Traversals, Traversal[_ <: ClassType[_], _ <: ClassType[_], _ <: HList]])
    extends TraverseStep {

  lazy val toNode: Task[Node] = this
  override def prettyPrint: String =
    "project(" + by.runtimeList
      .map(_.asInstanceOf[Traversal[_ <: ClassType[_], _ <: ClassType[_], _ <: HList]])
      .map(_.toString)
      .map("_." + _)
      .mkString(", ") + ")"
}
