/*
 * Copyright (C) 2025  Linked Ideal LLC.[https://linked-ideal.com/]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl._
import akka.stream.ActorMaterializer
import com.ideal.linked.common.DeploymentConverter.conf
import com.ideal.linked.toposoid.common.InMemoryDbUtils.setEndPoints
import com.ideal.linked.toposoid.common.{CLAIM, InMemoryDbUtils, PREMISE, TRANSVERSAL_STATE, ToposoidUtils, TransversalState}
import com.ideal.linked.toposoid.deduction.common.FacadeForAccessNeo4J.getCypherQueryResult
import com.ideal.linked.toposoid.protocol.model.base.{AnalyzedSentenceObject, AnalyzedSentenceObjects}
import com.ideal.linked.toposoid.protocol.model.frontend.Endpoint
import com.ideal.linked.toposoid.protocol.model.redis.KeyValueStoreInfo
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success}


/**
 * This controller creates an `Action` to manage multiple deductive inference logic to register,
 * update, and delete microservices.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController with LazyLogging{

  final val NO_HOST = "-"
  final val NO_PORT = "-"
  final val NO_NAME = "-"

  /**
   * This function receives the predicate argument structure analysis result of a Japanese sentence as JSON,
   * delegates the processing to the registered microservices that perform deductive inference, and returns the result in JSON.
   * @return
   */
  def executeDeduction()  = Action(parse.json) { request =>
    val transversalState = Json.parse(request.headers.get(TRANSVERSAL_STATE .str).get).as[TransversalState]
    try {
      val json = request.body

      //TODO: REDISから情報を取得
      val currentEndPoints = InMemoryDbUtils.getEndPoints(transversalState)

      logger.info(currentEndPoints.toString())
      val jsonStr:String = getCypherQueryResult("MATCH (n) RETURN n limit 1;", "", transversalState)
      if(jsonStr.equals("""{"records":[]}""")) Ok(json.toString()).as(JSON)
      val result = deduce(0, json.toString(), json.toString(), currentEndPoints, transversalState)
      logger.info(ToposoidUtils.formatMessageForLogger("All deduction units have been completed.", transversalState.userId))
      Ok(result._3).as(JSON)

    }catch {
      case e: Exception => {
        logger.error(ToposoidUtils.formatMessageForLogger(e.toString, transversalState.userId), e)
        BadRequest(Json.obj("status" -> "Error", "message" -> e.toString()))
      }
    }
  }

  /**
   * This function receives the URL information of the microservice as JSON and
   * Register and update microservices that perform deductive reasoning
   *
   * @return
   */
  def changeEndPoints() = Action(parse.json) { request =>
    val transversalState = Json.parse(request.headers.get(TRANSVERSAL_STATE.str).get).as[TransversalState]
    try {
      val json = request.body
      val endPoints: Seq[Endpoint] = Json.parse(json.toString).as[Seq[Endpoint]]
      val updatedEndPoints: Seq[Endpoint] = setEndPoints(endPoints, transversalState)
      logger.info(ToposoidUtils.formatMessageForLogger("Changing End-Points completed." + updatedEndPoints.toString(), transversalState.userId))
      Ok("""{"status":"OK"}""").as(JSON)
    } catch {
      case e: Exception => {
        logger.error(ToposoidUtils.formatMessageForLogger(e.toString, transversalState.userId), e)
        BadRequest(Json.obj("status" -> "Error", "message" -> e.toString()))
      }
    }
  }


  private def deduce(index:Int, targetJson:String, resultJson:String, endPoints:Seq[Endpoint], transversalState:TransversalState): (Int, String, String) ={

    val asosJson = execute(endPoints(index), targetJson, resultJson, transversalState)
    if(index == endPoints.size -1){
      (index, asosJson._1, asosJson._2)
    }else{
      deduce(index + 1, asosJson._1, asosJson._2, endPoints, transversalState)
    }
  }

  /**
   *　This function delegates processing to a registered group of microservices that perform deductive inference.
   * @param endpoint
   * @return
   */

  private def execute(endpoint:Endpoint, targetJson:String, resultJson:String, transversalState:TransversalState): (String, String) ={

    if(endpoint.host.equals(NO_HOST) || endpoint.port.equals(NO_PORT) || endpoint.name.equals(NO_NAME)) return (targetJson, resultJson)
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(targetJson).as[AnalyzedSentenceObjects]
    val hasPremise = analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType == PREMISE.index).size > 0
    //If the proposition has premise, the truth of the claim is determined along with the truth of havePremiseInGivenProposition.
    val checkTargets = hasPremise match  {
      case true => analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType == CLAIM.index && x.deductionResult.havePremiseInGivenProposition)
      case _ => analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType == CLAIM.index)
    }
    val notFinished = checkTargets.filterNot(x => x.deductionResult.status)

    if(notFinished.size > 0) {
      val targets:List[AnalyzedSentenceObject] = notFinished
      val entity = HttpEntity(ContentTypes.`application/json`, Json.toJson(AnalyzedSentenceObjects(targets)).toString())
      val req = HttpRequest(uri = "http://" + endpoint.host + ":" + endpoint.port + "/execute", method = HttpMethods.POST, entity = entity)
                  .withHeaders(RawHeader(TRANSVERSAL_STATE.str, Json.toJson(transversalState).toString()))
      val result = Http().singleRequest(req)
        .flatMap { res =>
          Unmarshal(res).to[String].map { data =>
            Json.parse(data.getBytes("UTF-8"))
          }
        }
      result.onComplete {
        case Success(js) =>
          logger.debug(js.toString())
        case Failure(e) =>
          logger.error(ToposoidUtils.formatMessageForLogger(e.toString, transversalState.userId), e)
      }
      while(!result.isCompleted){
        Thread.sleep(20)
      }
      getResultJson(result.value.get.get.toString(), resultJson)
    }else{
      getResultJson(targetJson, resultJson)
    }
  }

  private def getResultJson(targetJson:String, resultJson:String):(String,String) ={
    val targetAsos = Json.parse(targetJson).as[AnalyzedSentenceObjects].analyzedSentenceObjects
    val resultAsos = Json.parse(resultJson).as[AnalyzedSentenceObjects].analyzedSentenceObjects

    val asos = resultAsos.foldLeft(List.empty[AnalyzedSentenceObject]){
      (acc, x) => {
        val selfAsos = targetAsos.filter(_.knowledgeBaseSemiGlobalNode.sentenceId.equals(x.knowledgeBaseSemiGlobalNode.sentenceId))
        val aso = selfAsos.size match {
          case 0 => x
          case _ => AnalyzedSentenceObject(x.nodeMap, x.edgeList, x.knowledgeBaseSemiGlobalNode, selfAsos.head.deductionResult)
        }
        acc :+ aso
      }
    }
    val updateResultJson = Json.toJson(AnalyzedSentenceObjects(asos)).toString()
    (targetJson, updateResultJson)
  }
}

