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

import org.apache.pekko.util.Timeout
import com.ideal.linked.common.DeploymentConverter.conf
import com.ideal.linked.toposoid.common.{SentenceType, TRANSVERSAL_STATE, ToposoidUtils, TransversalState}
import com.ideal.linked.toposoid.knowledgebase.regist.model.{Knowledge, KnowledgeForImage, Reference}
import com.ideal.linked.toposoid.protocol.model.base.AnalyzedSentenceObjects
import com.ideal.linked.toposoid.protocol.model.frontend.Endpoint
import com.ideal.linked.toposoid.protocol.model.parser.{InputSentenceForParser, KnowledgeForParser}
import com.ideal.linked.toposoid.test.utils.TestUtils.{getAnalyzedSentenceObjectsJson, getAnalyzedSentenceObjectsJsonForSemiGlobal, setDeductionUnitEndPoints, uploadImage, uploadTable}
import controllers.TestUtilsEx.{getUUID, registerSingleClaim, deleteNeo4JAllData}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play.materializer
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers.{POST, contentType, status, _}
import play.api.test._

import scala.concurrent.duration.DurationInt
import com.ideal.linked.toposoid.common.ActionModeType
import com.ideal.linked.toposoid.protocol.model.base.DeductionConfiguration
import com.ideal.linked.toposoid.test.utils.TestUtils
import com.ideal.linked.toposoid.common.DeductionPhaseType
import com.ideal.linked.toposoid.knowledgebase.regist.model.ImageReference
import com.ideal.linked.toposoid.knowledgebase.regist.model.KnowledgeForTable
import com.ideal.linked.toposoid.knowledgebase.regist.model.TableReference

class HomeControllerSpecJapanese3 extends PlaySpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerSuite with DefaultAwaitTimeout with Injecting {

  val transversalState:TransversalState = TransversalState(userId="test-user", username="guest", roleId=0, csrfToken = "")
  val transversalStateJson:String = Json.toJson(transversalState).toString()

