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
import com.ideal.linked.toposoid.common.ToposoidUtils
import com.ideal.linked.toposoid.knowledgebase.regist.model.{Knowledge, PropositionRelation}
import com.ideal.linked.toposoid.protocol.model.base.AnalyzedSentenceObjects
import com.ideal.linked.toposoid.protocol.model.parser.{InputSentence, InputSentenceForParser, KnowledgeForParser, KnowledgeSentenceSetForParser}
import com.ideal.linked.toposoid.sentence.transformer.neo4j.Sentence2Neo4jTransformer
import com.ideal.linked.toposoid.vectorizer.FeatureVectorizer
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play.materializer
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers.{POST, contentType, defaultAwaitTimeout, status, _}
import play.api.test.{FakeRequest, _}
import io.jvm.uuid.UUID

import scala.concurrent.duration.DurationInt

class HomeControllerSpecEnglish extends PlaySpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerSuite with DefaultAwaitTimeout with Injecting{

  override def beforeAll(): Unit = {
    Neo4JAccessor.delete()
    //Sentence2Neo4jTransformer.createGraphAuto(List(UUID.random.toString), List(Knowledge("Life is so comfortable.","en_US", "{}", false)))
  }

  override def afterAll(): Unit = {
    Neo4JAccessor.delete()
  }

  override implicit def defaultAwaitTimeout: Timeout = 600.seconds
  val controller: HomeController = inject[HomeController]

