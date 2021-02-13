package org.leonhart

import MyService.{Request, Response}

import org.leonhart.spi.{ContextWithStartTime, ContextWithTraceId, NoOpImpl}

import java.time.Instant
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Awaitable, ExecutionContext, Future}

object MyApplication extends App {
  implicit val ec = ExecutionContext.global

  class StackedRequestContext extends ContextWithStartTime with ContextWithTraceId {
    var startTime: Instant  = Instant.MIN
    var traceId: String = ""
  }

  val service = new MyServiceImpl()(implicitly[ExecutionContext], new NoOpImpl {
    override type Ctx = Unit
    override def context: Unit = ()
  })
  val intercepted = new MyLoggingServiceInterceptor(new MyRateLimitingServiceInterceptor(service))
  val stacked1SPI = new spi.LoggingImpl with spi.MonitoringImpl[StackedRequestContext] with spi.TracingImpl[StackedRequestContext] {
    override type Ctx = StackedRequestContext
    override def context: StackedRequestContext = new StackedRequestContext
  }
  val stacked2SPI = new spi.MonitoringImpl[StackedRequestContext] with spi.LoggingImpl {
    override type Ctx = StackedRequestContext
    override def context: StackedRequestContext = new StackedRequestContext
  }
  val stacked1Service = new MyServiceImpl()(implicitly[ExecutionContext], stacked1SPI)
  val stacked2Service = new MyServiceImpl()(implicitly[ExecutionContext], stacked1SPI)
  val interceptedAndStacked = new MyLoggingServiceInterceptor(new MyRateLimitingServiceInterceptor(stacked1Service))

  // awaitIt(run(service))
  // awaitIt(run(intercepted))
  awaitIt(run(stacked1Service))
  awaitIt(run(stacked2Service))
  // awaitIt(run(interceptedAndStacked))

  def simpleRun(srv: MyService): Future[Response] = {
    srv.handle(Request("1", ""))
  }

  def run(s: MyService): Future[Response] = {
    s.handle(Request("1", ""))
    s.handle(Request("2", ""))
    // s.handle(Request("3", new IllegalStateException("illegal 3")))
  }

  def awaitIt[T](f: Awaitable[T]): Unit = {
    Await.result(f, 7.seconds)
  }
}
