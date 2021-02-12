package org.leonhart

import MyService.{Request, Response}

import scala.concurrent.{ExecutionContext, Future}

class MyServiceImpl(implicit ec: ExecutionContext) extends MyService with spi.MyService {
  override def handle(req: Request): Future[Response] = {
    // do stuff here


    Future {
      for (i <- 1 until 10) {
        onProgress(i * 0.1)

        if (i == 4) {
          val xreq = ServiceX.Request(s"id$i")
          val xctx = onServiceXRequest(xreq)
          onServiceXResponse(xctx, ServiceX.Response(xreq.id, 200))
        }

        req.data match {
          case throwable: Throwable =>
            throw throwable
          case _ =>
        }

        Thread.sleep(100)
      }

      Response(req.id, 200)
    }
  }
}

trait MyService {
  def handle(req: Request): Future[Response]
}

object MyService {
  case class Request(id: String, data: Any)
  case class Response(requestId: String, status: Int)
}

object ServiceX {
  case class Request(id: String)
  case class Response(requestId: String, status: Int)
}