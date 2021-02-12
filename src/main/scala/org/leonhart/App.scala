package org.leonhart

import MyService.{Request, Response}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Awaitable, ExecutionContext, Future}

object MyApplication extends App {
  implicit val ec = ExecutionContext.global

  val simple = new MyServiceImpl()
  val intercepted = new MyLoggingServiceInterceptor(new MyRateLimitingServiceInterceptor(new MyServiceImpl()))
  val stacked1 = new MyServiceImpl() with spi.LoggingImpl with spi.MonitoringImpl
  val stacked2 = new MyServiceImpl() with spi.MonitoringImpl with spi.LoggingImpl
  val interceptedAndStacked = new MyLoggingServiceInterceptor(new MyRateLimitingServiceInterceptor(stacked1))

  // awaitIt(run(simple))
  // awaitIt(run(intercepted))
  awaitIt(run(stacked1))
  // awaitIt(run(interceptedAndStacked))

  def simpleRun: Future[Response] = {
    simple.handle(Request("1", ""))
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
