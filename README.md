# toposoid-deduction-admin-web
This is a WEB API that works as a microservice within the Toposoid project.
Toposoid is a knowledge base construction platform.(see [Toposoid　Root Project](https://github.com/toposoid/toposoid.git))
This microservice provides the ability to manage multiple deductive inference logic to register, update microservices.

[![Unit Test And Build Image Action](https://github.com/toposoid/toposoid-deduction-admin-web/actions/workflows/action.yml/badge.svg?branch=main)](https://github.com/toposoid/toposoid-deduction-admin-web/actions/workflows/action.yml)

The functions of this microservice are in the red frame below.
<img width="1136" alt="" src="https://user-images.githubusercontent.com/82787843/169642794-6cf4c50f-a7e5-4688-bf3e-9994dfbedbc8.png">

* API Image
  * Input
  * <img width="1026" src="https://github.com/toposoid/toposoid-deduction-admin-web/assets/82787843/de405363-5823-477c-b327-14f2ebf49116">
  * Output
  * <img width="1022" src="https://github.com/toposoid/toposoid-deduction-admin-web/assets/82787843/28d7a331-61fb-4f7c-a4e7-24047a71ae06">


## Requirements
* Docker version 20.10.x, or later
* docker-compose version 1.22.x

### Recommended Environment For Standalone
* Required: at least 16GB of RAM
* Required: 50G or higher　of HDD
* Please understand that since we are dealing with large models such as LLM, the Dockerfile size is large and the required machine SPEC is high.

## Setup For Standalone
```bssh
docker-compose up
```
* The first startup takes a long time until docker pull finishes.

## Usage
```bash
# Please refer to the following for information on registering data to try deduction.
# ref. https://github.com/toposoid/toposoid-knowledge-register-web
#for example
curl -X POST -H "Content-Type: application/json" -d '{
    "premiseList": [],
    "premiseLogicRelation": [],
    "claimList": [
        {
            "sentence": "猫が２匹います。",
            "lang": "ja_JP",
            "extentInfoJson": "{}",
            "isNegativeSentence": false,
            "knowledgeForImages":[{
                "id": "",
                "imageReference": {
                    "reference": {
                        "url": "",
                        "surface": "猫が",
                        "surfaceIndex": 0,
                        "isWholeSentence": false,
                        "originalUrlOrReference": "http://images.cocodataset.org/val2017/000000039769.jpg"},
                    "x": 11,
                    "y": 11,
                    "width": 466,
                    "height": 310
                }
            }]
        }
    ],
    "claimLogicRelation": [
    ]
}' http://localhost:9002/regist

curl -X POST -H "Content-Type: application/json" -d '{
    "index": 0,   
    "function":{      
        "host": "localhost",
        "port": "9101",
        "sentenceType": 0        
    }
}' http://localhost:9003/changeEndPoints

curl -X POST -H "Content-Type: application/json" -d '{
    "analyzedSentenceObjects": [
        {
            "nodeMap": {
                "31bd069d-6b34-48fc-a687-9e8b5c91e61b-2": {
                    "nodeId": "31bd069d-6b34-48fc-a687-9e8b5c91e61b-2",
                    "propositionId": "da9e9a6a-6e09-43e8-9e85-7be71a5e3e5b",
                    "sentenceId": "31bd069d-6b34-48fc-a687-9e8b5c91e61b",
                    "predicateArgumentStructure": {
                        "currentId": 2,
                        "parentId": -1,
                        "isMainSection": true,
                        "surface": "います。",
                        "normalizedName": "射る",
                        "dependType": "D",
                        "caseType": "文末",
                        "isDenialWord": false,
                        "isConditionalConnection": false,
                        "normalizedNameYomi": "いる?居る",
                        "surfaceYomi": "います。",
                        "modalityType": "-",
                        "parallelType": "-",
                        "nodeType": 1,
                        "morphemes": [
                            "動詞,*,母音動詞,基本連用形",
                            "接尾辞,動詞性接尾辞,動詞性接尾辞ます型,基本形",
                            "特殊,句点,*,*"
                        ]
                    },
                    "localContext": {
                        "lang": "ja_JP",
                        "namedEntity": "",
                        "rangeExpressions": {
                            "": {}
                        },
                        "categories": {
                            "": ""
                        },
                        "domains": {
                            "": ""
                        },
                        "knowledgeFeatureReferences": []
                    }
                },
                "31bd069d-6b34-48fc-a687-9e8b5c91e61b-1": {
                    "nodeId": "31bd069d-6b34-48fc-a687-9e8b5c91e61b-1",
                    "propositionId": "da9e9a6a-6e09-43e8-9e85-7be71a5e3e5b",
                    "sentenceId": "31bd069d-6b34-48fc-a687-9e8b5c91e61b",
                    "predicateArgumentStructure": {
                        "currentId": 1,
                        "parentId": 2,
                        "isMainSection": false,
                        "surface": "２匹",
                        "normalizedName": "２",
                        "dependType": "D",
                        "caseType": "無格",
                        "isDenialWord": false,
                        "isConditionalConnection": false,
                        "normalizedNameYomi": "にひき",
                        "surfaceYomi": "にひき",
                        "modalityType": "-",
                        "parallelType": "-",
                        "nodeType": 1,
                        "morphemes": [
                            "名詞,数詞,*,*",
                            "接尾辞,名詞性名詞助数辞,*,*"
                        ]
                    },
                    "localContext": {
                        "lang": "ja_JP",
                        "namedEntity": "",
                        "rangeExpressions": {
                            "２": {
                                "prefix": "",
                                "quantity": "2",
                                "unit": "匹",
                                "range": "2"
                            }
                        },
                        "categories": {
                            "２": "数量"
                        },
                        "domains": {
                            "": ""
                        },
                        "knowledgeFeatureReferences": []
                    }
                },
                "31bd069d-6b34-48fc-a687-9e8b5c91e61b-0": {
                    "nodeId": "31bd069d-6b34-48fc-a687-9e8b5c91e61b-0",
                    "propositionId": "da9e9a6a-6e09-43e8-9e85-7be71a5e3e5b",
                    "sentenceId": "31bd069d-6b34-48fc-a687-9e8b5c91e61b",
                    "predicateArgumentStructure": {
                        "currentId": 0,
                        "parentId": 2,
                        "isMainSection": false,
                        "surface": "ペットが",
                        "normalizedName": "ペット",
                        "dependType": "D",
                        "caseType": "ガ格",
                        "isDenialWord": false,
                        "isConditionalConnection": false,
                        "normalizedNameYomi": "ぺっと",
                        "surfaceYomi": "ぺっとが",
                        "modalityType": "-",
                        "parallelType": "-",
                        "nodeType": 1,
                        "morphemes": [
                            "名詞,普通名詞,*,*",
                            "助詞,格助詞,*,*"
                        ]
                    },
                    "localContext": {
                        "lang": "ja_JP",
                        "namedEntity": "",
                        "rangeExpressions": {
                            "": {}
                        },
                        "categories": {
                            "ペット": "動物"
                        },
                        "domains": {
                            "ペット": "家庭・暮らし"
                        },
                        "knowledgeFeatureReferences": [
                            {
                                "propositionId": "da9e9a6a-6e09-43e8-9e85-7be71a5e3e5b",
                                "sentenceId": "31bd069d-6b34-48fc-a687-9e8b5c91e61b",
                                "featureId": "4254cb90-e53c-46d5-9aba-6c48897caeaf",
                                "featureType": 1,
                                "url": "http://toposoid-contents-admin-web:9012/contents/images/92025d59-3c3b-4d8f-9ec6-13ef38574281.jpeg",
                                "source": "http://images.cocodataset.org/val2017/000000039769.jpg",
                                "featureInputType": 0,
                                "extentText": "{}"
                            }
                        ]
                    }
                }
            },
            "edgeList": [
                {
                    "sourceId": "31bd069d-6b34-48fc-a687-9e8b5c91e61b-1",
                    "destinationId": "31bd069d-6b34-48fc-a687-9e8b5c91e61b-2",
                    "caseStr": "無格",
                    "dependType": "D",
                    "parallelType": "-",
                    "hasInclusion": false,
                    "logicType": "-"
                },
                {
                    "sourceId": "31bd069d-6b34-48fc-a687-9e8b5c91e61b-0",
                    "destinationId": "31bd069d-6b34-48fc-a687-9e8b5c91e61b-2",
                    "caseStr": "ガ格",
                    "dependType": "D",
                    "parallelType": "-",
                    "hasInclusion": false,
                    "logicType": "-"
                }
            ],
            "knowledgeBaseSemiGlobalNode": {
                "nodeId": "31bd069d-6b34-48fc-a687-9e8b5c91e61b",
                "propositionId": "da9e9a6a-6e09-43e8-9e85-7be71a5e3e5b",
                "sentenceId": "31bd069d-6b34-48fc-a687-9e8b5c91e61b",
                "sentence": "ペットが２匹います。",
                "sentenceType": 1,
                "localContextForFeature": {
                    "lang": "ja_JP",
                    "knowledgeFeatureReferences": []
                }
            },
            "deductionResult": {
                "status": false,
                "coveredPropositionResults": [],
                "havePremiseInGivenProposition": false
            }
        }
    ]
}' http://localhost:9003/executeDeduction
```

## Json details

* AnalyzedSentenceObjects

| name | type                         | explanation   |
| ------------- |------------------------------|---------------|
| analyzedSentenceObjects | List[AnalyzedSentenceObject] | see AnalyzedSentenceObject |


* AnalyzedSentenceObject

| name | type                           | explanation                                     |
| ----------- |--------------------------------|-------------------------------------------------|
| nodeMap | Map[String, KnowledgeBaseNode] | key:Node Identifier value:see KnowledgeBaseNode |
|edgeList| List[KnowledgeBaseEdge]        | see KnowledgeBaseEdge                           |
|knowledgeBaseSemiGlobalNode| KnowledgeBaseSemiGlobalNode| see KnowledgeBaseSemiGlobalNode                 |
|deductionResult|DeductionResult| see DeductionResult                             |

* KnowledgeBaseNode

| name | type                           | explanation            |
| ----------- |--------------------------------|------------------------|
|nodeId| String                         | Node Identifier        |
|propositionId| String                         | Proposition Identifier |
|sentenceId| Stirng                         | Sentence  Identifier   |
|predicateArgumentStructure| see PredicateArgumentStructure |
|localContext| LocalContext| see LocalContext       |

* PredicateArgumentStructure

| name | type    | explanation                                                                          |
| ----------- |---------|--------------------------------------------------------------------------------------|
|currentId| Int     | ID that identifies which clause of the sentence                                      |
|parentId| Int     | ID of the clause to which the relevant clause relates                                |
|isMainSection| Boolean | Flag indicating end of sentence true/false                                           |
|surface| String  | expression of clauses                                                                |
|normalizedName| String  | Normalized expression of clauses                                                     |
|dependType| String  | Relationship with parent clause Dependency relationship: D, Parallel relationship: P |
|caseType| String  | Clause case information                                                              |
|isDenialWord| Boolean | Flag representing negative expression true/false                                     |
|isConditionalConnection| Boolean | Flags representing conditional clauses and similar clauses                           |
|surfaceYomi| String  | Pronunciation of normalized expression of clauses                                    |
|modalityType| String  | Type of modality                                                                     |
|parallelType| String  | Type of  parallel                                                                            |
|nodeType| Int     |   com.ideal.linked.toposoid.common.SentenceType                                                                                   |
|morphemes|  List[String]       |  Morphological analysis results                                                                                    |

* localContext

| name | type    | explanation                                           |
| ----------- |---------|-------------------------------------------------------|
|lang|String| language locale                                       |
|namedEntity|String| named entity                                          |
|rangeExpressions| Map[String, Map[String, String]| uantity range representation                          |
|categories|Map[String, String]| Category (For Japanese, see the KNP feature category) |
|domains|Map[String, String]| Domain (For Japanese, refer to KNP's feature domain)  |
|knowledgeFeatureReferences|List[KnowledgeFeatureReference]| see  KnowledgeFeatureReference |


* KnowledgeFeatureReference

| name | type   | explanation                                  |
| ----------- |--------|----------------------------------------------|
|propositionId| String | Proposition Identifier                       |
|sentenceId| String | Sentence  Identifier                         |
|featureId| String | Feature  Identifier                          |
|featureType| Int    | com.ideal.linked.toposoid.common。FeatureType |
|url| String | Feature information URL                      |
|source| String | Feature informatio　Source                    |
|featureInputType| Int    | com.ideal.linked.toposoid.common.DataEntryType            |
|extentText| String | Default:{}                                   |


* KnowledgeBaseEdge

| name | type    | explanation                                                                    |
| ----------- |---------|--------------------------------------------------------------------------------|
|sourceId| String  | ID that identifies the child of the dependency in the relation between clauses |
|destinationId| String  | ID that identifies the dependent parent in the relation between clauses        |
|caseStr| String  | Relations between clauses (case structure, etc.)                               |
|dependType| String  | ref. KnowledgeBaseNode's dependType                                            |
|parallelType| String  | ref. KnowledgeBaseNode's　parallelType                                          |
|hasInclusion| Boolean | Presence of inclusion relationship                                             |
|logicType| String  | ref. KnowledgeBaseNode's logicType                                             |


* KnowledgeBaseSemiGlobalNode

| name | type   | explanation                |
| ----------- |--------|----------------------------|
|propositionId| String | Proposition Identifier     |
|sentenceId| String | Sentence  Identifier       |
|sentence| String | sentence                   |
|sentenceType| Int    | 0:Premise, 1:Claim         |
|localContextForFeature|LocalContextForFeature| see LocalContextForFeature |


* LocalContextForFeature

| name | type   | explanation |
| ----------- |--------|-------------|
|lang| String | language locale  |
|knowledgeFeatureReferences|    List[KnowledgeFeatureReference]    | see  KnowledgeFeatureReference       |

* DeductionResult

| name | type                           | explanation |
| ----------- |--------------------------------|-------------|
|status| Boolean                        |             |
|coveredPropositionResults| List[CoveredPropositionResult] | see CoveredPropositionResult |
|havePremiseInGivenProposition| Boolean                        |             |

* CoveredPropositionResult

| name | type    | explanation                 |
| ----------- |---------|-----------------------------|
|deductionUnit| String  |                             |
|sentenceId| String  | Sentence  Identifier        |
|coveredPropositionEdges|  List[CoveredPropositionEdge]       | see  CoveredPropositionEdge |
|knowledgeBaseSideInfoList| List[KnowledgeBaseSideInfo]        | see     KnowledgeBaseSideInfo  |

* CoveredPropositionEdge

| name | type    | explanation                |
| ----------- |---------|----------------------------|
|sourceNode|CoveredPropositionNode| see CoveredPropositionNode |
|destinationNode|CoveredPropositionNode| see CoveredPropositionNode|

* CoveredPropositionNode

| name | type   | explanation                |
| ----------- |--------|----------------------------|
|terminalId| String ||
|terminalSurface|String||
|terminalUrl|String||

* KnowledgeBaseSideInfo

| name | type   | explanation |
| ----------- |--------|-------------|
|propositionId| String |Proposition Identifier|
|sentenceId| String |   Sentence  Identifier          |
|featureInfoList|List[MatchedFeatureInfo]| see MatchedFeatureInfo        |

* MatchedFeatureInfo

| name | type   | explanation |
| ----------- |--------|-------------|
|featureId| String | Feature Identifier    |
|similarity| Float  |Similarity during matching|

# Note

## License
toposoid/toposoid-deduction-admin-web is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

## Author
* Makoto Kubodera([Linked Ideal LLC.](https://linked-ideal.com/))

Thank you!
