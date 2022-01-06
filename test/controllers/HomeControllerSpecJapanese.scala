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

import com.ideal.linked.data.accessor.neo4j.Neo4JAccessor
import com.ideal.linked.toposoid.knowledgebase.regist.model.Knowledge
import com.ideal.linked.toposoid.protocol.model.base.AnalyzedSentenceObjects
import com.ideal.linked.toposoid.sentence.transformer.neo4j.Sentence2Neo4jTransformer
import com.ideal.linked.common.DeploymentConverter.conf
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play.materializer
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers.{POST, contentAsString, contentType, defaultAwaitTimeout, status, _}
import play.api.test.{FakeRequest, _}

class HomeControllerSpecJapanese extends PlaySpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerSuite  with Injecting{

  override def beforeAll(): Unit = {
    Neo4JAccessor.delete()
    Sentence2Neo4jTransformer.createGraphAuto(List(Knowledge("太郎は秀逸な発案をした。", "ja_JP", "{}")))
  }

  override def afterAll(): Unit = {
    Neo4JAccessor.delete()
  }

  val controller: HomeController = inject[HomeController]
  "The specification1-japanese" should {
    "returns an appropriate response" in {

      val json1 = """{
                    |    "index": 0,
                    |    "function":{
                    |        "host": "%s",
                    |        "port": "9101"
                    |    }
                    |}""".stripMargin.format(conf.getString("DEDUCTION_UNIT1_HOST"))

      val json2 = """{
                    |    "index": 1,
                    |    "function":{
                    |        "host": "%s",
                    |        "port": "9101"
                    |    }
                    |}""".stripMargin.format(conf.getString("DEDUCTION_UNIT1_HOST"))

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


      val json3 = """{
                    |    "analyzedSentenceObjects": [
                    |        {
                    |            "nodeMap": {
                    |                "781d77ce-746f-4d86-8392-dacae3f6c9d2-3": {
                    |                    "nodeId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-3",
                    |                    "propositionId": "781d77ce-746f-4d86-8392-dacae3f6c9d2",
                    |                    "currentId": 3,
                    |                    "parentId": -1,
                    |                    "isMainSection": true,
                    |                    "surface": "した。",
                    |                    "normalizedName": "する",
                    |                    "dependType": "D",
                    |                    "caseType": "文末",
                    |                    "namedEntity": "",
                    |                    "rangeExpressions": {
                    |                        "": {}
                    |                    },
                    |                    "categories": {
                    |                        "": ""
                    |                    },
                    |                    "domains": {
                    |                        "": ""
                    |                    },
                    |                    "isDenial": false,
                    |                    "isConditionalConnection": false,
                    |                    "normalizedNameYomi": "する",
                    |                    "surfaceYomi": "した。",
                    |                    "modalityType": "-",
                    |                    "logicType": "-",
                    |                    "nodeType": 1,
                    |                    "lang": "ja_JP",
                    |                    "extentText": "{}"
                    |                },
                    |                "781d77ce-746f-4d86-8392-dacae3f6c9d2-2": {
                    |                    "nodeId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-2",
                    |                    "propositionId": "781d77ce-746f-4d86-8392-dacae3f6c9d2",
                    |                    "currentId": 2,
                    |                    "parentId": 3,
                    |                    "isMainSection": false,
                    |                    "surface": "提案を",
                    |                    "normalizedName": "提案",
                    |                    "dependType": "D",
                    |                    "caseType": "ヲ格",
                    |                    "namedEntity": "",
                    |                    "rangeExpressions": {
                    |                        "": {}
                    |                    },
                    |                    "categories": {
                    |                        "提案": "抽象物"
                    |                    },
                    |                    "domains": {
                    |                        "": ""
                    |                    },
                    |                    "isDenial": false,
                    |                    "isConditionalConnection": false,
                    |                    "normalizedNameYomi": "ていあん",
                    |                    "surfaceYomi": "ていあんを",
                    |                    "modalityType": "-",
                    |                    "logicType": "-",
                    |                    "nodeType": 1,
                    |                    "lang": "ja_JP",
                    |                    "extentText": "{}"
                    |                },
                    |                "781d77ce-746f-4d86-8392-dacae3f6c9d2-1": {
                    |                    "nodeId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-1",
                    |                    "propositionId": "781d77ce-746f-4d86-8392-dacae3f6c9d2",
                    |                    "currentId": 1,
                    |                    "parentId": 2,
                    |                    "isMainSection": false,
                    |                    "surface": "秀逸な",
                    |                    "normalizedName": "秀逸だ",
                    |                    "dependType": "D",
                    |                    "caseType": "連格",
                    |                    "namedEntity": "",
                    |                    "rangeExpressions": {
                    |                        "": {}
                    |                    },
                    |                    "categories": {
                    |                        "": ""
                    |                    },
                    |                    "domains": {
                    |                        "": ""
                    |                    },
                    |                    "isDenial": false,
                    |                    "isConditionalConnection": false,
                    |                    "normalizedNameYomi": "しゅういつだ",
                    |                    "surfaceYomi": "しゅういつな",
                    |                    "modalityType": "-",
                    |                    "logicType": "-",
                    |                    "nodeType": 1,
                    |                    "lang": "ja_JP",
                    |                    "extentText": "{}"
                    |                },
                    |                "781d77ce-746f-4d86-8392-dacae3f6c9d2-0": {
                    |                    "nodeId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-0",
                    |                    "propositionId": "781d77ce-746f-4d86-8392-dacae3f6c9d2",
                    |                    "currentId": 0,
                    |                    "parentId": 3,
                    |                    "isMainSection": false,
                    |                    "surface": "太郎は",
                    |                    "normalizedName": "太郎",
                    |                    "dependType": "D",
                    |                    "caseType": "未格",
                    |                    "namedEntity": "PERSON:太郎",
                    |                    "rangeExpressions": {
                    |                        "": {}
                    |                    },
                    |                    "categories": {
                    |                        "": ""
                    |                    },
                    |                    "domains": {
                    |                        "": ""
                    |                    },
                    |                    "isDenial": false,
                    |                    "isConditionalConnection": false,
                    |                    "normalizedNameYomi": "たろう",
                    |                    "surfaceYomi": "たろうは",
                    |                    "modalityType": "-",
                    |                    "logicType": "-",
                    |                    "nodeType": 1,
                    |                    "lang": "ja_JP",
                    |                    "extentText": "{}"
                    |                }
                    |            },
                    |            "edgeList": [
                    |                {
                    |                    "sourceId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-2",
                    |                    "destinationId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-3",
                    |                    "caseStr": "ヲ格",
                    |                    "dependType": "D",
                    |                    "logicType": "-",
                    |                    "lang": "ja_JP"
                    |                },
                    |                {
                    |                    "sourceId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-1",
                    |                    "destinationId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-2",
                    |                    "caseStr": "連格",
                    |                    "dependType": "D",
                    |                    "logicType": "-",
                    |                    "lang": "ja_JP"
                    |                },
                    |                {
                    |                    "sourceId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-0",
                    |                    "destinationId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-3",
                    |                    "caseStr": "未格",
                    |                    "dependType": "D",
                    |                    "logicType": "-",
                    |                    "lang": "ja_JP"
                    |                }
                    |            ],
                    |            "sentenceType": 1,
                    |            "deductionResultMap": {
                    |                "0": {
                    |                    "status": false,
                    |                    "matchedPropositionIds": [],
                    |                    "deductionUnit": ""
                    |                },
                    |                "1": {
                    |                    "status": false,
                    |                    "matchedPropositionIds": [],
                    |                    "deductionUnit": ""
                    |                }
                    |            }
                    |        }
                    |    ]
                    |}""".stripMargin

      val fr3 = FakeRequest(POST, "/executeDeduction")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json3))

