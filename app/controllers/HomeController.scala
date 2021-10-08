/*
 * Copyright 2021 Linked Ideal LLC.[https://linked-ideal.com/]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Flow, GraphDSL, Sink, Source}
import com.ideal.linked.common.DeploymentConverter.conf
import com.ideal.linked.toposoid.protocol.model.base.{AnalyzedSentenceObject, AnalyzedSentenceObjects}
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success}

case class Endpoint(host:String, port:String)
object Endpoint {
  implicit val jsonWrites = Json.writes[Endpoint]
  implicit val jsonReads = Json.reads[Endpoint]
}

case class ReqSelector(index:Int, function:Endpoint)
object ReqSelector {
  implicit val jsonWrites = Json.writes[ReqSelector]
  implicit val jsonReads = Json.reads[ReqSelector]
}

/**
 * This controller creates an `Action` to manage multiple deductive inference logic to register,
 * update, and delete microservices.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController with LazyLogging{

  val defaultEndPoints:Seq[Endpoint] = Seq(Endpoint(conf.getString("DEDUCTION_UNIT1_HOST"), port="9101"),  Endpoint(conf.getString("DEDUCTION_UNIT1_HOST"), port="9102"))
  var endPoints:Seq[Endpoint] = defaultEndPoints
  var targetJson:String = ""

  /**
   * This function receives the URL information of the microservice as JSON and
   * Register and update microservices that perform deductive reasoning
   * @return
   */
  def changeEndPoints()  = Action(parse.json) { request =>
    try{
      val json = request.body
      val reqSelector: ReqSelector = Json.parse(json.toString).as[ReqSelector]
      endPoints = endPoints.updated(reqSelector.index, reqSelector.function)
      Ok("""{"status":"OK"}""").as(JSON)
    }catch {
      case e: Exception => {
        logger.error(e.toString, e)
        BadRequest(Json.obj("status" -> "Error", "message" -> e.toString()))
      }
    }
  }

  /**
   * This function receives the predicate argument structure analysis result of a Japanese sentence as JSON,
   * delegates the processing to the registered microservices that perform deductive inference, and returns the result in JSON.
   * @return
   */
  def executeDeduction()  = Action(parse.json) { request =>
    try {

      val json = request.body
      val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(json.toString).as[AnalyzedSentenceObjects]

      implicit val system = ActorSystem("my-system")
      implicit val materializer = ActorMaterializer()
      implicit val executionContext = system.dispatcher

      val in = Source(endPoints.to[scala.collection.immutable.Seq])

      val out = Sink.seq[String]

      targetJson = json.toString()
      val g = RunnableGraph.fromGraph(GraphDSL.create(out) { implicit builder => o =>
        import GraphDSL.Implicits._
        val flow = builder.add(Flow[Endpoint].map(deduce(_)))
        in ~> flow ~>  o
        ClosedShape
      })
      val hoge = g.run()
      var resultJson = ""
      hoge.onComplete {
        case Success(js) =>
          println(s"Success: $js")
          println("---------------------------------------------------")
          resultJson = js.last
        case Failure(e) =>
          println(s"Failure: $e")
      }
      while(!hoge.isCompleted){
        Thread.sleep(20)
      }

      print("check")


      Ok(targetJson).as(JSON)

    }catch {
      case e: Exception => {
        logger.error(e.toString, e)
        BadRequest(Json.obj("status" -> "Error", "message" -> e.toString()))
      }
    }
  }

  /**
   *　This function delegates processing to a registered group of microservices that perform deductive inference.
   * @param endpoint
   * @return
   */
  private def deduce(endpoint:Endpoint): String ={

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()

    val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(targetJson.toString).as[AnalyzedSentenceObjects]
    //既に真になっているものは解析対象外
    val nonTargets:List[AnalyzedSentenceObject] = analyzedSentenceObjects.analyzedSentenceObjects.filter((x:AnalyzedSentenceObject) => x.deductionResultMap.get(x.sentenceType.toString).get.status)

    //何も処理をしなかったようにまずは、inputのjsonをセット
    var queryResultJson:String = targetJson

    //処理対象が存在する場合のみAPIに問い合わせる
    val targets:List[AnalyzedSentenceObject] = analyzedSentenceObjects.analyzedSentenceObjects.filterNot((x:AnalyzedSentenceObject) => x.deductionResultMap.get(x.sentenceType.toString).get.status)

    if(targets.size != 0) {
      val entity = HttpEntity(ContentTypes.`application/json`, Json.toJson(AnalyzedSentenceObjects(targets)).toString())
      val req = HttpRequest(uri = "http://" + endpoint.host + ":" + endpoint.port + "/execute", method = HttpMethods.POST, entity = entity)
      val result = Http().singleRequest(req)
        .flatMap { res =>
          Unmarshal(res).to[String].map { data =>
            Json.parse(data.getBytes("UTF-8"))
          }
        }
      result.onComplete {
        case Success(js) =>
          println(s"Success: $js")
          queryResultJson = s"$js"
        case Failure(e) =>
          println(s"Failure: $e")
      }
      while(!result.isCompleted){
        Thread.sleep(20)
      }

      val resultAnalyzedSentenceObjects = Json.parse(queryResultJson).as[AnalyzedSentenceObjects]
      val mergeList:List[AnalyzedSentenceObject] = resultAnalyzedSentenceObjects.analyzedSentenceObjects ++ nonTargets
      queryResultJson = Json.toJson(AnalyzedSentenceObjects(mergeList)).toString()
    }
    targetJson = queryResultJson
    queryResultJson

  }

}
