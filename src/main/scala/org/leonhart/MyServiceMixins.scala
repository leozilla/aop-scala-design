package org.leonhart

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

// to learn about mixin composition see this links
// http://jonasboner.com/aop-style-mixin-composition-stacks-in-scala/
// https://docs.scala-lang.org/tour/mixin-class-composition.html

trait MyLoggingService extends MyService {
  implicit def ec: ExecutionContext

  abstract override def handle(req: MyService.Request): Future[MyService.Response] = {
    println(s"before handle request ${req.id}")
    val f = super.handle(req)
    f.onComplete {
      case Failure(exception) => println(s"handle request failed ${req.id}")
      case Success(value) => println(s"handle request succeeded ${req.id}")
    }

    f
  }
}
