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
import com.ideal.linked.toposoid.common.{CLAIM, PREMISE, TRANSVERSAL_STATE, ToposoidUtils, TransversalState}
import com.ideal.linked.toposoid.knowledgebase.regist.model.{Knowledge, PropositionRelation, Reference}
import com.ideal.linked.toposoid.protocol.model.base.AnalyzedSentenceObjects
import com.ideal.linked.toposoid.protocol.model.frontend.Endpoint
import com.ideal.linked.toposoid.protocol.model.parser.{InputSentenceForParser, KnowledgeForParser, KnowledgeSentenceSetForParser}
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

class HomeControllerSpecJapanese0 extends PlaySpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerSuite with DefaultAwaitTimeout with Injecting {

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

  "The deduction that noo4j has no data" should {
    "returns an appropriate response" in {
      val sentenceA = "太郎は秀逸な発案をした。"
      val sentence1 = Knowledge(sentenceA,"ja_JP", "{}", false)
      val propositionIdForInference = UUID.random.toString
      val premiseKnowledge = List.empty[KnowledgeForParser]
      val claimKnowledge = List(KnowledgeForParser(propositionIdForInference, UUID.random.toString, sentence1))
      val inputSentence = Json.toJson(InputSentenceForParser(premiseKnowledge, claimKnowledge)).toString()
      val json = ToposoidUtils.callComponent(inputSentence, conf.getString("TOPOSOID_SENTENCE_PARSER_JP_WEB_HOST"), conf.getString("TOPOSOID_SENTENCE_PARSER_JP_WEB_PORT"), "analyze", transversalState)

      val fr3 = FakeRequest(POST, "/executeDeduction")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse(json))

      val result3 = call(controller.executeDeduction(), fr3)
      status(result3) mustBe OK
      contentType(result3) mustBe Some("application/json")

      val jsonResult = contentAsJson(result3).toString()
      val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType.equals(PREMISE.index) && x.deductionResult.status).size == 0)
      assert(analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType.equals(CLAIM.index) && x.deductionResult.status).size == 0)
    }
  }

  "The deduction that there are no deduction-units." should {
    "returns an appropriate response" in {

      /*
      for(index <- 0 to 4){
        val json =
          """{
            |    "index": %d,
            |    "function":{
            |        "host": "%s",
            |        "port": "%s",
            |        "name": "%s"
            |    }
            |}""".stripMargin.format(index, "-", "-", "-")

        val fr1 = FakeRequest(POST, "/changeEndPoints")
          .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
          .withJsonBody(Json.parse(json))

        val result1 = call(controller.changeEndPoints(), fr1)
        status(result1) mustBe OK
        contentType(result1) mustBe Some("application/json")
        assert(contentAsJson(result1).toString().equals("""{"status":"OK"}"""))
      }
       */

      val emptyEndPoints = Seq( Endpoint("-", "-", ""), Endpoint("-", "-", ""), Endpoint("-", "-", ""), Endpoint("-", "-", ""), Endpoint("-", "-", ""))


      val fr1 = FakeRequest(POST, "/changeEndPoints")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.toJson(emptyEndPoints))

      val result1 = call(controller.changeEndPoints(), fr1)
      status(result1) mustBe OK
      contentType(result1) mustBe Some("application/json")
      assert(contentAsJson(result1).toString().equals("""{"status":"OK"}"""))


      val sentenceA = "太郎は秀逸な発案をした。"
      val sentence1 = Knowledge(sentenceA, "ja_JP", "{}", false)
      val propositionIdForInference = UUID.random.toString
      val premiseKnowledge = List.empty[KnowledgeForParser]
      val claimKnowledge = List(KnowledgeForParser(propositionIdForInference, UUID.random.toString, sentence1))
      val inputSentence = Json.toJson(InputSentenceForParser(premiseKnowledge, claimKnowledge)).toString()
      val json = ToposoidUtils.callComponent(inputSentence, conf.getString("TOPOSOID_SENTENCE_PARSER_JP_WEB_HOST"), conf.getString("TOPOSOID_SENTENCE_PARSER_JP_WEB_PORT"), "analyze", transversalState)

      val fr3 = FakeRequest(POST, "/executeDeduction")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse(json))

      val result3 = call(controller.executeDeduction(), fr3)
      status(result3) mustBe OK
      contentType(result3) mustBe Some("application/json")

      val jsonResult2 = contentAsJson(result3).toString()
      val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(jsonResult2).as[AnalyzedSentenceObjects]
      assert(analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType.equals(PREMISE.index) && x.deductionResult.status).size == 0)
      assert(analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType.equals(CLAIM.index) && x.deductionResult.status).size == 0)

    }
  }

}


