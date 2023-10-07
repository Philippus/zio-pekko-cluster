package zio.pekko.cluster.pubsub

case class MessageEnvelope[Msg](msg: Msg)
