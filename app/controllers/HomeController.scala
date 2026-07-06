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
import com.ideal.linked.common.DeploymentConverter.conf
import com.ideal.linked.toposoid.common.{SentenceType, InMemoryDbUtils, TRANSVERSAL_STATE, ToposoidUtils, TransversalState}
import com.ideal.linked.toposoid.protocol.model.base.{AnalyzedSentenceObject, AnalyzedSentenceObjects}
import com.ideal.linked.toposoid.protocol.model.frontend.Endpoint
import com.ideal.linked.toposoid.protocol.model.redis.KeyValueStoreInfo
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.JsValue

import scala.util.{Failure, Success}
import com.ideal.linked.toposoid.common.Neo4JUtilsImpl
import com.ideal.linked.toposoid.protocol.model.base.DeductionConfiguration
import com.ideal.linked.toposoid.protocol.model.base.DeductionResult


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
   * This function receives the URL information of the microservice as JSON and
   * Register and update microservices that perform deductive reasoning
   *
   * @return
   */
  def changeEndPoints():Action[JsValue] = Action(parse.json[JsValue]) { request =>
    val transversalState = Json.parse(request.headers.get(TRANSVERSAL_STATE.str).get).as[TransversalState]
    try {
      val json = request.body
      val endPoints: Seq[Endpoint] = Json.parse(json.toString).as[Seq[Endpoint]]
      val updatedEndPoints: Seq[Endpoint] = InMemoryDbUtils.setDeductionGroupEndPoints(endPoints, transversalState)
      logger.info(ToposoidUtils.formatMessageForLogger("Changing End-Points completed." + updatedEndPoints.toString(), transversalState.userId))
      Ok("""{"status":"OK"}""").as(JSON)
    } catch {
      case e: Exception => {
        logger.error(ToposoidUtils.formatMessageForLogger(e.toString, transversalState.userId), e)
        BadRequest(Json.obj("status" -> "Error", "message" -> e.toString()))
      }
    }
  }

  /**
   * This function receives the predicate argument structure analysis result of a Japanese sentence as JSON,
   * delegates the processing to the registered microservices that perform deductive inference, and returns the result in JSON.
   * @return
   */
  def executeDeduction():Action[JsValue] = Action(parse.json[JsValue]) { request =>
    val transversalState = Json.parse(request.headers.get(TRANSVERSAL_STATE .str).get).as[TransversalState]
    try {
      val json = request.body
      val currentEndPoints = InMemoryDbUtils.getDeductionGroupEndPoints(transversalState)
      logger.info(currentEndPoints.toString())
      val jsonStr:String = Neo4JUtilsImpl().getCypherQueryResult("MATCH (n) RETURN n limit 1;", "", transversalState)
      if(jsonStr.equals("""{"records":[]}""")){
        Ok(json.toString()).as(JSON)
      }else{
        val result = deduce(json.toString(), currentEndPoints, transversalState)
        logger.info(ToposoidUtils.formatMessageForLogger("All deduction units have been completed.", transversalState.userId))
        Ok(result).as(JSON)
      }
    
    }catch {
      case e: Exception => {
        logger.error(ToposoidUtils.formatMessageForLogger(e.toString, transversalState.userId), e)
        BadRequest(Json.obj("status" -> "Error", "message" -> e.toString()))
      }
    }
  }

  //evidenceKnowledgeListとdeductionPhaseType以外は、元に戻す。
  private def updateAnalyzedSentenceObjects(orgAsos:AnalyzedSentenceObjects, deducedAsos:AnalyzedSentenceObjects):AnalyzedSentenceObjects = {
    val asoList = deducedAsos.analyzedSentenceObjects.zip(orgAsos.analyzedSentenceObjects).map((x,y) => {
      val updatedDeductionResult = DeductionResult(
        y.deductionResult.status, 
        y.deductionResult.authenticityType,
        y.deductionResult.coveredPropositionEdges,
        x.deductionResult.evidenceKnowledgeList,
        y.deductionResult.havePremiseInGivenProposition,
        x.deductionResult.deductionPhaseType
      )
      AnalyzedSentenceObject(y.nodeMap, y.edgeList, y.knowledgeBaseSemiGlobalNode,  updatedDeductionResult)
    })
    AnalyzedSentenceObjects(asoList, orgAsos.deductionConfiguration)
  }


  private def deduce(json:String, endPoints:Seq[Endpoint], transversalState:TransversalState):String = {

    val orgAsos: AnalyzedSentenceObjects = Json.parse(json).as[AnalyzedSentenceObjects]
    
    val deducedAsosMap:List[(String, AnalyzedSentenceObjects)] = endPoints.foldLeft(List(("-" -> orgAsos)))(
      (acc, x) => {   
        val targetAsos:AnalyzedSentenceObjects = updateAnalyzedSentenceObjects(orgAsos, acc.last._2)     
        val resultAsos:AnalyzedSentenceObjects = execute(x, targetAsos, transversalState) 
        acc :+ (x.name -> resultAsos)
    })

    //各グループでstatusがTrueかつ最後のPhaseのものを残す。statusが全てFalseの場合は、最後のPhaseのものを返す。
    val updatedAsos = deducedAsosMap.foldLeft(List.empty[AnalyzedSentenceObject]){
      (acc, x) => {
        x._1 match {
          case "-" =>  x._2.analyzedSentenceObjects
          case _ => {
            acc.zip(x._2.analyzedSentenceObjects).map((y,z) => {     
              z.deductionResult.status match {
                case true => z
                case false => y
              }
            })             
          } 
        }
      }
    }
    Json.toJson(AnalyzedSentenceObjects(updatedAsos, orgAsos.deductionConfiguration)).toString
  }

  private def execute(endpoint:Endpoint, analyzedSentenceObjects:AnalyzedSentenceObjects, transversalState:TransversalState):AnalyzedSentenceObjects ={

    if(endpoint.host.equals(NO_HOST) || endpoint.port.equals(NO_PORT) || endpoint.name.equals(NO_NAME)) return analyzedSentenceObjects
    
    val deducitonConfig = analyzedSentenceObjects.deductionConfiguration
    val hasPremise = analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType == SentenceType.PREMISE.index).size > 0
    //If the proposition has premise, the truth of the claim is determined along with the truth of havePremiseInGivenProposition.
    val targets = hasPremise match  {
      case true => analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType == SentenceType.CLAIM.index && x.deductionResult.havePremiseInGivenProposition)
      case _ => analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType == SentenceType.CLAIM.index)
    }    
    val json = Json.toJson(AnalyzedSentenceObjects(targets, deducitonConfig)).toString()

    if(targets.size > 0) {
      val result = ToposoidUtils.callComponent(
            json,
            endpoint.host,
            endpoint.port,
            "execute",
            transversalState)
      Json.parse(result).as[AnalyzedSentenceObjects]
    }else{
      analyzedSentenceObjects
    }
  }

  /*
  private def getResultJson(json:String):List[AnalyzedSentenceObject] ={
    
    val resultAsos = Json.parse(json).as[AnalyzedSentenceObjects].analyzedSentenceObjects
    resultAsos.foldLeft(List.empty[AnalyzedSentenceObject]){
      (acc, x) => {
        val selfAsos = targetAsos.filter(_.knowledgeBaseSemiGlobalNode.sentenceId.equals(x.knowledgeBaseSemiGlobalNode.sentenceId))
        val aso = selfAsos.size match {
          case 0 => x
          case _ => {
            //evidenceは、引き継ぎ、coveredEdgesは、引き継がない？
            AnalyzedSentenceObject(x.nodeMap, x.edgeList, x.knowledgeBaseSemiGlobalNode, selfAsos.head.deductionResult)

          }
        }
        acc :+ aso
      }
    }
  }
  */

  /*
  private def getResultJson(targetJson:String, resultJson:String, deducitonConfig:DeductionConfiguration):(String,String) ={
    val targetAsos = Json.parse(targetJson).as[AnalyzedSentenceObjects].analyzedSentenceObjects
    val resultAsos = Json.parse(resultJson).as[AnalyzedSentenceObjects].analyzedSentenceObjects

    val asos = resultAsos.foldLeft(List.empty[AnalyzedSentenceObject]){
      (acc, x) => {
        val selfAsos = targetAsos.filter(_.knowledgeBaseSemiGlobalNode.sentenceId.equals(x.knowledgeBaseSemiGlobalNode.sentenceId))
        val aso = selfAsos.size match {
          case 0 => x
          case _ => {
            //evidenceは、引き継ぎ、coveredEdgesは、引き継がない？
            AnalyzedSentenceObject(x.nodeMap, x.edgeList, x.knowledgeBaseSemiGlobalNode, selfAsos.head.deductionResult)

          }
        }
        acc :+ aso
      }
    }
    val updateResultJson = Json.toJson(AnalyzedSentenceObjects(asos, deducitonConfig)).toString()
    (targetJson, updateResultJson)
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
    val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(targetJson).as[AnalyzedSentenceObjects]
    val deducitonConfig = analyzedSentenceObjects.deductionConfiguration
    val hasPremise = analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType == SentenceType.PREMISE.index).size > 0
    //If the proposition has premise, the truth of the claim is determined along with the truth of havePremiseInGivenProposition.
    val checkTargets = hasPremise match  {
      case true => analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType == SentenceType.CLAIM.index && x.deductionResult.havePremiseInGivenProposition)
      case _ => analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType == SentenceType.CLAIM.index)
    }
    val notFinished = checkTargets.filterNot(x => x.deductionResult.status)
    if(notFinished.size > 0) {
      val targets:List[AnalyzedSentenceObject] = notFinished      
      val result = ToposoidUtils.callComponent(
            Json.toJson(AnalyzedSentenceObjects(targets, deducitonConfig)).toString(),
            endpoint.host,
            endpoint.port,
            "execute",
            transversalState)

      getResultJson(result, resultJson, deducitonConfig)
    }else{
      getResultJson(targetJson, resultJson, deducitonConfig)
    }
  }

  */

}

