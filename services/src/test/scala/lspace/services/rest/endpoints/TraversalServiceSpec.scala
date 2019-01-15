package lspace.services.rest.endpoints

import argonaut.Parse
import com.twitter.finagle
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Duration
import io.finch.{Bootstrap, Input}
import lspace.librarian.process.traversal.{Collection, P}
import lspace.librarian.provider.mem.{MemGraph, MemGraphDefault}
import lspace.librarian.structure.{Graph, Node}
import lspace.librarian.util.SampleGraph
import lspace.parse.json.JsonLD
import lspace.server.util
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import shapeless.{:+:, CNil}

class TraversalServiceSpec extends WordSpec with Matchers with BeforeAndAfterAll {

  val graph             = MemGraph("GraphServiceSpec")
  val jsonld            = JsonLD(graph)
  lazy val graphService = TraversalService(graph)(jsonld)

  override def beforeAll(): Unit = {
    SampleGraph.loadSocial(graph)
  }

  override def afterAll(): Unit = {
    graph.close()
  }

  import util._
  "a traversal-service" should {
    "execute a traversal only on a POST request" in {
      val traversal = MemGraphDefault.g.N.has(SampleGraph.properties.balance, P.gt(300)).count
      import JsonLDModule.Encode._
      val input = Input
        .post("/traverse")
        .withBody[JsonLDModule.JsonLD](traversal.toNode)
        .withHeaders("Accept" -> "text/plain")
      graphService.traverse(input).awaitOutput().map { output =>
        output.isRight shouldBe true
        val collection = output.right.get.value
        collection.item shouldBe List(2)
      }
    }
    "get all labels" in {
      val input = Input
        .get("/label")
      val res = graphService.getLabels(input).awaitOutput().map { output =>
        output.isRight shouldBe true
        val collection = output.right.get.value
        println(collection.item.size)
        collection.item.nonEmpty shouldBe true
      }
    }
  }
}
