# toposoid-deduction-admin-web
This is a WEB API that works as a microservice within the Toposoid project.
Toposoid is a knowledge base construction platform.(see [Toposoid　Root Project](https://github.com/toposoid/toposoid.git))
This microservice provides the ability to manage multiple deductive inference logic to register, update microservices.

[![Unit Test And Build Image Action](https://github.com/toposoid/toposoid-deduction-admin-web/actions/workflows/action.yml/badge.svg?branch=main)](https://github.com/toposoid/toposoid-deduction-admin-web/actions/workflows/action.yml)

The functions of this microservice are in the red frame below.
<img width="1136" alt="" src="https://user-images.githubusercontent.com/82787843/169642794-6cf4c50f-a7e5-4688-bf3e-9994dfbedbc8.png">

- API Image
<img width="1201" src="https://user-images.githubusercontent.com/82787843/135613475-1431ddaa-5a4c-4f82-8346-51b41987da2e.png">

<img width="1201"  src="https://user-images.githubusercontent.com/82787843/212541658-9f73894d-ab76-4b7c-a518-1772e712e232.png">


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
If vald does not start due to an error, commenting out the following part in docker-compose.yml may work.
```yml
  vald:
    image: vdaas/vald-agent-ngt:v1.6.3
    #user: 1000:1000
    volumes:
      - ./vald-config:/etc/server
      #- /etc/passwd:/etc/passwd:ro
      #- /etc/group:/etc/group:ro
    networks:
      app_net:
        ipv4_address: 172.30.0.10
    ports:
      - 8081:8081
```

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
                "4df8a375-189c-46cb-b8b7-cd4f47fe285f-0": {
                    "nodeId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-0",
                    "propositionId": "e171b187-75a9-4851-beb9-5463ed95d8c4",
                    "currentId": 0,
                    "parentId": 1,
                    "isMainSection": false,
                    "surface": "自然界の",
                    "normalizedName": "自然",
                    "dependType": "D",
                    "caseType": "ノ格",
                    "namedEntity": "",
                    "rangeExpressions": {
                        "": {}
                    },
                    "categories": {
                        "": "",
                        "界": "抽象物"
                    },
                    "domains": {
                        "": ""
                    },
                    "isDenialWord": false,
                    "isConditionalConnection": false,
                    "normalizedNameYomi": "しぜんかい?境",
                    "surfaceYomi": "しぜんかいの",
                    "modalityType": "-",
                    "logicType": "-",
                    "nodeType": 1,
                    "lang": "ja_JP",
                    "extentText": "{}"
                },
                "4df8a375-189c-46cb-b8b7-cd4f47fe285f-1": {
                    "nodeId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-1",
                    "propositionId": "e171b187-75a9-4851-beb9-5463ed95d8c4",
                    "currentId": 1,
                    "parentId": 6,
                    "isMainSection": false,
                    "surface": "物理法則は",
                    "normalizedName": "物理",
                    "dependType": "D",
                    "caseType": "未格",
                    "namedEntity": "",
                    "rangeExpressions": {
                        "": {}
                    },
                    "categories": {
                        "物理": "抽象物",
                        "法則": "抽象物"
                    },
                    "domains": {
                        "物理": "教育・学習;科学・技術",
                        "": ""
                    },
                    "isDenialWord": false,
                    "isConditionalConnection": false,
                    "normalizedNameYomi": "ぶつりほうそく",
                    "surfaceYomi": "ぶつりほうそくは",
                    "modalityType": "-",
                    "logicType": "-",
                    "nodeType": 1,
                    "lang": "ja_JP",
                    "extentText": "{}"
                },
                "4df8a375-189c-46cb-b8b7-cd4f47fe285f-6": {
                    "nodeId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-6",
                    "propositionId": "e171b187-75a9-4851-beb9-5463ed95d8c4",
                    "currentId": 6,
                    "parentId": -1,
                    "isMainSection": true,
                    "surface": "成立する。",
                    "normalizedName": "成立",
                    "dependType": "D",
                    "caseType": "文末",
                    "namedEntity": "",
                    "rangeExpressions": {
                        "": {}
                    },
                    "categories": {
                        "成立": "抽象物"
                    },
                    "domains": {
                        "": ""
                    },
                    "isDenialWord": false,
                    "isConditionalConnection": false,
                    "normalizedNameYomi": "せいりつ",
                    "surfaceYomi": "せいりつする。",
                    "modalityType": "-",
                    "logicType": "-",
                    "nodeType": 1,
                    "lang": "ja_JP",
                    "extentText": "{}"
                },
                "4df8a375-189c-46cb-b8b7-cd4f47fe285f-4": {
                    "nodeId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-4",
                    "propositionId": "e171b187-75a9-4851-beb9-5463ed95d8c4",
                    "currentId": 4,
                    "parentId": 5,
                    "isMainSection": false,
                    "surface": "どの",
                    "normalizedName": "どの",
                    "dependType": "D",
                    "caseType": "連体",
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
                    "normalizedNameYomi": "どの",
                    "surfaceYomi": "どの",
                    "modalityType": "-",
                    "logicType": "-",
                    "nodeType": 1,
                    "lang": "ja_JP",
                    "extentText": "{}"
                },
                "4df8a375-189c-46cb-b8b7-cd4f47fe285f-2": {
                    "nodeId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-2",
                    "propositionId": "e171b187-75a9-4851-beb9-5463ed95d8c4",
                    "currentId": 2,
                    "parentId": 3,
                    "isMainSection": false,
                    "surface": "例外",
                    "normalizedName": "例外",
                    "dependType": "D",
                    "caseType": "未格",
                    "namedEntity": "",
                    "rangeExpressions": {
                        "": {}
                    },
                    "categories": {
                        "例外": "抽象物"
                    },
                    "domains": {
                        "": ""
                    },
                    "isDenialWord": false,
                    "isConditionalConnection": false,
                    "normalizedNameYomi": "れいがい",
                    "surfaceYomi": "れいがい",
                    "modalityType": "-",
                    "logicType": "-",
                    "nodeType": 1,
                    "lang": "ja_JP",
                    "extentText": "{}"
                },
                "4df8a375-189c-46cb-b8b7-cd4f47fe285f-5": {
                    "nodeId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-5",
                    "propositionId": "e171b187-75a9-4851-beb9-5463ed95d8c4",
                    "currentId": 5,
                    "parentId": 6,
                    "isMainSection": false,
                    "surface": "慣性系でも",
                    "normalizedName": "慣性",
                    "dependType": "D",
                    "caseType": "デ格",
                    "namedEntity": "",
                    "rangeExpressions": {
                        "": {}
                    },
                    "categories": {
                        "": "",
                        "系": "抽象物"
                    },
                    "domains": {
                        "": ""
                    },
                    "isDenialWord": false,
                    "isConditionalConnection": false,
                    "normalizedNameYomi": "かんせいけい",
                    "surfaceYomi": "かんせいけいでも",
                    "modalityType": "-",
                    "logicType": "AND",
                    "nodeType": 1,
                    "lang": "ja_JP",
                    "extentText": "{}"
                },
                "4df8a375-189c-46cb-b8b7-cd4f47fe285f-3": {
                    "nodeId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-3",
                    "propositionId": "e171b187-75a9-4851-beb9-5463ed95d8c4",
                    "currentId": 3,
                    "parentId": 6,
                    "isMainSection": false,
                    "surface": "なく",
                    "normalizedName": "無い",
                    "dependType": "D",
                    "caseType": "連用",
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
                    "normalizedNameYomi": "ない",
                    "surfaceYomi": "なく",
                    "modalityType": "-",
                    "logicType": "-",
                    "nodeType": 1,
                    "lang": "ja_JP",
                    "extentText": "{}"
                }
            },
            "edgeList": [
                {
                    "sourceId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-5",
                    "destinationId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-6",
                    "caseStr": "デ格",
                    "dependType": "D",
                    "logicType": "AND",
                    "lang": "ja_JP"
                },
                {
                    "sourceId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-4",
                    "destinationId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-5",
                    "caseStr": "連体",
                    "dependType": "D",
                    "logicType": "-",
                    "lang": "ja_JP"
                },
                {
                    "sourceId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-3",
                    "destinationId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-6",
                    "caseStr": "連用",
                    "dependType": "D",
                    "logicType": "-",
                    "lang": "ja_JP"
                },
                {
                    "sourceId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-2",
                    "destinationId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-3",
                    "caseStr": "未格",
                    "dependType": "D",
                    "logicType": "-",
                    "lang": "ja_JP"
                },
                {
                    "sourceId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-1",
                    "destinationId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-6",
                    "caseStr": "未格",
                    "dependType": "D",
                    "logicType": "-",
                    "lang": "ja_JP"
                },
                {
                    "sourceId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-0",
                    "destinationId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f-1",
                    "caseStr": "ノ格",
                    "dependType": "D",
                    "logicType": "-",
                    "lang": "ja_JP"
                }
            ],
            "sentenceType": 1,
            "sentenceId": "4df8a375-189c-46cb-b8b7-cd4f47fe285f",
            "lang": "ja_JP",
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