      val result3 = call(controller.executeDeduction(), fr3)
      status(result3) mustBe OK
      contentType(result3) mustBe Some("application/json")

      val jsonResult =contentAsJson(result3).toString()
      val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(analyzedSentenceObjects.analyzedSentenceObjects.filterNot(_.deductionResultMap.get("1").get.status).size == 1)
    }
  }

  "The specification2-japanese" should {
    "returns an appropriate response" in {

      val json1 = """{
                    |    "index": 0,
                    |    "function":{
                    |        "host": "%s",
                    |        "port": "9101"
                    |    }
                    |}""".stripMargin.format(conf.getString("DEDUCTION_UNIT1_HOST"))

      val json2 = """{
                    |    "index": 1,
                    |    "function":{
                    |        "host": "%s",
                    |        "port": "9102"
                    |    }
                    |}""".stripMargin.format(conf.getString("DEDUCTION_UNIT2_HOST"))

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

      val json3 = """{
                    |    "analyzedSentenceObjects": [
                    |        {
                    |            "nodeMap": {
                    |                "781d77ce-746f-4d86-8392-dacae3f6c9d2-3": {
                    |                    "nodeId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-3",
                    |                    "propositionId": "781d77ce-746f-4d86-8392-dacae3f6c9d2",
                    |                    "currentId": 3,
                    |                    "parentId": -1,
                    |                    "isMainSection": true,
                    |                    "surface": "した。",
                    |                    "normalizedName": "する",
                    |                    "dependType": "D",
                    |                    "caseType": "文末",
                    |                    "namedEntity": "",
                    |                    "rangeExpressions": {
                    |                        "": {}
                    |                    },
                    |                    "categories": {
                    |                        "": ""
                    |                    },
                    |                    "domains": {
                    |                        "": ""
                    |                    },
                    |                    "isDenial": false,
                    |                    "isConditionalConnection": false,
                    |                    "normalizedNameYomi": "する",
                    |                    "surfaceYomi": "した。",
                    |                    "modalityType": "-",
                    |                    "logicType": "-",
                    |                    "nodeType": 1,
                    |                    "lang": "ja_JP",
                    |                    "extentText": "{}"
                    |                },
                    |                "781d77ce-746f-4d86-8392-dacae3f6c9d2-2": {
                    |                    "nodeId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-2",
                    |                    "propositionId": "781d77ce-746f-4d86-8392-dacae3f6c9d2",
                    |                    "currentId": 2,
                    |                    "parentId": 3,
                    |                    "isMainSection": false,
                    |                    "surface": "提案を",
                    |                    "normalizedName": "提案",
                    |                    "dependType": "D",
                    |                    "caseType": "ヲ格",
                    |                    "namedEntity": "",
                    |                    "rangeExpressions": {
                    |                        "": {}
                    |                    },
                    |                    "categories": {
                    |                        "提案": "抽象物"
                    |                    },
                    |                    "domains": {
                    |                        "": ""
                    |                    },
                    |                    "isDenial": false,
                    |                    "isConditionalConnection": false,
                    |                    "normalizedNameYomi": "ていあん",
                    |                    "surfaceYomi": "ていあんを",
                    |                    "modalityType": "-",
                    |                    "logicType": "-",
                    |                    "nodeType": 1,
                    |                    "lang": "ja_JP",
                    |                    "extentText": "{}"
                    |                },
                    |                "781d77ce-746f-4d86-8392-dacae3f6c9d2-1": {
                    |                    "nodeId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-1",
                    |                    "propositionId": "781d77ce-746f-4d86-8392-dacae3f6c9d2",
                    |                    "currentId": 1,
                    |                    "parentId": 2,
                    |                    "isMainSection": false,
                    |                    "surface": "秀逸な",
                    |                    "normalizedName": "秀逸だ",
                    |                    "dependType": "D",
                    |                    "caseType": "連格",
                    |                    "namedEntity": "",
                    |                    "rangeExpressions": {
                    |                        "": {}
                    |                    },
                    |                    "categories": {
                    |                        "": ""
                    |                    },
                    |                    "domains": {
                    |                        "": ""
                    |                    },
                    |                    "isDenial": false,
                    |                    "isConditionalConnection": false,
                    |                    "normalizedNameYomi": "しゅういつだ",
                    |                    "surfaceYomi": "しゅういつな",
                    |                    "modalityType": "-",
                    |                    "logicType": "-",
                    |                    "nodeType": 1,
                    |                    "lang": "ja_JP",
                    |                    "extentText": "{}"
                    |                },
                    |                "781d77ce-746f-4d86-8392-dacae3f6c9d2-0": {
                    |                    "nodeId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-0",
                    |                    "propositionId": "781d77ce-746f-4d86-8392-dacae3f6c9d2",
                    |                    "currentId": 0,
                    |                    "parentId": 3,
                    |                    "isMainSection": false,
                    |                    "surface": "太郎は",
                    |                    "normalizedName": "太郎",
                    |                    "dependType": "D",
                    |                    "caseType": "未格",
                    |                    "namedEntity": "PERSON:太郎",
                    |                    "rangeExpressions": {
                    |                        "": {}
                    |                    },
                    |                    "categories": {
                    |                        "": ""
                    |                    },
                    |                    "domains": {
                    |                        "": ""
                    |                    },
                    |                    "isDenial": false,
                    |                    "isConditionalConnection": false,
                    |                    "normalizedNameYomi": "たろう",
                    |                    "surfaceYomi": "たろうは",
                    |                    "modalityType": "-",
                    |                    "logicType": "-",
                    |                    "nodeType": 1,
                    |                    "lang": "ja_JP",
                    |                    "extentText": "{}"
                    |                }
                    |            },
                    |            "edgeList": [
                    |                {
                    |                    "sourceId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-2",
                    |                    "destinationId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-3",
                    |                    "caseStr": "ヲ格",
                    |                    "dependType": "D",
                    |                    "logicType": "-",
                    |                    "lang": "ja_JP"
                    |                },
                    |                {
                    |                    "sourceId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-1",
                    |                    "destinationId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-2",
                    |                    "caseStr": "連格",
                    |                    "dependType": "D",
                    |                    "logicType": "-",
                    |                    "lang": "ja_JP"
                    |                },
                    |                {
                    |                    "sourceId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-0",
                    |                    "destinationId": "781d77ce-746f-4d86-8392-dacae3f6c9d2-3",
                    |                    "caseStr": "未格",
                    |                    "dependType": "D",
                    |                    "logicType": "-",
                    |                    "lang": "ja_JP"
                    |                }
                    |            ],
                    |            "sentenceType": 1,
                    |            "deductionResultMap": {
                    |                "0": {
                    |                    "status": false,
                    |                    "matchedPropositionIds": [],
                    |                    "deductionUnit": ""
                    |                },
                    |                "1": {
                    |                    "status": false,
                    |                    "matchedPropositionIds": [],
                    |                    "deductionUnit": ""
                    |                }
                    |            }
                    |        }
                    |    ]
                    |}""".stripMargin

      val fr3 = FakeRequest(POST, "/executeDeduction")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(json3))

      val result3 = call(controller.executeDeduction(), fr3)
      status(result3) mustBe OK
      contentType(result3) mustBe Some("application/json")

      val jsonResult =contentAsJson(result3).toString()
      val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(analyzedSentenceObjects.analyzedSentenceObjects.filter(_.deductionResultMap.get("1").get.status).size == 1)

    }
  }
}