  def registSingleClaim(knowledgeForParser:KnowledgeForParser): Unit = {
    val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
      List.empty[KnowledgeForParser],
      List.empty[PropositionRelation],
      List(knowledgeForParser),
      List.empty[PropositionRelation])
    Sentence2Neo4jTransformer.createGraph(knowledgeSentenceSetForParser)
    FeatureVectorizer.createVector(knowledgeSentenceSetForParser)
    Thread.sleep(5000)
  }

  "The specification1-english" should {
    "returns an appropriate response" in {
      val sentenceA = "Life is so comfortable."
      val paraphraseA = "Life is so comfortable."
      val propositionId1 = UUID.random.toString
      val sentenceId1 = UUID.random.toString
      val knowledge1 = Knowledge(sentenceA, "en_US", "{}", false)
      val paraphrase1 = Knowledge(paraphraseA,"en_US", "{}", false)
      registSingleClaim(KnowledgeForParser(propositionId1, sentenceId1, knowledge1))

      val json1 = """{
                    |    "index": 0,
                    |    "function":{
                    |        "host": "%s",
                    |        "port": "9101"
                    |    }
                    |}""".stripMargin.format(conf.getString("TOPOSOID_DEDUCTION_UNIT1_HOST"))

      val json2 = """{
                    |    "index": 1,
                    |    "function":{
                    |        "host": "%s",
                    |        "port": "9101"
                    |    }
                    |}""".stripMargin.format(conf.getString("TOPOSOID_DEDUCTION_UNIT1_HOST"))

      val json3 = """{
                    |    "index": 2,
                    |    "function":{
                    |        "host": "%s",
                    |        "port": "9101"
                    |    }
                    |}""".stripMargin.format(conf.getString("TOPOSOID_DEDUCTION_UNIT1_HOST"))

      val fr1 = FakeRequest(POST, "/changeEndPoints")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json1))

      val result1 = call(controller.changeEndPoints(), fr1)
      status(result1) mustBe OK
      contentType(result1) mustBe Some("application/json")
      assert(contentAsJson(result1).toString().equals("""{"status":"OK"}"""))

      val fr2 = FakeRequest(POST, "/changeEndPoints")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json2))

      val result2 = call(controller.changeEndPoints(), fr2)
      status(result2) mustBe OK
      contentType(result2) mustBe Some("application/json")
      assert(contentAsJson(result2).toString().equals("""{"status":"OK"}"""))

      val fr3 = FakeRequest(POST, "/changeEndPoints")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json3))

      val result3 = call(controller.changeEndPoints(), fr3)
      status(result3) mustBe OK
      contentType(result3) mustBe Some("application/json")
      assert(contentAsJson(result3).toString().equals("""{"status":"OK"}"""))

      val propositionIdForInference = UUID.random.toString
      val premiseKnowledge = List.empty[KnowledgeForParser]
      val claimKnowledge = List(KnowledgeForParser(propositionIdForInference, UUID.random.toString, paraphrase1))
      val inputSentence = Json.toJson(InputSentenceForParser(premiseKnowledge, claimKnowledge)).toString()

      //val inputSentence = Json.toJson(InputSentence(List.empty[Knowledge], List(Knowledge("Living is so comfortable.","en_US", "{}", false)))).toString()
      val json4 = ToposoidUtils.callComponent(inputSentence, conf.getString("SENTENCE_PARSER_EN_WEB_HOST"), "9007", "analyze")

      val fr4 = FakeRequest(POST, "/executeDeduction")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json4))

      val result4 = call(controller.executeDeduction(), fr4)
      status(result4) mustBe OK
      contentType(result4) mustBe Some("application/json")

      val jsonResult =contentAsJson(result4).toString()
      val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(analyzedSentenceObjects.analyzedSentenceObjects.filter(_.deductionResultMap.get("1").get.status).size == 1)
      assert(analyzedSentenceObjects.analyzedSentenceObjects.filter(_.deductionResultMap.get("1").get.deductionUnit.equals("exact-match")).size == 1)
    }
  }

  "The specification2-english" should {
    "returns an appropriate response" in {
      val sentenceA = "Life is so comfortable."
      val paraphraseA = "Living is so comfortable."
      val propositionId1 = UUID.random.toString
      val sentenceId1 = UUID.random.toString

      val knowledge1 = Knowledge(sentenceA, "en_US", "{}", false)
      val paraphrase1 = Knowledge(paraphraseA,"en_US", "{}", false)
      registSingleClaim(KnowledgeForParser(propositionId1, sentenceId1, knowledge1))

      val json1 = """{
                    |    "index": 0,
                    |    "function":{
                    |        "host": "%s",
                    |        "port": "9101"
                    |    }
                    |}""".stripMargin.format(conf.getString("TOPOSOID_DEDUCTION_UNIT1_HOST"))

      val json2 = """{
                    |    "index": 1,
                    |    "function":{
                    |        "host": "%s",
                    |        "port": "9102"
                    |    }
                    |}""".stripMargin.format(conf.getString("TOPOSOID_DEDUCTION_UNIT2_HOST"))

      val json3 = """{
                    |    "index": 2,
                    |    "function":{
                    |        "host": "%s",
                    |        "port": "9101"
                    |    }
                    |}""".stripMargin.format(conf.getString("TOPOSOID_DEDUCTION_UNIT1_HOST"))

      val fr1 = FakeRequest(POST, "/changeEndPoints")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json1))

      val result1 = call(controller.changeEndPoints(), fr1)
      status(result1) mustBe OK
      contentType(result1) mustBe Some("application/json")
      assert(contentAsJson(result1).toString().equals("""{"status":"OK"}"""))

      val fr2 = FakeRequest(POST, "/changeEndPoints")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json2))

      val result2 = call(controller.changeEndPoints(), fr2)
      status(result2) mustBe OK
      contentType(result2) mustBe Some("application/json")
      assert(contentAsJson(result2).toString().equals("""{"status":"OK"}"""))

      val fr3 = FakeRequest(POST, "/changeEndPoints")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json3))

      val result3 = call(controller.changeEndPoints(), fr3)
      status(result3) mustBe OK
      contentType(result3) mustBe Some("application/json")
      assert(contentAsJson(result3).toString().equals("""{"status":"OK"}"""))

      val propositionIdForInference = UUID.random.toString
      val premiseKnowledge = List.empty[KnowledgeForParser]
      val claimKnowledge = List(KnowledgeForParser(propositionIdForInference, UUID.random.toString, paraphrase1))
      val inputSentence = Json.toJson(InputSentenceForParser(premiseKnowledge, claimKnowledge)).toString()

      //val inputSentence = Json.toJson(InputSentence(List.empty[Knowledge], List(Knowledge("Living is so comfortable.","en_US", "{}", false)))).toString()
      val json4 = ToposoidUtils.callComponent(inputSentence, conf.getString("SENTENCE_PARSER_EN_WEB_HOST"), "9007", "analyze")

      val fr4 = FakeRequest(POST, "/executeDeduction")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json4))

      val result4 = call(controller.executeDeduction(), fr4)
      status(result4) mustBe OK
      contentType(result4) mustBe Some("application/json")

      val jsonResult =contentAsJson(result4).toString()
      val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(analyzedSentenceObjects.analyzedSentenceObjects.filter(_.deductionResultMap.get("1").get.status).size == 1)
      assert(analyzedSentenceObjects.analyzedSentenceObjects.filter(_.deductionResultMap.get("1").get.deductionUnit.equals("synonym-match")).size == 1)

    }
  }


  "The specification3-english" should {
    "returns an appropriate response" in {
      val sentenceA = "The culprit is among us."
      val paraphraseA = "We confirmed that the culprit was one of us."
      val propositionId1 = UUID.random.toString
      val sentenceId1 = UUID.random.toString

      val knowledge1 = Knowledge(sentenceA, "en_US", "{}", false)
      val paraphrase1 = Knowledge(paraphraseA,"en_US", "{}", false)
      registSingleClaim(KnowledgeForParser(propositionId1, sentenceId1, knowledge1))

      val json1 = """{
                    |    "index": 0,
                    |    "function":{
                    |        "host": "%s",
                    |        "port": "9101"
                    |    }
                    |}""".stripMargin.format(conf.getString("TOPOSOID_DEDUCTION_UNIT1_HOST"))

      val json2 = """{
                    |    "index": 1,
                    |    "function":{
                    |        "host": "%s",
                    |        "port": "9102"
                    |    }
                    |}""".stripMargin.format(conf.getString("TOPOSOID_DEDUCTION_UNIT2_HOST"))

      val json3 = """{
                    |    "index": 2,
                    |    "function":{
                    |        "host": "%s",
                    |        "port": "9103"
                    |    }
                    |}""".stripMargin.format(conf.getString("TOPOSOID_DEDUCTION_UNIT3_HOST"))

      val fr1 = FakeRequest(POST, "/changeEndPoints")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json1))

      val result1 = call(controller.changeEndPoints(), fr1)
      status(result1) mustBe OK
      contentType(result1) mustBe Some("application/json")
      assert(contentAsJson(result1).toString().equals("""{"status":"OK"}"""))

      val fr2 = FakeRequest(POST, "/changeEndPoints")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json2))

      val result2 = call(controller.changeEndPoints(), fr2)
      status(result2) mustBe OK
      contentType(result2) mustBe Some("application/json")
      assert(contentAsJson(result2).toString().equals("""{"status":"OK"}"""))

      val fr3 = FakeRequest(POST, "/changeEndPoints")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json3))

      val result3 = call(controller.changeEndPoints(), fr3)
      status(result3) mustBe OK
      contentType(result3) mustBe Some("application/json")
      assert(contentAsJson(result3).toString().equals("""{"status":"OK"}"""))

      val propositionIdForInference = UUID.random.toString
      val premiseKnowledge = List.empty[KnowledgeForParser]
      val claimKnowledge = List(KnowledgeForParser(propositionIdForInference, UUID.random.toString, paraphrase1))
      val inputSentence = Json.toJson(InputSentenceForParser(premiseKnowledge, claimKnowledge)).toString()

      //val inputSentence = Json.toJson(InputSentence(List.empty[Knowledge], List(Knowledge("Living is so comfortable.","en_US", "{}", false)))).toString()
      val json4 = ToposoidUtils.callComponent(inputSentence, conf.getString("SENTENCE_PARSER_EN_WEB_HOST"), "9007", "analyze")

      val fr4 = FakeRequest(POST, "/executeDeduction")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json4))

      val result4 = call(controller.executeDeduction(), fr4)
      status(result4) mustBe OK
      contentType(result4) mustBe Some("application/json")

      val jsonResult =contentAsJson(result4).toString()
      val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(analyzedSentenceObjects.analyzedSentenceObjects.filter(_.deductionResultMap.get("1").get.status).size == 1)
      assert(analyzedSentenceObjects.analyzedSentenceObjects.filter(_.deductionResultMap.get("1").get.deductionUnit.equals("sentence-vector-match")).size == 1)
    }
  }

  "The specification4-english" should {
    "returns an appropriate response" in {
      val sentenceA = "Life is so comfortable."
      val paraphraseA = "Life is so comfortable."
      val propositionId1 = UUID.random.toString
      val sentenceId1 = UUID.random.toString

      val knowledge1 = Knowledge(sentenceA, "en_US", "{}", false)
      val paraphrase1 = Knowledge(paraphraseA,"en_US", "{}", false)
      registSingleClaim(KnowledgeForParser(propositionId1, sentenceId1, knowledge1))

      val json1 = """{
                    |    "index": 0,
                    |    "function":{
                    |        "host": "%s",
                    |        "port": "9101"
                    |    }
                    |}""".stripMargin.format(conf.getString("TOPOSOID_DEDUCTION_UNIT1_HOST"))

      val json2 = """{
                    |    "index": 1,
                    |    "function":{
                    |        "host": "%s",
                    |        "port": "9102"
                    |    }
                    |}""".stripMargin.format(conf.getString("TOPOSOID_DEDUCTION_UNIT2_HOST"))

      val json3 = """{
                    |    "index": 2,
                    |    "function":{
                    |        "host": "%s",
                    |        "port": "9103"
                    |    }
                    |}""".stripMargin.format(conf.getString("TOPOSOID_DEDUCTION_UNIT3_HOST"))

      val fr1 = FakeRequest(POST, "/changeEndPoints")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json1))

      val result1 = call(controller.changeEndPoints(), fr1)
      status(result1) mustBe OK
      contentType(result1) mustBe Some("application/json")
      assert(contentAsJson(result1).toString().equals("""{"status":"OK"}"""))

      val fr2 = FakeRequest(POST, "/changeEndPoints")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json2))

      val result2 = call(controller.changeEndPoints(), fr2)
      status(result2) mustBe OK
      contentType(result2) mustBe Some("application/json")
      assert(contentAsJson(result2).toString().equals("""{"status":"OK"}"""))

      val fr3 = FakeRequest(POST, "/changeEndPoints")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json3))

      val result3 = call(controller.changeEndPoints(), fr3)
      status(result3) mustBe OK
      contentType(result3) mustBe Some("application/json")
      assert(contentAsJson(result3).toString().equals("""{"status":"OK"}"""))

      val propositionIdForInference = UUID.random.toString
      val premiseKnowledge = List.empty[KnowledgeForParser]
      val claimKnowledge = List(KnowledgeForParser(propositionIdForInference, UUID.random.toString, paraphrase1))
      val inputSentence = Json.toJson(InputSentenceForParser(premiseKnowledge, claimKnowledge)).toString()

      //val inputSentence = Json.toJson(InputSentence(List.empty[Knowledge], List(Knowledge("Living is so comfortable.","en_US", "{}", false)))).toString()
      val json4 = ToposoidUtils.callComponent(inputSentence, conf.getString("SENTENCE_PARSER_EN_WEB_HOST"), "9007", "analyze")

      val fr4 = FakeRequest(POST, "/executeDeduction")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json4))

      val result4 = call(controller.executeDeduction(), fr4)
      status(result4) mustBe OK
      contentType(result4) mustBe Some("application/json")

      val jsonResult =contentAsJson(result4).toString()
      val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(analyzedSentenceObjects.analyzedSentenceObjects.filter(_.deductionResultMap.get("1").get.status).size == 1)
      assert(analyzedSentenceObjects.analyzedSentenceObjects.filter(_.deductionResultMap.get("1").get.deductionUnit.equals("exact-match")).size == 1)

    }
  }


}
