package org.leonhart

import org.leonhart.MyService.Response

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

// interceptors are usually used in java to accomplish the same as mixin composition does in scala
// but they can still be used in scala, they are a little less powerfull but more explicit

class MyLoggingServiceInterceptor(next: MyService)(implicit ec: ExecutionContext) extends MyService {

  override def handle(req: MyService.Request): Future[MyService.Response] = {
    println(s"MyLoggingServiceInterceptor: before handle request ${req.id}")
    val f = next.handle(req)
    f.onComplete {
      case Failure(exception) => println(s"MyLoggingServiceInterceptor: handle request failed ${req.id}")
      case Success(value) => println(s"MyLoggingServiceInterceptor: handle request succeeded ${req.id}")
    }

    f
  }
}


class MyRateLimitingServiceInterceptor(next: MyService)(implicit ec: ExecutionContext) extends MyService {

  override def handle(req: MyService.Request): Future[MyService.Response] = {
    val isRateLimitExceeded = false
    if (isRateLimitExceeded) {
      Future.successful(Response(req.id, 503))
    } else  next.handle(req)
  }
}