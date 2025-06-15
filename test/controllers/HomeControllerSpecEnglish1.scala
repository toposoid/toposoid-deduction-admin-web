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

import akka.util.Timeout
import com.ideal.linked.common.DeploymentConverter.conf
import com.ideal.linked.toposoid.common.{CLAIM, TRANSVERSAL_STATE, ToposoidUtils, TransversalState}
import com.ideal.linked.toposoid.knowledgebase.regist.model.{Knowledge, Reference}
import com.ideal.linked.toposoid.protocol.model.base.AnalyzedSentenceObjects
import com.ideal.linked.toposoid.protocol.model.frontend.Endpoint
import com.ideal.linked.toposoid.protocol.model.parser.{InputSentenceForParser, KnowledgeForParser}
import controllers.TestUtilsEx.{addImageInfoToLocalNode, getImageInfo, getKnowledge, getUUID, registerSingleClaim}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play.materializer
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers.{POST, contentType, defaultAwaitTimeout, status, _}
import play.api.test._
import io.jvm.uuid.UUID

import scala.concurrent.duration.DurationInt

class HomeControllerSpecEnglish1 extends PlaySpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerSuite with DefaultAwaitTimeout with Injecting{

  val transversalState:TransversalState = TransversalState(userId="test-user", username="guest", roleId=0, csrfToken = "")
  val transversalStateJson:String = Json.toJson(transversalState).toString()

  before {
    ToposoidUtils.callComponent("{}", conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_PORT"), "createSchema", transversalState)
    ToposoidUtils.callComponent("{}", conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_PORT"), "createSchema", transversalState)
    TestUtilsEx.deleteNeo4JAllData(transversalState)
    Thread.sleep(1000)
  }

  override def beforeAll(): Unit = {
    TestUtilsEx.deleteNeo4JAllData(transversalState)
  }

  override def afterAll(): Unit = {
    TestUtilsEx.deleteNeo4JAllData(transversalState)
  }

  override implicit def defaultAwaitTimeout: Timeout = 600.seconds
  val controller: HomeController = inject[HomeController]
  val lang = "en_US"

  /*
  def setEndPoints(indices: List[Int]): Unit = {
    for (index <- 0 to 4) {
      val endPointInfo = indices.contains(index) match {
        case true => {
          val host = conf.getString("TOPOSOID_DEDUCTION_UNIT%d_HOST".format(index + 1))
          val port = conf.getString("TOPOSOID_DEDUCTION_UNIT%d_PORT".format(index + 1))
          val name = conf.getString("TOPOSOID_DEDUCTION_UNIT%d_NAME".format(index + 1))
          (host, port, name)
        }
        case _ => {
          ("-", "-", "-")
        }
      }
      val json =
        """{
          |    "index": %d,
          |    "function":{
          |        "host": "%s",
          |        "port": "%s",
          |        "name": "%s"
          |    }
          |}""".stripMargin.format(index, endPointInfo._1, endPointInfo._2, endPointInfo._3)

      val fr1 = FakeRequest(POST, "/changeEndPoints")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
        .withJsonBody(Json.parse(json))

      val result1 = call(controller.changeEndPoints(), fr1)
      status(result1) mustBe OK
    }
  }
  */
  def setEndPoints(indices: List[Int]): Unit = {
    val endPoints: Seq[Endpoint] = List(0, 1, 2, 3, 4).foldLeft(Seq.empty[Endpoint]) {
      (acc, x) => {
        val endpoint = indices.contains(x) match {
          case true => {
            val host = conf.getString("TOPOSOID_DEDUCTION_UNIT%d_HOST".format(x + 1))
            val port = conf.getString("TOPOSOID_DEDUCTION_UNIT%d_PORT".format(x + 1))
            val name = conf.getString("TOPOSOID_DEDUCTION_UNIT%d_NAME".format(x + 1))
            Endpoint(host, port, name)
          }
          case _ => Endpoint("-", "-", "-")
        }
        acc :+ endpoint
      }
    }
    val fr1 = FakeRequest(POST, "/changeEndPoints")
      .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
      .withJsonBody(Json.toJson(endPoints))

    val result1 = call(controller.changeEndPoints(), fr1)
    status(result1) mustBe OK
  }


  "The specification1-english(exist-match)" should {
    "returns an appropriate response" in {
      val sentenceA = "Life is so comfortable."
      val paraphraseA = "Life is so comfortable."
      val propositionId1 = UUID.random.toString
      val sentenceId1 = UUID.random.toString
      val knowledge1 = Knowledge(sentenceA, "en_US", "{}", false)
      val paraphrase1 = Knowledge(paraphraseA,"en_US", "{}", false)
      registerSingleClaim(KnowledgeForParser(propositionId1, sentenceId1, knowledge1), transversalState)
      setEndPoints(List(0,1))

      val propositionIdForInference = UUID.random.toString
      val premiseKnowledge = List.empty[KnowledgeForParser]
      val claimKnowledge = List(KnowledgeForParser(propositionIdForInference, UUID.random.toString, paraphrase1))
      val inputSentence = Json.toJson(InputSentenceForParser(premiseKnowledge, claimKnowledge)).toString()

      //val inputSentence = Json.toJson(InputSentence(List.empty[Knowledge], List(Knowledge("Living is so comfortable.","en_US", "{}", false)))).toString()
      val json4 = ToposoidUtils.callComponent(inputSentence, conf.getString("TOPOSOID_SENTENCE_PARSER_EN_WEB_HOST"), conf.getString("TOPOSOID_SENTENCE_PARSER_EN_WEB_PORT"), "analyze", transversalState)

      val fr4 = FakeRequest(POST, "/executeDeduction")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse(json4))

      val result4 = call(controller.executeDeduction(), fr4)
      status(result4) mustBe OK
      contentType(result4) mustBe Some("application/json")

      val jsonResult =contentAsJson(result4).toString()
      val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      val targetAsos = analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType.equals(CLAIM.index))

      val coveredPropositionEdgeSize = targetAsos.foldLeft(0) { (acc, x) => x.deductionResult.coveredPropositionResults.foldLeft(0) { (acc2, y) => acc2 + y.coveredPropositionEdges.size } + acc }
      val coveredKnowledgeSize = targetAsos.foldLeft(0) { (acc, x) => x.deductionResult.coveredPropositionResults.foldLeft(0) { (acc2, y) => acc2 + y.knowledgeBaseSideInfoList.size } + acc }
      val actualEdgeSize = targetAsos.foldLeft(0) { (acc, x) => acc + x.edgeList.size }

      assert(actualEdgeSize == coveredPropositionEdgeSize)
      assert(actualEdgeSize <= coveredKnowledgeSize)
      assert(targetAsos.filter(x => x.deductionResult.status).size == 1)
      assert(targetAsos.filter(x => x.deductionResult.coveredPropositionResults.filter(_.deductionUnit.equals("exact-match")).size == 1).size == 1)
    }
  }

