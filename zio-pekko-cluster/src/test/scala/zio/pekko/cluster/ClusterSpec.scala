package zio.pekko.cluster

import scala.language.postfixOps

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.cluster.ClusterEvent.MemberLeft
import com.typesafe.config.{Config, ConfigFactory}
import zio.stream.ZStream
import zio.test.Assertion._
import zio.test._
import zio.test.TestEnvironment
import zio.test.ZIOSpecDefault
import zio._

object ClusterSpec extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment, Any] =
    suite("ClusterSpec")(
      test("receive cluster events") {
        val config: Config = ConfigFactory.parseString(s"""
                                                          |pekko {
                                                          |  actor {
                                                          |    provider = "cluster"
                                                          |  }
                                                          |  remote {
                                                          |    enabled-transports = ["pekko.remote.artery.canonical"]
                                                          |    artery.canonical {
                                                          |      hostname = "127.0.0.1"
                                                          |      port = 2554
                                                          |    }
                                                          |  }
                                                          |  cluster {
                                                          |    seed-nodes = ["pekko://Test@127.0.0.1:2554"]
                                                          |    downing-provider-class = "org.apache.pekko.cluster.sbr.SplitBrainResolverProvider"
                                                          |  }
                                                          |}
                  """.stripMargin)

        val actorSystem: ZIO[Scope, Throwable, ActorSystem] =
          ZIO.acquireRelease(ZIO.attempt(ActorSystem("Test", config)))(sys =>
            ZIO.fromFuture(_ => sys.terminate()).either
          )

        assertZIO(
          for {
            queue <- Cluster.clusterEvents()
            _     <- Clock.sleep(5 seconds)
            _     <- Cluster.leave
            items <- ZStream
                       .fromQueue(queue)
                       .takeUntil {
                         case _: MemberLeft => true
                         case _             => false
                       }
                       .runCollect
                       .timeoutFail(new Exception("Timeout"))(10 seconds)
          } yield items
        )(isNonEmpty).provideLayer(ZLayer.scoped(actorSystem))
      }
    ) @@ TestAspect.withLiveClock
}
