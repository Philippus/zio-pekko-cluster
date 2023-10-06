package zio.pekko.cluster

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.cluster.ClusterEvent.MemberLeft
import com.typesafe.config.{ Config, ConfigFactory }
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment
import zio.{ Managed, Task, ZLayer }

object ClusterSpec extends DefaultRunnableSpec {

  def spec: ZSpec[TestEnvironment, Any] =
    suite("ClusterSpec")(
      testM("receive cluster events") {
        val config: Config = ConfigFactory.parseString(s"""
                                                          |pekko {
                                                          |  actor {
                                                          |    provider = "cluster"
                                                          |  }
                                                          |  remote.artery.enabled = false
                                                          |  remote.classic {
                                                          |    enabled-transports = ["pekko.remote.classic.netty.tcp"]
                                                          |    netty.tcp {
                                                          |      hostname = "127.0.0.1"
                                                          |      port = 7355
                                                          |    }
                                                          |  }
                                                          |  cluster {
                                                          |    seed-nodes = ["pekko.tcp://Test@127.0.0.1:7355"]
                                                          |  }
                                                          |}
                  """.stripMargin)

        val actorSystem: Managed[Throwable, ActorSystem] =
          Managed.make(Task(ActorSystem("Test", config)))(sys => Task.fromFuture(_ => sys.terminate()).either)

        assertM(
          for {
            queue <- Cluster.clusterEvents()
            _     <- Cluster.leave
            item  <- queue.take
          } yield item
        )(isSubtype[MemberLeft](anything)).provideLayer(ZLayer.fromManaged(actorSystem))
      }
    )
}
