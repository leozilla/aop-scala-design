package org.leonhart.spi

import org.leonhart.ServiceX

import java.time.{Duration, Instant}
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

// provides extension points for the MyService implementation
trait MyService {
  type Ctx

  def context: Ctx

  def onServiceXRequest(ctx: Ctx, request: ServiceX.Request): Unit = ()
  def onServiceXResponse(ctx: Ctx, resp: ServiceX.Response): Unit = ()
  def onProgress(percentComplete: Double): Unit = ()
}

trait NoOpImpl extends MyService {

  override def onServiceXRequest(ctx: Ctx, request: ServiceX.Request): Unit = {
    super.onServiceXRequest(ctx, request)
  }

  override def onServiceXResponse(ctx: Ctx, resp: ServiceX.Response): Unit = {
    super.onServiceXResponse(ctx, resp)
  }

  override def onProgress(percentComplete: Double): Unit = {
    super.onProgress(percentComplete)
  }
}

trait LoggingImpl extends MyService {

  abstract override def onServiceXRequest(ctx: Ctx, request: ServiceX.Request): Unit = {
    super.onServiceXRequest(ctx, request)
  }

  abstract override def onServiceXResponse(ctx: Ctx, resp: ServiceX.Response): Unit = {
    super.onServiceXResponse(ctx, resp)
  }

  abstract override def onProgress(percentComplete: Double): Unit = {
    super.onProgress(percentComplete)
    println(s"LoggingImpl: request processing at $percentComplete%")
  }
}

trait ContextWithStartTime {
  var startTime: Instant
}

trait MonitoringImpl[C <: ContextWithStartTime] extends MyService {
  private val serviceXRequestsTotal = new AtomicInteger()
  private val serviceXResponsesTotal = new AtomicInteger()
  private val serviceXResponseTimes = new ConcurrentLinkedQueue[Duration]()

  override type Ctx = C

  abstract override def onServiceXRequest(ctx: Ctx, req: ServiceX.Request): Unit =  {
    super.onServiceXRequest(ctx, req)
    val startTime = Instant.now() // I would use guava Stopwatch here in production code
    val rt = serviceXRequestsTotal.incrementAndGet()
    println(s"MonitoringImpl: increasing service X request count ($rt)")
    ctx.startTime = startTime
  }

  abstract override def onServiceXResponse(ctx: Ctx, response: ServiceX.Response): Unit = {
    super.onServiceXResponse(ctx, response)
    val endTime = Instant.now()
    val rt = serviceXResponsesTotal.incrementAndGet()
    val responseTime = java.time.Duration.between(ctx.asInstanceOf[ContextWithStartTime].startTime, endTime) // should usually not be used for this
    serviceXResponseTimes.add(responseTime)
    println(s"MonitoringImpl: recording service x response time (${responseTime})")
    println(s"MonitoringImpl: increasing service X response count ($rt)")
  }

  abstract override def onProgress(percentComplete: Double): Unit = {
    super.onProgress(percentComplete)
  }
}

trait ContextWithTraceId {
  var traceId: String
}

trait TracingImpl[C <: ContextWithTraceId] extends MyService {
  private val traceIdGen = new AtomicInteger()

  override type Ctx = C

  abstract override def onServiceXRequest(ctx: Ctx, req: ServiceX.Request): Unit =  {
    super.onServiceXRequest(ctx, req)
    val traceId = traceIdGen.incrementAndGet().toString
    println(s"TracingImpl: start tracing service x span (${traceId})")
    ctx.traceId = traceId
  }

  abstract override def onServiceXResponse(ctx: Ctx, response: ServiceX.Response): Unit = {
    super.onServiceXResponse(ctx, response)
    val traceId = ctx.asInstanceOf[ContextWithTraceId].traceId
    println(s"TracingImpl: complete tracing service x span (${traceId})")
  }

  abstract override def onProgress(percentComplete: Double): Unit = {
    super.onProgress(percentComplete)
  }
}