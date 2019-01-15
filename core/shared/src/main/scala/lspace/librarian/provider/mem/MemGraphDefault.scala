package lspace.librarian.provider.mem

import lspace.librarian.datatype.DataType
import lspace.librarian.structure.{Ontology, Property}
import lspace.librarian.structure.util.IdProvider
import monix.execution.atomic.Atomic

object MemGraphDefault extends MemDataGraph {
  val iri: String = "memgraphdefault"

  private val _iri      = iri
  private lazy val self = this

  lazy val idProvider: IdProvider = new IdProvider {
    private val id = Atomic(1000l)
    def next: Long = id.incrementAndGet()
  }

  val ns: MemNSGraph = new MemNSGraph {
    def iri: String          = "memgraphdefault" + ".ns"
    private val _iri         = iri
    lazy val graph: MemGraph = self
    private val _thisgraph   = thisgraph
    lazy val index: MemIndexGraph = new MemIndexGraph {
      def iri: String = _iri + ".ns" + ".index"

      lazy val graph: MemGraph      = _thisgraph
      lazy val index: MemIndexGraph = this
    }

    trait Ontologies extends super.Ontologies {
      override def cached(iri: String): Option[Ontology] =
        Ontology.allOntologies.byIri
          .get(iri)
          .orElse(ns.ontologies.byIri
            .get(iri))
    }
    override val ontologies = new Ontologies {}

    trait Properties extends super.Properties {
      override def cached(iri: String): Option[Property] =
        Property.allProperties.byIri
          .get(iri)
          .orElse(properties.byIri.get(iri))
    }
    override val properties = new Properties {}

    trait Datatypes extends super.Datatypes {
      override def cached(iri: String): Option[DataType[_]] =
        DataType.allDataTypes.byIri
          .get(iri)
          .orElse(ns.datatypes.byIri.get(iri))
    }
    override val datatypes = new Datatypes {}
  }

  val index: MemIndexGraph = new MemIndexGraph {
    def iri: String = _iri + ".index"

    lazy val graph: MemGraph = self
    private val _thisgraph   = thisgraph
    lazy val index: MemIndexGraph = new MemIndexGraph {
      def iri: String = _iri + ".index" + ".index"

      lazy val graph: MemGraph      = _thisgraph
      lazy val index: MemIndexGraph = this
    }
  }

  init()
}
