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
import com.ideal.linked.toposoid.common.CLAIM
import com.ideal.linked.toposoid.deduction.common.FacadeForAccessNeo4J.getCypherQueryResult
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

  val defaultEndPoints:Seq[Endpoint] = Seq(
    Endpoint(conf.getString("TOPOSOID_DEDUCTION_UNIT1_HOST"), port="9101"),
    Endpoint(conf.getString("TOPOSOID_DEDUCTION_UNIT2_HOST"), port="9102"),
    Endpoint(conf.getString("TOPOSOID_DEDUCTION_UNIT3_HOST"), port="9103"))
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
      targetJson = json.toString()

      val jsonStr:String = getCypherQueryResult("MATCH (n) RETURN n limit 1;", "")
      if(!jsonStr.equals("""{"records":[]}""")){

        implicit val system = ActorSystem("my-system")
        implicit val materializer = ActorMaterializer()
        implicit val executionContext = system.dispatcher

        //val in = Source(endPoints.to[scala.collection.immutable.Seq])
        val in = Source(endPoints.toSeq)
        val out = Sink.seq[String]
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
    val checkTargets = analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.sentenceType == CLAIM.index)
    val notFinished = checkTargets.filter(x => x.deductionResultMap.get(CLAIM.index.toString).get.status).size < checkTargets.size

    var queryResultJson:String = targetJson

    if(notFinished) {
      val targets:List[AnalyzedSentenceObject] = analyzedSentenceObjects.analyzedSentenceObjects
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
      //val mergeList:List[AnalyzedSentenceObject] = resultAnalyzedSentenceObjects.analyzedSentenceObjects ++ nonTargets
      queryResultJson = Json.toJson(AnalyzedSentenceObjects(resultAnalyzedSentenceObjects.analyzedSentenceObjects)).toString()
    }
    targetJson = queryResultJson
    queryResultJson

  }

}