  "The specification2-english(synonym-match)" should {
    "returns an appropriate response" in {
      val sentenceA = "Life is so comfortable."
      val paraphraseA = "Living is so comfortable."
      val propositionId1 = UUID.random.toString
      val sentenceId1 = UUID.random.toString

      val knowledge1 = Knowledge(sentenceA, "en_US", "{}", false)
      val paraphrase1 = Knowledge(paraphraseA,"en_US", "{}", false)
      registerSingleClaim(KnowledgeForParser(propositionId1, sentenceId1, knowledge1), transversalState)
      setEndPoints(List(0,1))

      val propositionIdForInference = UUID.random.toString
      val premiseKnowledge = List.empty[KnowledgeForParser]
      val claimKnowledge = List(KnowledgeForParser(propositionIdForInference, UUID.random.toString, paraphrase1))
      val inputSentence = Json.toJson(InputSentenceForParser(premiseKnowledge, claimKnowledge)).toString()

      //val inputSentence = Json.toJson(InputSentence(List.empty[Knowledge], List(Knowledge("Living is so comfortable.","en_US", "{}", false)))).toString()
      val json4 = ToposoidUtils.callComponent(inputSentence, conf.getString("TOPOSOID_SENTENCE_PARSER_EN_WEB_HOST"), conf.getString("TOPOSOID_SENTENCE_PARSER_EN_WEB_PORT"), "analyze", transversalState)

      val fr4 = FakeRequest(POST, "/executeDeduction")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse(json4))

      val result4 = call(controller.executeDeduction(), fr4)
      status(result4) mustBe OK
      contentType(result4) mustBe Some("application/json")

      val jsonResult =contentAsJson(result4).toString()
      val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      val targetAsos = analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType.equals(CLAIM.index))
      val coveredPropositionEdgeSize = targetAsos.foldLeft(0) { (acc, x) => x.deductionResult.coveredPropositionResults.foldLeft(0) { (acc2, y) => acc2 + y.coveredPropositionEdges.size } + acc }
      val coveredKnowledgeSize = targetAsos.foldLeft(0) { (acc, x) => x.deductionResult.coveredPropositionResults.foldLeft(0) { (acc2, y) => acc2 + y.knowledgeBaseSideInfoList.size } + acc }
      val actualEdgeSize = targetAsos.foldLeft(0) { (acc, x) => acc + x.edgeList.size }

