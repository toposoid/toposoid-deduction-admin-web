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
import com.ideal.linked.data.accessor.neo4j.Neo4JAccessor
import com.ideal.linked.toposoid.common.{CLAIM, TRANSVERSAL_STATE, ToposoidUtils, TransversalState}
import com.ideal.linked.toposoid.knowledgebase.regist.model.{Knowledge, KnowledgeForImage, Reference}
import com.ideal.linked.toposoid.protocol.model.base.AnalyzedSentenceObjects
import com.ideal.linked.toposoid.protocol.model.parser.{InputSentenceForParser, KnowledgeForParser}
import controllers.TestUtils._
import io.jvm.uuid.UUID
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play.materializer
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers.{POST, contentType, status, _}
import play.api.test._

import scala.concurrent.duration.DurationInt

class HomeControllerSpecEnglish3 extends PlaySpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerSuite with DefaultAwaitTimeout with Injecting{
  val transversalState:String = Json.toJson(TransversalState(username="guest")).toString()
  before {
    ToposoidUtils.callComponent("{}", conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_PORT"), "createSchema", TransversalState(username="guest"))
    ToposoidUtils.callComponent("{}", conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_PORT"), "createSchema", TransversalState(username="guest"))
    Neo4JAccessor.delete()
    Thread.sleep(1000)
  }

  override def beforeAll(): Unit = {
    Neo4JAccessor.delete()
  }

  override def afterAll(): Unit = {
    Neo4JAccessor.delete()
  }

  override implicit def defaultAwaitTimeout: Timeout = 600.seconds
  val controller: HomeController = inject[HomeController]
  val lang = "en_US"

  "The specification1-english(all)" should {
    "returns an appropriate response" in {
      val sentenceA = "Life is so comfortable."
      val sentenceB = "There are two cats."
      val referenceB = Reference(url = "", surface = "cats", surfaceIndex = 3, isWholeSentence = false,
        originalUrlOrReference = "http://images.cocodataset.org/val2017/000000039769.jpg")
      val imageBoxInfoB = ImageBoxInfo(x = 11, y = 11, weight = 466, height = 310)
      val sentenceC = "The culprit is among us."
      val sentenceD = "A large truck is parked."
      val referenceD = Reference(url = "", surface = "truck", surfaceIndex = 2, isWholeSentence = true,
        originalUrlOrReference = "https://farm8.staticflickr.com/7103/7210629614_5a388d9a9c_z.jpg")
      val imageBoxInfoD = ImageBoxInfo(x = 23, y = 25, weight = 601, height = 341)

      val propositionId1 = getUUID()
      val sentenceId1 = getUUID()
      val knowledge1 = Knowledge(sentenceA, lang, "{}", false)
      registSingleClaim(KnowledgeForParser(propositionId1, sentenceId1, knowledge1))

      val propositionId2 = getUUID()
      val sentenceId2 = getUUID()
      val knowledge2 = getKnowledge(lang = lang, sentence = sentenceB, reference = referenceB, imageBoxInfo = imageBoxInfoB)
      registSingleClaim(KnowledgeForParser(propositionId2, sentenceId2, knowledge2))

      val propositionId3 = getUUID()
      val sentenceId3 = getUUID()
      val knowledge3 = Knowledge(sentenceC, lang, "{}", false)
      registSingleClaim(KnowledgeForParser(propositionId3, sentenceId3, knowledge3))

      val propositionId4 = getUUID()
      val sentenceId4 = getUUID()
      val knowledge4 = getKnowledge(lang = lang, sentence = sentenceD, reference = referenceD, imageBoxInfo = imageBoxInfoD)
      registSingleClaim(KnowledgeForParser(propositionId4, sentenceId4, knowledge4))

      val paraphraseA = "Living is so comfortable."
      val paraphraseB = "There are two pets."
      val referenceParaB = Reference(url = "", surface = "pets", surfaceIndex = 3, isWholeSentence = false,
        originalUrlOrReference = "http://images.cocodataset.org/val2017/000000039769.jpg")
      val imageBoxInfoParaB = ImageBoxInfo(x = 11, y = 11, weight = 466, height = 310)

      val knowledgeParaB = getKnowledge(lang, paraphraseB, referenceParaB, imageBoxInfoParaB)
      val paraphraseC = "The culprit was one of us."
      val paraphraseD = "A large vehicle is parked."
      val referenceParaD = Reference(url = "", surface = "vehicle", surfaceIndex = 2, isWholeSentence = true,
        originalUrlOrReference = "https://farm8.staticflickr.com/7103/7210629614_5a388d9a9c_z.jpg")
      val imageBoxInfoParaD = ImageBoxInfo(x = 23, y = 25, weight = 601, height = 341)
      val knowledgeParaD = getKnowledge(lang, paraphraseD, referenceParaD, imageBoxInfoParaD)

      val propositionIdForInference = getUUID()

      val knowledgeForParser1 = KnowledgeForParser(propositionIdForInference, getUUID(), Knowledge(paraphraseA, lang, "{}", false, List.empty[KnowledgeForImage]))
      val knowledgeForParser3 = KnowledgeForParser(propositionIdForInference, getUUID(), Knowledge(paraphraseC, lang, "{}", false, List.empty[KnowledgeForImage]))

      val premiseKnowledge = List.empty[KnowledgeForParser]
      val claimKnowledgeA = List(knowledgeForParser1)
      val claimKnowledgeC = List(knowledgeForParser3)

      val inputSentenceA = Json.toJson(InputSentenceForParser(premiseKnowledge, claimKnowledgeA)).toString()
      val jsonNoImageA = ToposoidUtils.callComponent(inputSentenceA, conf.getString("TOPOSOID_SENTENCE_PARSER_EN_WEB_HOST"), conf.getString("TOPOSOID_SENTENCE_PARSER_EN_WEB_PORT"), "analyze", TransversalState(username="guest"))

      val premiseKnowledgeB = List.empty[KnowledgeForParser]
      val claimKnowledgeB = List(KnowledgeForParser(propositionIdForInference, getUUID(), knowledgeParaB))
      val inputSentenceB = Json.toJson(InputSentenceForParser(premiseKnowledgeB, claimKnowledgeB)).toString()

      val inputSentenceC = Json.toJson(InputSentenceForParser(premiseKnowledge, claimKnowledgeC)).toString()
      val jsonNoImageC = ToposoidUtils.callComponent(inputSentenceC, conf.getString("TOPOSOID_SENTENCE_PARSER_EN_WEB_HOST"), conf.getString("TOPOSOID_SENTENCE_PARSER_EN_WEB_PORT"), "analyze", TransversalState(username="guest"))

      val premiseKnowledgeD = List.empty[KnowledgeForParser]
      val claimKnowledgeD = List(KnowledgeForParser(propositionIdForInference, getUUID(), knowledgeParaD))
      val inputSentenceD = Json.toJson(InputSentenceForParser(premiseKnowledgeD, claimKnowledgeD)).toString()

      val asoA = Json.parse(jsonNoImageA).as[AnalyzedSentenceObjects].analyzedSentenceObjects.head
      val asoB = addImageInfoToLocalNode(lang, inputSentenceB, knowledgeParaB.knowledgeForImages).analyzedSentenceObjects.head
      val asoC = Json.parse(jsonNoImageC).as[AnalyzedSentenceObjects].analyzedSentenceObjects.head
      val asoD = addImageInfoToSemiGlobalNode(lang, inputSentenceD, knowledgeParaD.knowledgeForImages).analyzedSentenceObjects.head

      val inputAsos = AnalyzedSentenceObjects(List(asoA, asoB, asoC, asoD))
      val json = Json.toJson(inputAsos).toString()
      val fr = FakeRequest(POST, "/executeDeduction")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
        .withJsonBody(Json.parse(json))

      val result = call(controller.executeDeduction(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")

      val jsonResult = contentAsJson(result).toString()
      val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      val targetAsos = analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType.equals(CLAIM.index))
      val coveredPropositionEdgeSize = targetAsos.foldLeft(0) { (acc, x) =>
        x.deductionResult.coveredPropositionResults.foldLeft(0) {
          (acc2, y) => {
            if (x.deductionResult.coveredPropositionResults.filter(y => List("sentence-feature-match", "whole-sentence-image-feature-match").contains(y.deductionUnit)).size > 0) {
              if (y.deductionUnit.equals("sentence-feature-match") || y.deductionUnit.equals("whole-sentence-image-feature-match")) {
                acc2 + y.coveredPropositionEdges.size
              } else {
                0
              }
            } else {
              acc2 + y.coveredPropositionEdges.size
            }
          }
        } + acc
      }
      val actualEdgeSize = targetAsos.foldLeft(0) { (acc, x) => acc + x.edgeList.size }

      assert(analyzedSentenceObjects.analyzedSentenceObjects.size == 4)
      assert(targetAsos.filter(x => x.deductionResult.status).size == 4)
      assert(actualEdgeSize == coveredPropositionEdgeSize)
      assert(targetAsos.filter(x => x.deductionResult.coveredPropositionResults.filter(_.deductionUnit.equals("exact-match")).size > 0).size > 0)
      assert(targetAsos.filter(x => x.deductionResult.coveredPropositionResults.filter(_.deductionUnit.equals("synonym-match")).size > 0).size > 0)
      assert(targetAsos.filter(x => x.deductionResult.coveredPropositionResults.filter(_.deductionUnit.equals("image-vector-match")).size > 0).size > 0)
      assert(targetAsos.filter(x => x.deductionResult.coveredPropositionResults.filter(_.deductionUnit.equals("sentence-feature-match")).size > 0).size > 0)
      assert(targetAsos.filter(x => x.deductionResult.coveredPropositionResults.filter(_.deductionUnit.equals("whole-sentence-image-feature-match")).size > 0).size > 0)
    }
  }




}
