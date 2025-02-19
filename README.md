# Chat-ollama 

## 介绍

随着AIGC的发展，AI chat模型的大火，带来的是大量的模型开源，ollama的出现，使得大模型的api调用更加的规范化，Chat-ollama是一个基于Ollama的聊天机器人demo，可以用来快速构建一个基于Ollama的聊天机器人。

因基于Ollama 需要自行搭建，对于Ollama的使用可以参考[Ollama官网](https://ollama.com/)

每个人的机器性能不同，建议根据实际情况选择不同的聊天模型



### 项目结构
项目中主要使用了langchain4j 与 spring-ai 2套框架进行搭建，如需自行搭中可按自己所需进行框架选择。
 
项目中的Rag 使用了redis，因此需要redis配置。但langchain4j 与 spring-ai 支持更多的向量数据库，如需更改可自行选择 

因为reis 7.0后的版本才支持RAG，使用时要使用7.0之后的版本，因为涉及到组件的问题，建议直接使用redis-stack

## 依赖

| 依赖 | 说明 | 版本 |
| --- | --- | --- |
| spring-boot-starter-web | web依赖 | 2.6.2 |
| spring-boot-starter-webflux | webflux依赖 | 2.6.2 |
| langchain4j-spring-boot-starter | langchain4j依赖 | 0.34.0 |



## Ollama

目前项目使用的Rag，如果不想使用Rag可以微调大模型，虽然微调模型可以使模型的垂直领域回答更加精准，但常规问题会降低，微调
建议使用unsloth进行模型微调，并生成ollama可读大模型，需要开发者学习python等。