  before {
    ToposoidUtils.callComponent("{}", conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_SENTENCE_VECTORDB_ACCESSOR_PORT"), "createSchema", transversalState)
    ToposoidUtils.callComponent("{}", conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_IMAGE_VECTORDB_ACCESSOR_PORT"), "createSchema", transversalState)
    ToposoidUtils.callComponent("{}", conf.getString("TOPOSOID_TABLE_VECTORDB_ACCESSOR_HOST"), conf.getString("TOPOSOID_TABLE_VECTORDB_ACCESSOR_PORT"), "createSchema", transversalState)
    TestUtilsEx.deleteNeo4JAllData(transversalState)
    Thread.sleep(1000)
  }
  after {
    TestUtilsEx.deleteNeo4JAllData(transversalState)
  }

  override def beforeAll(): Unit = {
    TestUtilsEx.deleteNeo4JAllData(transversalState)
  }

  override def afterAll(): Unit = {
    TestUtilsEx.deleteNeo4JAllData(transversalState)
  }
  val lang = "ja_JP"

  override implicit def defaultAwaitTimeout: Timeout = 600.seconds

  val controller: HomeController = inject[HomeController]
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
  /*
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
  */
  "The specification1-japanese5(all)" should {
    "returns an appropriate response" in {
      val sentenceA = "太郎は秀逸な発案をした。"
      val sentenceB = "猫が２匹寝てます。"
      val referenceB = Reference(url = "", surface = "猫が", surfaceIndex = 0, isWholeSentence = false,
        originalUrlOrReference = "http://images.cocodataset.org/val2017/000000039769.jpg")
      val imageReferenceB = ImageReference(referenceB, x = 11, y = 11, width = 466, height = 310)
      val knowledgeForImageB = KnowledgeForImage(getUUID(), imageReferenceB)                    
      //val imageBoxInfoB = ImageBoxInfo(x = 11, y = 11, weight = 466, height = 310)
      val sentenceC = "自然界の法則がすべての慣性系で同じように成り立っている。"
      val sentenceD = "トラックが一台止まっています。"
      val referenceD = Reference(url = "", surface = "トラックが", surfaceIndex = 0, isWholeSentence = true,
        originalUrlOrReference = "https://farm8.staticflickr.com/7103/7210629614_5a388d9a9c_z.jpg")
      val imageReferenceD = ImageReference(referenceD, x = 23, y = 25, width = 601, height = 341)
      val knowledgeForImageD = KnowledgeForImage(getUUID(), imageReferenceD)         
      //val imageBoxInfoD = ImageBoxInfo(x = 23, y = 25, weight = 601, height = 341)

      val sentenceE = "証拠データが一つあります。"
      val referenceE = Reference(url = "", surface = "証拠データが", surfaceIndex = 0, isWholeSentence = false,
        originalUrlOrReference = "https://www.e-stat.go.jp/stat-search/file-download?statInfId=000001086170&fileKind=0")
      val tableReferenceE = TableReference(referenceE, skipHeaderRows=5, skipRowList=List(),multiHeaderRows=4, sheetNameForExcel= "se0101")
      val knowledgeForTableE = KnowledgeForTable(getUUID(), tableReferenceE)  

      val sentenceF = "一つの証拠データの提出を厳格に義務付けます。"
      val referenceF = Reference(url = "", surface = "", surfaceIndex = -1, isWholeSentence = true,
        originalUrlOrReference = "https://www.e-stat.go.jp/stat-search/file-download?statInfId=000040292480&fileKind=1")
      val tableReferenceF = TableReference(referenceF, skipHeaderRows=8, skipRowList=List(),multiHeaderRows=1, sheetNameForExcel= "")  
      val knowledgeForTableF = KnowledgeForTable(getUUID(), tableReferenceF)    

      val propositionId1 = getUUID()
      val sentenceId1 = getUUID()
      val knowledge1 = Knowledge(sentenceA, lang, "{}", false)
      registerSingleClaim(KnowledgeForParser(propositionId1, sentenceId1, knowledge1), transversalState)

      val propositionId2 = getUUID()
      val sentenceId2 = getUUID()
      val knowledge2 = Knowledge(lang = lang, sentence = sentenceB, extentInfoJson = "{}", knowledgeForImages=List(uploadImage(knowledgeForImageB, transversalState)))
      registerSingleClaim(KnowledgeForParser(propositionId2, sentenceId2, knowledge2), transversalState)

      val propositionId3 = getUUID()
      val sentenceId3 = getUUID()
      val knowledge3 = Knowledge(sentenceC, lang, "{}", false)
      registerSingleClaim(KnowledgeForParser(propositionId3, sentenceId3, knowledge3), transversalState)

      val propositionId4 = getUUID()
      val sentenceId4 = getUUID()
      val knowledge4 = Knowledge(lang = lang, sentence = sentenceD, extentInfoJson = "{}", knowledgeForImages=List(uploadImage(knowledgeForImageD, transversalState)))
      registerSingleClaim(KnowledgeForParser(propositionId4, sentenceId4, knowledge4), transversalState)

      val propositionId5 = getUUID()
      val sentenceId5 = getUUID()
      val knowledge5 = Knowledge(lang = lang, sentence = sentenceE, extentInfoJson = "{}", knowledgeForTables=List(uploadTable(knowledgeForTableE, transversalState)))
      registerSingleClaim(KnowledgeForParser(propositionId5, sentenceId5, knowledge5), transversalState)
      
      val propositionId6 = getUUID()
      val sentenceId6 = getUUID()
      val knowledge6 = Knowledge(lang = lang, sentence = sentenceF, extentInfoJson = "{}", knowledgeForTables=List(uploadTable(knowledgeForTableF, transversalState)))
      registerSingleClaim(KnowledgeForParser(propositionId6, sentenceId6, knowledge6), transversalState)

      TestUtils.setDeductionUnitEndPoints(DeductionPhaseType.DEDUCTION_TERM_BASE, transversalState)
      TestUtils.setDeductionUnitEndPoints(DeductionPhaseType.DEDUCTION_SENTENCE_BASE, transversalState)

      val endPoints: Seq[Endpoint] = List(Endpoint("toposoid-embedding-deduction-unit-common-web", "9202", "GroupEmbeddingMatch"), Endpoint("toposoid-clause-deduction-unit-common-web", "9201", "GroupClauseMatch"))
      val fr0 = FakeRequest(POST, "/changeEndPoints")
      .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
      .withJsonBody(Json.toJson(endPoints))
      val result0 = call(controller.changeEndPoints(), fr0)
      status(result0) mustBe OK


      val paraphraseA = "太郎は秀逸な提案をした。"
      val knowledgeParaA = Knowledge(paraphraseA, lang, extentInfoJson = "{}")

      val paraphraseB = "ペットが２匹寝てます。"      
      val referenceParaB = Reference(url = "", surface = "ペットが", surfaceIndex = 0, isWholeSentence = false,
        originalUrlOrReference = "http://images.cocodataset.org/val2017/000000039769.jpg")
      val imageReferenceParaB = ImageReference(referenceParaB, x = 11, y = 11, width = 466, height = 310)
      val knowledgeForImageParaB = KnowledgeForImage(getUUID(), imageReferenceParaB)                          
      //val imageBoxInfoParaB = ImageBoxInfo(x = 11, y = 11, weight = 466, height = 310)
      val knowledgeParaB = Knowledge(paraphraseB, lang, extentInfoJson = "{}", knowledgeForImages=List(uploadImage(knowledgeForImageParaB, transversalState)))

      val paraphraseC = "自然界の物理法則は例外なくどの慣性系でも成立する。"
      val knowledgeParaC = Knowledge(paraphraseC, lang, extentInfoJson = "{}")
      
      val paraphraseD = "トレーラーが一台止まっています。"
      val referenceParaD = Reference(url = "", surface = "大型車が", surfaceIndex = 0, isWholeSentence = true,
        originalUrlOrReference = "https://farm8.staticflickr.com/7103/7210629614_5a388d9a9c_z.jpg")
      val imageReferenceParaD = ImageReference(referenceParaD, x = 23, y = 25, width = 601, height = 341)
      val knowledgeForImageParaD = KnowledgeForImage(getUUID(), imageReferenceParaD)               
      //val imageBoxInfoParaD = ImageBoxInfo(x = 23, y = 25, weight = 601, height = 341)
      val knowledgeParaD = Knowledge(paraphraseD, lang, extentInfoJson = "{}", knowledgeForImages=List(uploadImage(knowledgeForImageParaD, transversalState)))

      val paraphraseE = "証拠サンプルが一つあります。"
      val referenceParaE = Reference(url = "", surface = "証拠サンプルが", surfaceIndex = 0, isWholeSentence = false,
            originalUrlOrReference = "https://www.e-stat.go.jp/stat-search/file-download?statInfId=000001086170&fileKind=0")  
      val tableReferenceParaE = TableReference(referenceParaE, skipHeaderRows=5, skipRowList=List(),multiHeaderRows=4, sheetNameForExcel= "se0101")
      val knowledgeForTableParaE = KnowledgeForTable(getUUID(), tableReferenceParaE)
      val knowledgeParaE = Knowledge(paraphraseE, lang, extentInfoJson = "{}", knowledgeForTables=List(uploadTable(knowledgeForTableParaE, transversalState)))

      val paraphraseF = "一つの証拠サンプルの提出を厳格に義務付けます。"
      val referenceParaF = Reference(url = "", surface = "", surfaceIndex = -1, isWholeSentence = false,
        originalUrlOrReference = "https://www.e-stat.go.jp/stat-search/file-download?statInfId=000040292480&fileKind=1")
      val tableReferenceParaF = TableReference(referenceParaF, skipHeaderRows=8, skipRowList=List(),multiHeaderRows=1, sheetNameForExcel= "")
      val knowledgeForTableParaF = KnowledgeForTable(getUUID(), tableReferenceParaF)  
      val knowledgeParaF = Knowledge(paraphraseF, lang, extentInfoJson = "{}", knowledgeForTables=List(uploadTable(knowledgeForTableParaF, transversalState)))

      val propositionIdForInference = getUUID()  

      val premiseKnowledge = List.empty[KnowledgeForParser]
      val claimKnowledge1 = List(
        KnowledgeForParser(propositionIdForInference, getUUID(), knowledgeParaA),
        KnowledgeForParser(propositionIdForInference, getUUID(), knowledgeParaB),
        KnowledgeForParser(propositionIdForInference, getUUID(), knowledgeParaC),
        KnowledgeForParser(propositionIdForInference, getUUID(), knowledgeParaE)
        )
      val claimKnowledge2 = List(
        KnowledgeForParser(propositionIdForInference, getUUID(), knowledgeParaD),
        KnowledgeForParser(propositionIdForInference, getUUID(), knowledgeParaF)
      )
      val inputSentenceForParser1 = InputSentenceForParser(premiseKnowledge, claimKnowledge1, ActionModeType.DEDUCTION_MODE.index)
      val json1 = getAnalyzedSentenceObjectsJson(lang,inputSentenceForParser1, transversalState)

      val inputSentenceForParser2 = InputSentenceForParser(premiseKnowledge, claimKnowledge2, ActionModeType.DEDUCTION_MODE.index)
      val json2 = getAnalyzedSentenceObjectsJsonForSemiGlobal(lang,inputSentenceForParser2, transversalState)

      val asos1 = Json.parse(json1).as[AnalyzedSentenceObjects]
      val asos2 = Json.parse(json2).as[AnalyzedSentenceObjects]


      val json = Json.toJson(AnalyzedSentenceObjects(asos1.analyzedSentenceObjects ::: asos2.analyzedSentenceObjects, asos1.deductionConfiguration)).toString()
      val fr = FakeRequest(POST, "/executeDeduction")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalStateJson)
        .withJsonBody(Json.parse(json))

      val result = call(controller.executeDeduction(), fr)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")

      val jsonResult = contentAsJson(result).toString()
      val analyzedSentenceObjects: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      val targetAsos = analyzedSentenceObjects.analyzedSentenceObjects.filter(x => x.knowledgeBaseSemiGlobalNode.sentenceType.equals(SentenceType.CLAIM.index))
      val coveredPropositionEdgeSize = targetAsos.foldLeft(0) { (acc, x) =>
          acc + x.deductionResult.coveredPropositionEdges.size
      }
      

      val actualEdgeSize = targetAsos.foldLeft(0) { (acc, x) => acc + x.edgeList.size }

      assert(analyzedSentenceObjects.analyzedSentenceObjects.size == 6)
      assert(targetAsos.filter(x => x.deductionResult.status).size == 6)
      assert(actualEdgeSize == coveredPropositionEdgeSize)
      assert(targetAsos.filter(x => x.deductionResult.evidenceKnowledgeList.filter(x => x.deductionUnits.contains("ClauseBaseMatch")).size > 0).size > 0)
      assert(targetAsos.filter(x => x.deductionResult.evidenceKnowledgeList.filter(x => x.deductionUnits.contains("ClauseSynonymMatch")).size > 0).size > 0)
      assert(targetAsos.filter(x => x.deductionResult.evidenceKnowledgeList.filter(x => x.deductionUnits.contains("ClauseImageMatch")).size > 0).size > 0)
      assert(targetAsos.filter(x => x.deductionResult.evidenceKnowledgeList.filter(x => x.deductionUnits.contains("ClauseTableMatch")).size > 0).size > 0)
      assert(targetAsos.filter(x => x.deductionResult.evidenceKnowledgeList.filter(x => x.deductionUnits.contains("EmbeddingSentenceMatch")).size > 0).size > 0)
      assert(targetAsos.filter(x => x.deductionResult.evidenceKnowledgeList.filter(x => x.deductionUnits.contains("EmbeddingWholeSentenceImageMatch")).size > 0).size > 0)
      assert(targetAsos.filter(x => x.deductionResult.evidenceKnowledgeList.filter(x => x.deductionUnits.contains("EmbeddingWholeSentenceTableMatch")).size > 0).size > 0)

    }
  }


}