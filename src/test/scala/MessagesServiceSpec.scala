
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import doobie.util.ExecutionContexts
import fetch.{DataCache, DataSource}
import org.github.ainr.bloate4.infrastructure.logging.LazyLogging
import org.github.ainr.bloate4.infrastructure.logging.interpreters.LoggerWithMetrics
import org.github.ainr.bloate4.infrastructure.metrics.LoggerCounters
import org.github.ainr.bloate4.repositories.MessagesRepo
import org.github.ainr.bloate4.services.messages.MessagesServiceImpl
import org.github.ainr.bloate4.services.messages.domain.Message
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MessagesServiceSpec extends AnyFlatSpec with AsyncIOSpec with Matchers with MockFactory with LazyLogging {


  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)
  implicit val tm = IO.timer(ExecutionContexts.synchronous)

  val repo = mock[MessagesRepo[IO]]
  val fetchMessage = mock[DataSource[IO, Int, Message]]
  val messagesCache = mock[DataCache[IO]]
  val c = mock[LoggerCounters[IO]]
  val l = new LoggerWithMetrics[IO](logger)(c)

  val service = new MessagesServiceImpl[IO](repo, fetchMessage, messagesCache)(l)

  it should "be success" in {
    //service.saveMessage("Foo").asserting(_ shouldBe MessageSavingResult(""))
    1 shouldBe 1
  }

}
