package lspace.lgraph.store

import java.time.Instant

import lspace.lgraph.LResource.LinksSet
import lspace.lgraph.{LGraph, LResource}
import lspace.librarian.structure.Property.default.{`@id`, `@ids`}
import lspace.librarian.structure.store.EdgeStore
import lspace.librarian.structure.{Edge, Property}

import scala.collection.mutable
import scala.concurrent.duration._

object LEdgeStore {
  def apply[G <: LGraph](iri: String, graph: G): LEdgeStore[G] = new LEdgeStore(iri, graph)
}

class LEdgeStore[G <: LGraph](val iri: String, val graph: G) extends LStore[G] with EdgeStore[G] {
  override def store(edge: T): Unit = {
//    if ((edge.key == `@id` || edge.key == `@ids`) && edge.to.isInstanceOf[graph._Value[String]])
//      graph.`@idStore`.store(edge.to.asInstanceOf[graph._Value[String]])

    super.store(edge)

    graph.storeManager
      .storeEdges(List(edge))
      .runSyncUnsafe(15 seconds)(monix.execution.Scheduler.global, monix.execution.schedulers.CanBlock.permit)
    //      .runToFuture(monix.execution.Scheduler.global)
  }

  override def store(edges: List[T]): Unit = {
    edges.foreach(super.store)
    graph.storeManager
      .storeEdges(edges)
      .runSyncUnsafe(15 seconds)(monix.execution.Scheduler.global, monix.execution.schedulers.CanBlock.permit)
    //      .runToFuture(monix.execution.Scheduler.global)
  }

  override def cache(edge: T): Unit = {

    val linksOut = edge.from
      .asInstanceOf[LResource[Any]]
      .linksOut
      .getOrElse(edge.key, LResource.LinksSet[Any, Any]())

    val linksIn = edge.to
      .asInstanceOf[LResource[Any]]
      .linksIn
      .getOrElse(edge.key, LResource.LinksSet[Any, Any]())

    val cacheTime = Instant.now()

    if (edge.from
          .asInstanceOf[LResource[Any]]
          ._lastoutsync
          .exists(_.isAfter(cacheTime.minusSeconds(120)))) {
      edge.from
        .asInstanceOf[LResource[Any]]
        .linksOut += edge.key -> linksOut.copy(
        lastsync = Some(cacheTime),
        links = mutable.LinkedHashSet[Edge[Any, Any]]() ++= (linksOut.links
          .asInstanceOf[mutable.LinkedHashSet[Edge[Any, Any]]] += edge.asInstanceOf[Edge[Any, Any]]).toList
          .sortBy(_.id)
      )

      edge.to
        .asInstanceOf[LResource[Any]]
        .linksIn += edge.key -> linksIn.copy(
        lastsync = Some(cacheTime),
        links = mutable.LinkedHashSet[Edge[Any, Any]]() ++= (linksIn.links
          .asInstanceOf[mutable.LinkedHashSet[Edge[Any, Any]]] += edge.asInstanceOf[Edge[Any, Any]]).toList.sortBy(_.id)
      )
    } else {
      edge.from
        .asInstanceOf[LResource[Any]]
        .linksOut += edge.key -> linksOut.copy(
        links = mutable.LinkedHashSet[Edge[Any, Any]]() ++= (linksOut.links
          .asInstanceOf[mutable.LinkedHashSet[Edge[Any, Any]]] += edge.asInstanceOf[Edge[Any, Any]]).toList
          .sortBy(_.id))

      edge.to
        .asInstanceOf[LResource[Any]]
        .linksIn += edge.key -> linksIn.copy(links = mutable.LinkedHashSet[Edge[Any, Any]]() ++= (linksIn.links
        .asInstanceOf[mutable.LinkedHashSet[Edge[Any, Any]]] += edge.asInstanceOf[Edge[Any, Any]]).toList.sortBy(_.id))
    }

    super.cache(edge)
    if (edge.key == Property.default.`@id` || edge.key == Property.default.`@ids`) edge.from match {
      case node: graph._Node =>
        graph.nodeStore.store(node)
      case edge: graph._Edge[_, _] =>
        graph.edgeStore.store(edge)
      case value: graph._Value[_] =>
        graph.valueStore.store(value)
    }
  }

