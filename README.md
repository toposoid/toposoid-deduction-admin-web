# toposoid-deduction-admin-web
This is a WEB API that works as a microservice within the Toposoid project.
Toposoid is a knowledge base construction platform.(see [Toposoid　Root Project](https://github.com/toposoid/toposoid.git))
This microservice provides the ability to manage multiple deductive inference logic to register, update microservices.

[![Unit Test And Build Image Action](https://github.com/toposoid/toposoid-deduction-admin-web/actions/workflows/action.yml/badge.svg?branch=main)](https://github.com/toposoid/toposoid-deduction-admin-web/actions/workflows/action.yml)

The functions of this microservice are in the red frame below.
<img width="1136" alt="" src="https://user-images.githubusercontent.com/82787843/169642794-6cf4c50f-a7e5-4688-bf3e-9994dfbedbc8.png">

- API Image
<img width="1201" alt="2021-10-01 20 31 35" src="https://user-images.githubusercontent.com/82787843/135613475-1431ddaa-5a4c-4f82-8346-51b41987da2e.png">

<img width="1204" alt="2021-10-01 20 33 42" src="https://user-images.githubusercontent.com/82787843/135613393-dd124dc0-0f81-4839-85d2-10ba2e9d2281.png">

## Requirements
* Docker version 20.10.x, or later
* docker-compose version 1.22.x

### Memory requirements
* Required: at least 8GB of RAM (The maximum heap memory size of the JVM is set to 6G (Application: 4G, Neo4J: 2G))
* Required: 50G or higher　of HDD

## Setup
```bssh
docker-compose up -d
```
It takes more than 20 minutes to pull the Docker image for the first time.
## Usage
```bash
curl -X POST -H "Content-Type: application/json" -d '{
    "index": 0,   
    "function":{      
        "host": "127.0.0.1",
        "port": "9101",
        "sentenceType": 0        
    }
}' http://localhost:9003/changeEndPoints

curl -X POST -H "Content-Type: application/json" -d '{
    "analyzedSentenceObjects": [
        {
            "nodeMap": {
                "21687cd8-437b-48c6-bd21-210f67cc5e07-3": {
                    "nodeId": "21687cd8-437b-48c6-bd21-210f67cc5e07-3",
                    "propositionId": "21687cd8-437b-48c6-bd21-210f67cc5e07",
                    "currentId": 3,
                    "parentId": -1,
                    "isMainSection": true,
                    "surface": "した。",
                    "normalizedName": "する",
                    "dependType": "D",
                    "caseType": "文末",
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
                    "isDenialWord": false,
                    "isConditionalConnection": false,
                    "normalizedNameYomi": "する",
                    "surfaceYomi": "した。",
                    "modalityType": "-",
                    "logicType": "-",
                    "nodeType": 1,
                    "lang": "ja_JP",
                    "extentText": "{}"
                },
                "21687cd8-437b-48c6-bd21-210f67cc5e07-2": {
                    "nodeId": "21687cd8-437b-48c6-bd21-210f67cc5e07-2",
                    "propositionId": "21687cd8-437b-48c6-bd21-210f67cc5e07",
                    "currentId": 2,
                    "parentId": 3,
                    "isMainSection": false,
                    "surface": "発案を",
                    "normalizedName": "発案",
                    "dependType": "D",
                    "caseType": "ヲ格",
                    "namedEntity": "",
                    "rangeExpressions": {
                        "": {}
                    },
                    "categories": {
                        "発案": "抽象物"
                    },
                    "domains": {
                        "": ""
                    },
                    "isDenialWord": false,
                    "isConditionalConnection": false,
                    "normalizedNameYomi": "はつあん",
                    "surfaceYomi": "はつあんを",
                    "modalityType": "-",
                    "logicType": "-",
                    "nodeType": 1,
                    "lang": "ja_JP",
                    "extentText": "{}"
                },
                "21687cd8-437b-48c6-bd21-210f67cc5e07-1": {
                    "nodeId": "21687cd8-437b-48c6-bd21-210f67cc5e07-1",
                    "propositionId": "21687cd8-437b-48c6-bd21-210f67cc5e07",
                    "currentId": 1,
                    "parentId": 2,
                    "isMainSection": false,
                    "surface": "秀逸な",
                    "normalizedName": "秀逸だ",
                    "dependType": "D",
                    "caseType": "連格",
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
                    "isDenialWord": false,
                    "isConditionalConnection": false,
                    "normalizedNameYomi": "しゅういつだ",
                    "surfaceYomi": "しゅういつな",
                    "modalityType": "-",
                    "logicType": "-",
                    "nodeType": 1,
                    "lang": "ja_JP",
                    "extentText": "{}"
                },
                "21687cd8-437b-48c6-bd21-210f67cc5e07-0": {
                    "nodeId": "21687cd8-437b-48c6-bd21-210f67cc5e07-0",
                    "propositionId": "21687cd8-437b-48c6-bd21-210f67cc5e07",
                    "currentId": 0,
                    "parentId": 3,
                    "isMainSection": false,
                    "surface": "太郎は",
                    "normalizedName": "太郎",
                    "dependType": "D",
                    "caseType": "未格",
                    "namedEntity": "PERSON:太郎",
                    "rangeExpressions": {
                        "": {}
                    },
                    "categories": {
                        "": ""
                    },
                    "domains": {
                        "": ""
                    },
                    "isDenialWord": false,
                    "isConditionalConnection": false,
                    "normalizedNameYomi": "たろう",
                    "surfaceYomi": "たろうは",
                    "modalityType": "-",
                    "logicType": "-",
                    "nodeType": 1,
                    "lang": "ja_JP",
                    "extentText": "{}"
                }
            },
            "edgeList": [
                {
                    "sourceId": "21687cd8-437b-48c6-bd21-210f67cc5e07-2",
                    "destinationId": "21687cd8-437b-48c6-bd21-210f67cc5e07-3",
                    "caseStr": "ヲ格",
                    "dependType": "D",
                    "logicType": "-",
                    "lang": "ja_JP"
                },
                {
                    "sourceId": "21687cd8-437b-48c6-bd21-210f67cc5e07-1",
                    "destinationId": "21687cd8-437b-48c6-bd21-210f67cc5e07-2",
                    "caseStr": "連格",
                    "dependType": "D",
                    "logicType": "-",
                    "lang": "ja_JP"
                },
                {
                    "sourceId": "21687cd8-437b-48c6-bd21-210f67cc5e07-0",
                    "destinationId": "21687cd8-437b-48c6-bd21-210f67cc5e07-3",
                    "caseStr": "未格",
                    "dependType": "D",
                    "logicType": "-",
                    "lang": "ja_JP"
                }
            ],
            "sentenceType": 1,
            "deductionResultMap": {
                "0": {
                    "status": false,
                    "matchedPropositionIds": [],
                    "deductionUnit": ""
                },
                "1": {
                    "status": false,
                    "matchedPropositionIds": [],
                    "deductionUnit": ""
                }
            }
        }
    ]
}' http://localhost:9003/executeDeduction
```

# Note

## License
toposoid/toposoid-deduction-admin-web is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

## Author
* Makoto Kubodera([Linked Ideal LLC.](https://linked-ideal.com/))

Thank you!
