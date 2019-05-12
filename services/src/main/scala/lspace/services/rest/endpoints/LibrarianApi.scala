package lspace.services.rest.endpoints

import java.time.Instant

import cats.effect.IO
import io.finch.{Application, Bootstrap, Endpoint, Ok}
import lspace._
import lspace.codec.{jsonld, ActiveContext, ContextedT, NativeTypeDecoder, NativeTypeEncoder}
import lspace.librarian.traversal.Collection
import lspace.provider.detached.DetachedGraph
import lspace.services.LApplication
import monix.eval.Task
import shapeless.{:+:, CNil, HList}
import scribe._

object LibrarianApi {
  def apply[Json0](graph0: Graph, activeContext0: ActiveContext = ActiveContext())(
      implicit baseDecoder0: NativeTypeDecoder.Aux[Json0],
      baseEncoder0: NativeTypeEncoder.Aux[Json0]): LibrarianApi =
    new LibrarianApi {
      val graph: Graph = graph0
      type Json = Json0
      implicit val activeContext                                     = activeContext0
      implicit override def baseDecoder: NativeTypeDecoder.Aux[Json] = baseDecoder0
      implicit override def baseEncoder: NativeTypeEncoder.Aux[Json] = baseEncoder0
    }
}

trait LibrarianApi extends ExecutionApi {
  def graph: Graph

  type Json
  implicit def baseDecoder: NativeTypeDecoder.Aux[Json]
  implicit def baseEncoder: NativeTypeEncoder.Aux[Json]
  implicit def activeContext: ActiveContext

  import lspace.services.codecs.Decode._
  import lspace.decode.DecodeJsonLD.jsonldToTraversal
  implicit lazy val decoder = lspace.codec.jsonld.Decoder(DetachedGraph)
  import Implicits.AsyncGuide.guide
  import Implicits.Scheduler.global

  def streamingQuery: Endpoint[IO, _root_.fs2.Stream[IO, ContextedT[Collection[Any, ClassType[Any]]]]] = {
    import io.finch.internal.HttpContent
    import cats.effect._, _root_.fs2._
    import io.finch.fs2._
    import _root_.fs2.interop.reactivestreams._
    import scala.concurrent.ExecutionContext
    implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    post(
      "querystream" :: body[Task[Traversal[ClassType[Any], ClassType[Any], HList]],
                            lspace.services.codecs.Application.JsonLD]).mapOutputAsync {
      traversalTask: Task[Traversal[ClassType[Any], ClassType[Any], HList]] =>
        traversalTask
          .map { traversal =>
            //            println(s"executing ${traversal.prettyPrint}")
            val start = Instant.now()
            traversal.untyped
              .withGraph(graph)
              .apply()
              .bufferTumbling(100)
              .map { values =>
                val collection: Collection[Any, ClassType[Any]] = Collection(start, Instant.now(), values.toList)
                collection.logger.debug("result count: " + values.size.toString)
                ContextedT(collection)
              }
              .toReactivePublisher
              .toStream[IO]()
          }
          .map(Ok(_))
          .toIO
    }
  }
  def query: Endpoint[IO, ContextedT[Collection[Any, ClassType[Any]]]] = {
    post(
      "query" :: body[Task[Traversal[ClassType[Any], ClassType[Any], HList]],
                      lspace.services.codecs.Application.JsonLD]).mapOutputAsync {
      traversalTask: Task[Traversal[ClassType[Any], ClassType[Any], HList]] =>
        traversalTask.flatMap { traversal =>
          val start = Instant.now()
          traversal.untyped
            .withGraph(graph)
            .toListF
            .map { values =>
              val collection: Collection[Any, ClassType[Any]] = Collection(start, Instant.now(), values.toList)
              collection.logger.debug("result count: " + values.size.toString)
              Ok(ContextedT(collection))
            }
        }.toIO
    }
  }
  def mutate: Endpoint[IO, Unit] = ???
  def ask: Endpoint[IO, Boolean] = {
    post(
      "query" :: body[Task[Traversal[ClassType[Any], ClassType[Any], HList]],
                      lspace.services.codecs.Application.JsonLD]).mapOutputAsync {
      traversalTask: Task[Traversal[ClassType[Any], ClassType[Any], HList]] =>
        traversalTask.flatMap { traversal =>
          val start = Instant.now()
          traversal.untyped
            .withGraph(graph)
            .headOptionF
            .map(_.isDefined)
            .map(Ok)
        }.toIO
    }
  }
  def subscribe: Endpoint[IO, List[Node]] = ???

  import lspace.services.codecs.Encode._
  import lspace.encode.EncodeJsonLD._

  implicit lazy val encoder: jsonld.Encoder = jsonld.Encoder(baseEncoder)

  def raw = query :+: streamingQuery

  def compiled: Endpoint.Compiled[IO] =
    Bootstrap
      .configure(enableMethodNotAllowed = true, enableUnsupportedMediaType = true)
      .serve[LApplication.JsonLD](query :+: streamingQuery)
      .compile
}