  override def uncache(edge: T): Unit = {

    if (edge.key == Property.default.`@id` || edge.key == Property.default.`@ids`) edge.from match {
      case node: graph._Node =>
        graph.nodeStore.uncacheByIri(node)
      case edge: graph._Edge[_, _] =>
        graph.edgeStore.uncacheByIri(edge)
      case value: graph._Value[_] =>
        graph.valueStore.uncacheByIri(value)
    }

    val linksOut = edge.from
      .asInstanceOf[LResource[Any]]
      .linksOut
      .getOrElse(edge.key, LResource.LinksSet[Any, Any]())

    val linksIn = edge.to
      .asInstanceOf[LResource[Any]]
      .linksIn
      .getOrElse(edge.key, LResource.LinksSet[Any, Any]())

    linksOut.links.asInstanceOf[mutable.LinkedHashSet[Edge[Any, _]]] - edge.asInstanceOf[Edge[Any, _]] match {
      case set if set.isEmpty =>
        edge.from
          .asInstanceOf[LResource[Any]]
          .linksOut -= edge.key
      case set =>
        edge.from
          .asInstanceOf[LResource[Any]]
          .linksOut += edge.key -> LinksSet(linksOut.lastsync, set.asInstanceOf[mutable.LinkedHashSet[Edge[Any, Any]]])
    }

    linksIn.links.asInstanceOf[mutable.LinkedHashSet[Edge[_, Any]]] - edge.asInstanceOf[Edge[_, Any]] match {
      case set if set.isEmpty =>
        edge.to
          .asInstanceOf[LResource[Any]]
          .linksIn -= edge.key
      case set =>
        edge.to
          .asInstanceOf[LResource[Any]]
          .linksIn += edge.key -> LinksSet(linksOut.lastsync, set.asInstanceOf[mutable.LinkedHashSet[Edge[Any, Any]]])
    }

    super.uncache(edge)
  }

  override def byIri(iri: String): Stream[T] = super.byIri(iri) ++ graph.storeManager.edgeByIri(iri) distinct

  override def byId(id: Long): Option[T] = super.byId(id).orElse(graph.storeManager.edgeById(id).headOption)

  def byId(ids: List[Long]): Stream[T] = {
    val byCache          = ids.map(id => id -> byId(id))
    val (noCache, cache) = byCache.partition(_._2.isEmpty)
    (cache.flatMap(_._2) toStream) ++ (if (noCache.nonEmpty) graph.storeManager.edgesById(noCache.map(_._1))
                                       else Stream())
  }

  def byId(fromId: Option[Long] = None, key: Option[Property] = None, toId: Option[Long] = None): Stream[T] = {
    fromId match {
      case None =>
        key match {
          case None =>
            toId match {
              case None       => all()
              case Some(toId) => graph.storeManager.edgesByToId(toId)
            }
          case Some(key) =>
            toId match {
              case None =>
                val keyId = graph.ns.nodes.hasIri(key.iri).head.id
                Stream() //graph.storeManager.edgesByKey(key)
              case Some(toId) => graph.storeManager.edgesByToIdAndKey(toId, key)
            }
        }
      case Some(fromId) =>
        key match {
          case None =>
            toId match {
              case None       => graph.storeManager.edgesByFromId(fromId)
              case Some(toId) => graph.storeManager.edgesByFromIdAndToId(fromId, toId)
            }
          case Some(key) =>
            toId match {
              case None       => graph.storeManager.edgesByFromIdAndKey(fromId, key)
              case Some(toId) => graph.storeManager.edgesByFromIdAndKeyAndToId(fromId, key, toId)
            }
        }
    }
  }

  def byIri(fromIri: Option[String] = None, key: Option[Property] = None, toIri: Option[String] = None): Stream[T] = ???

  override def delete(edge: T): Unit = {
    super.delete(edge)
    graph.storeManager
      .deleteEdges(List(edge))
      .runSyncUnsafe(15 seconds)(monix.execution.Scheduler.global, monix.execution.schedulers.CanBlock.permit)
    //      .runToFuture(monix.execution.Scheduler.global)
  }

  override def delete(edges: List[T]): Unit = {
    edges.foreach(super.delete)
    graph.storeManager
      .deleteEdges(edges)
      .runSyncUnsafe(15 seconds)(monix.execution.Scheduler.global, monix.execution.schedulers.CanBlock.permit)
    //      .runToFuture(monix.execution.Scheduler.global)
  }

  def all(): Stream[graph._Edge[_, _]] = graph.storeManager.edges
  def count(): Long                    = graph.storeManager.edgeCount()
}