package zio.pekko.cluster.pubsub.impl

import org.apache.pekko.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import org.apache.pekko.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import SubscriberImpl.SubscriberActor
import zio.pekko.cluster.pubsub.{MessageEnvelope, Subscriber}
import zio.{Exit, Fiber, Promise, Queue, Runtime, Task, Unsafe, ZIO}

private[pubsub] trait SubscriberImpl[A] extends Subscriber[A] {
  val getActorSystem: ActorSystem
  val getMediator: ActorRef

  override def listenWith(topic: String, queue: Queue[A], group: Option[String] = None): Task[Unit] =
    for {
      rts        <- ZIO.runtime[Any]
      subscribed <- Promise.make[Nothing, Unit]
      _          <- ZIO.attempt(
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
    private var pending: Fiber.Runtime[Nothing, Unit] =
      Unsafe.unsafe { implicit u =>
        rts.unsafe.fork(ZIO.unit)
      }

    mediator ! Subscribe(topic, group, self)

    def receive: Actor.Receive = {
      case SubscribeAck(_)      =>
        Unsafe.unsafe { implicit u =>
          rts.unsafe.run(subscribed.succeed(())).getOrThrow()
        }
        ()
      case MessageEnvelope(msg) =>
        Unsafe.unsafe { implicit u =>
          val previous = pending
          val next     = rts.unsafe.fork(previous.join *> queue.offer(msg.asInstanceOf[A]).unit)
          next.unsafe.addObserver {
            case Exit.Success(_) => ()
            case Exit.Failure(c) => if (c.isInterrupted) self ! PoisonPill // stop listening if the queue was shut down
          }
          pending = next
        }
        ()
    }
  }
}
