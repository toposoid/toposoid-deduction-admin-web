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
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer}
import com.ideal.linked.common.DeploymentConverter.conf
import com.ideal.linked.toposoid.common.{CLAIM, PREMISE, TRANSVERSAL_STATE, ToposoidUtils, TransversalState}
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

  final val NO_HOST = "-"
  final val NO_PORT = "-"

  private val defaultEndPoints:Seq[Endpoint] = Seq(
    Endpoint(conf.getString("TOPOSOID_DEDUCTION_UNIT1_HOST"), port = conf.getString("TOPOSOID_DEDUCTION_UNIT1_PORT")),
    Endpoint(conf.getString("TOPOSOID_DEDUCTION_UNIT2_HOST"), port = conf.getString("TOPOSOID_DEDUCTION_UNIT2_PORT")),
    Endpoint(conf.getString("TOPOSOID_DEDUCTION_UNIT3_HOST"), port = conf.getString("TOPOSOID_DEDUCTION_UNIT3_PORT")),
    Endpoint(conf.getString("TOPOSOID_DEDUCTION_UNIT4_HOST"), port = conf.getString("TOPOSOID_DEDUCTION_UNIT4_PORT")),
    Endpoint(conf.getString("TOPOSOID_DEDUCTION_UNIT5_HOST"), port = conf.getString("TOPOSOID_DEDUCTION_UNIT5_PORT")))
  private var endPoints:Seq[Endpoint] = defaultEndPoints
  //private var targetJson:String = ""

  /**
   * This function receives the URL information of the microservice as JSON and
   * Register and update microservices that perform deductive reasoning
   * @return
   */
  def changeEndPoints()  = Action(parse.json) { request =>
    val transversalState = Json.parse(request.headers.get(TRANSVERSAL_STATE .str).get).as[TransversalState]
    try{
      val json = request.body
      val reqSelector: ReqSelector = Json.parse(json.toString).as[ReqSelector]
      endPoints = endPoints.updated(reqSelector.index, reqSelector.function)
      logger.info(ToposoidUtils.formatMessageForLogger("Changing End-Points completed." + endPoints.toString(), transversalState.username))
      Ok("""{"status":"OK"}""").as(JSON)
    }catch {
      case e: Exception => {
        logger.error(ToposoidUtils.formatMessageForLogger(e.toString, transversalState.username), e)
        BadRequest(Json.obj("status" -> "Error", "message" -> e.toString()))
      }
    }
  }

  def getEndPoints() = Action(parse.json) { request =>
    val transversalState = Json.parse(request.headers.get(TRANSVERSAL_STATE .str).get).as[TransversalState]
    try {
      Ok(Json.toJson(endPoints)).as(JSON)
    } catch {
      case e: Exception => {
        logger.error(ToposoidUtils.formatMessageForLogger(e.toString, transversalState.username), e)
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
    val transversalState = Json.parse(request.headers.get(TRANSVERSAL_STATE .str).get).as[TransversalState]
    try {
      val json = request.body
      logger.info(endPoints.toString())
      val jsonStr:String = getCypherQueryResult("MATCH (n) RETURN n limit 1;", "", transversalState)
      if(jsonStr.equals("""{"records":[]}""")) Ok(json.toString()).as(JSON)
      val result = deduce(0, json.toString(), json.toString(), transversalState)
      logger.info(ToposoidUtils.formatMessageForLogger("All deduction units have been completed.", transversalState.username))
      Ok(result._3).as(JSON)

    }catch {
      case e: Exception => {
        logger.error(ToposoidUtils.formatMessageForLogger(e.toString, transversalState.username), e)
        BadRequest(Json.obj("status" -> "Error", "message" -> e.toString()))
      }
    }
  }

  private def deduce(index:Int, targetJson:String, resultJson:String, transversalState:TransversalState): (Int, String, String) ={
    val asosJson = execute(endPoints(index), targetJson, resultJson, transversalState)
    if(index == endPoints.size -1){
      (index, asosJson._1, asosJson._2)
    }else{
      deduce(index + 1, asosJson._1, asosJson._2, transversalState)
    }
  }

  /**
   *　This function delegates processing to a registered group of microservices that perform deductive inference.
   * @param endpoint
   * @return
   */

  private def execute(endpoint:Endpoint, targetJson:String, resultJson:String, transversalState:TransversalState): (String, String) ={

    if(endpoint.host.equals(NO_HOST) || endpoint.port.equals(NO_PORT)) return (targetJson, resultJson)
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
          logger.error(ToposoidUtils.formatMessageForLogger(e.toString, transversalState.username), e)
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

