package org.leonhart.spi

import org.leonhart.ServiceX

import java.time.{Duration, Instant}
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

// provides extension points for the MyService implementation
trait MyService {
  def onServiceXRequest(request: ServiceX.Request): RequestContext = RequestContext(Instant.now())
  def onServiceXResponse(ctx: RequestContext, resp: ServiceX.Response): Unit = ()
  def onProgress(percentComplete: Double): Unit = ()

  case class RequestContext(t: Instant)
}

trait LoggingImpl extends MyService {

  override def onProgress(percentComplete: Double): Unit = println(s"LoggingImpl: request processing at $percentComplete%")
}

trait MonitoringImpl extends MyService {
  private val serviceXRequestsTotal = new AtomicInteger()
  private val serviceXResponsesTotal = new AtomicInteger()
  private val serviceXResponseTimes = new ConcurrentLinkedQueue[Duration]()

  override def onServiceXRequest(req: ServiceX.Request): RequestContext =  {
    val startTime = Instant.now() // I would use guava Stopwatch here in production code
    val rt = serviceXRequestsTotal.incrementAndGet()
    println(s"MonitoringImpl: increasing service X request count ($rt)")
    RequestContext(startTime)
  }

  override def onServiceXResponse(startTime: RequestContext, response: ServiceX.Response): Unit = {
    val endTime = Instant.now()
    val rt = serviceXResponsesTotal.incrementAndGet()
    val responseTime = java.time.Duration.between(startTime.t, endTime) // should usually not be used for this
    serviceXResponseTimes.add(responseTime)
    println(s"MonitoringImpl: recording service x response time (${responseTime})")
    println(s"MonitoringImpl: increasing service X response count ($rt)")
  }
}