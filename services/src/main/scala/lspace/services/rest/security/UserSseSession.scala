package lspace.services.rest.security

import java.time.{Instant, LocalDateTime, ZoneId}

import lspace.client.{Client, User}
import lspace.client.session.{ClientSession, OpenSession, UserSession}
import lspace.librarian.provider.detached.DetachedGraph
import lspace.librarian.structure.Property

object UserSseSession {
  def apply(
      iri: String,
      client: Client,
      user: User,
      expiration: Instant = LocalDateTime.now.plusHours(4).atZone(ZoneId.systemDefault).toInstant): UserSseSession = {
    val node = DetachedGraph.nodes.create(UserSession.ontology)
    node.addOut(Property.default.typed.iriUrlString, iri)
    node.addOut(OpenSession.keys.expirationDate, expiration)
    node.addOut(ClientSession.keys.clientClient, client)
    node.addOut(UserSession.keys.userUser, user)

    new UserSseSession(new UserSession(node) {})
  }
}

class UserSseSession(override val session: UserSession) extends OpenSseSession(session)