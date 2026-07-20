package zio.pekko.cluster.pubsub.impl

import org.apache.pekko.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import org.apache.pekko.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import zio.Exit.{Failure, Success}
import SubscriberImpl.SubscriberActor
import zio.pekko.cluster.pubsub.{MessageEnvelope, Subscriber}
import zio.{Fiber, Promise, Queue, Runtime, Task, ZIO}

private[pubsub] trait SubscriberImpl[A] extends Subscriber[A] {
  val getActorSystem: ActorSystem
  val getMediator: ActorRef

  override def listenWith(topic: String, queue: Queue[A], group: Option[String] = None): Task[Unit] =
    for {
      rts        <- Task.runtime
      subscribed <- Promise.make[Nothing, Unit]
      _          <- Task(
                      getActorSystem.actorOf(Props(new SubscriberActor[A](getMediator, topic, group, rts, queue, subscribed)))
                    )
      _          <- subscribed.await
    } yield ()
}

object SubscriberImpl {
  private[impl] class SubscriberActor[A](
      mediator: ActorRef,
      topic: String,
      group: Option[String],
      rts: Runtime[Any],
      queue: Queue[A],
      subscribed: Promise[Nothing, Unit]
  ) extends Actor {
    private var pending: Fiber[Nothing, Unit] =
      rts.unsafeRun(ZIO.unit.forkDaemon)

    mediator ! Subscribe(topic, group, self)

    def receive: PartialFunction[Any, Unit] = {
      case SubscribeAck(_)      =>
        rts.unsafeRunSync(subscribed.succeed(()))
        ()
      case MessageEnvelope(msg) =>
        val previous = pending
        pending = rts.unsafeRun(
          (
            previous.join *>
              queue.offer(msg.asInstanceOf[A]).unit.catchAllCause { cause =>
                if (cause.interrupted) ZIO.succeed(self ! PoisonPill).unit
                else ZIO.unit
              }
          ).forkDaemon
        )
    }
  }
}