      assert(actualEdgeSize == coveredPropositionEdgeSize)
      assert(actualEdgeSize <= coveredKnowledgeSize)
      assert(targetAsos.filter(x => x.deductionResult.status).size == 1)
      assert(targetAsos.filter(x => x.deductionResult.coveredPropositionResults.filter(_.deductionUnit.equals("synonym-match")).size == 1).size == 1)
    }
  }


  "The specification3-english(image-vector-match)" should {
    "returns an appropriate response" in {

      val sentenceA = "There are two cats."
      val referenceA = Reference(url = "", surface = "cats", surfaceIndex = 3, isWholeSentence = false,
        originalUrlOrReference = "http://images.cocodataset.org/val2017/000000039769.jpg")
      val imageBoxInfoA = ImageBoxInfo(x = 11, y = 11, weight = 466, height = 310)

      val paraphraseA = "There are two pets."
      val referenceParaA = Reference(url = "", surface = "pets", surfaceIndex = 3, isWholeSentence = false,
        originalUrlOrReference = "http://images.cocodataset.org/val2017/000000039769.jpg")
      val imageBoxInfoParaA = ImageBoxInfo(x = 11, y = 11, weight = 466, height = 310)

      val propositionId1 = getUUID()
      val sentenceId1 = getUUID()
      //val knowledge1 = Knowledge(sentenceA,"ja_JP", "{}", false, List(imageA))
      val knowledge1 = getKnowledge(lang = lang, sentence = sentenceA, reference = referenceA, imageBoxInfo = imageBoxInfoA, transversalState)
      val paraphrase1 = getKnowledge(lang = lang, sentence = paraphraseA, reference = referenceA, imageBoxInfo = imageBoxInfoA, transversalState)
      registerSingleClaim(KnowledgeForParser(propositionId1, sentenceId1, knowledge1), transversalState)
      setEndPoints(List(0, 1, 3))

      val propositionIdForInference = getUUID()
      val premiseKnowledge = List.empty[KnowledgeForParser]
      val claimKnowledge = List(KnowledgeForParser(propositionIdForInference, getUUID(), paraphrase1))
      val inputSentence = Json.toJson(InputSentenceForParser(premiseKnowledge, claimKnowledge)).toString()
      //val json = ToposoidUtils.callComponent(inputSentence, conf.getString("TOPOSOID_SENTENCE_PARSER_JP_WEB_HOST"), conf.getString("TOPOSOID_SENTENCE_PARSER_JP_WEB_PORT"), "analyze", transversalState)
      val asos = addImageInfoToLocalNode(lang = lang, inputSentence, List(getImageInfo(referenceParaA, imageBoxInfoParaA, transversalState)), transversalState)
      val json: String = Json.toJson(asos).toString()

      val fr = FakeRequest(POST, "/executeDeduction")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse(json))

      val result = call(controller.executeDeduction(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")

      val jsonResult = contentAsJson(result).toString()
      val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      val targetAsos = analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType.equals(CLAIM.index))

      val coveredPropositionEdgeSize = targetAsos.foldLeft(0) { (acc, x) => x.deductionResult.coveredPropositionResults.foldLeft(0) { (acc2, y) => acc2 + y.coveredPropositionEdges.size } + acc }
      val coveredKnowledgeSize = targetAsos.foldLeft(0) { (acc, x) => x.deductionResult.coveredPropositionResults.foldLeft(0) { (acc2, y) => acc2 + y.knowledgeBaseSideInfoList.size } + acc }
      val actualEdgeSize = targetAsos.foldLeft(0) { (acc, x) => acc + x.edgeList.size }

      assert(actualEdgeSize == coveredPropositionEdgeSize)
      assert(actualEdgeSize <= coveredKnowledgeSize)
      assert(targetAsos.filter(x => x.deductionResult.status).size == 1)
      assert(targetAsos.filter(x => x.deductionResult.coveredPropositionResults.filter(_.deductionUnit.equals("image-vector-match")).size == 1).size == 1)

    }
  }

}
