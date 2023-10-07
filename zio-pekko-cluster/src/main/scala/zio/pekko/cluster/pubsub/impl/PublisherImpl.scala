package zio.pekko.cluster.pubsub.impl

import org.apache.pekko.actor.ActorRef
import org.apache.pekko.cluster.pubsub.DistributedPubSubMediator.Publish
import zio.pekko.cluster.pubsub.{MessageEnvelope, Publisher}
import zio.{ Task, ZIO }

private[pubsub] trait PublisherImpl[A] extends Publisher[A] {
  val getMediator: ActorRef

  override def publish(topic: String, data: A, sendOneMessageToEachGroup: Boolean = false): Task[Unit] =
    ZIO.attempt(getMediator ! Publish(topic, MessageEnvelope(data), sendOneMessageToEachGroup))
}
