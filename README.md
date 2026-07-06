# 🧠 SQL Copilot

Assistente inteligente para geração e explicação de consultas SQL utilizando LLMs.

Converte solicitações em linguagem natural para consultas SQL seguras, permitindo apenas operações de leitura (`SELECT`).

---

## ✨ Funcionalidades

- 🤖 Gerar consultas SQL a partir de linguagem natural
- 📖 Explicar consultas SQL existentes
- 🔒 Permitir apenas consultas `SELECT`
- 🛡️ Bloquear prompts potencialmente maliciosos
- 🚦 Limitar requisições por IP (Rate Limit)
- 🔁 Utilizar Circuit Breaker para chamadas à LLM
- ⚡ Cache em memória para otimizar Rate Limit
- 💾 Cache de respostas para prompts idênticos (evita chamadas desnecessárias à LLM)
- 🌐 Interface Web com Thymeleaf
- 📄 Documentação interativa via Swagger UI (SpringDoc OpenAPI)

---

# 🚀 Tecnologias

| Tecnologia | Versão |
|------------|---------|
| Java | 21 |
| Spring Boot | 3.5.16 |
| Spring Cloud OpenFeign | 2025.0.3 |

---

# 📚 Bibliotecas

| Biblioteca | Finalidade |
|------------|------------|
| spring-boot-starter-web | API REST |
| spring-boot-starter-validation | Validação de DTOs |
| spring-boot-starter-thymeleaf | Interface Web |
| spring-boot-starter-aop | Suporte ao Resilience4j |
| spring-boot-starter-cache | Abstração de cache do Spring |
| spring-cloud-starter-openfeign | Comunicação com a LLM |
| resilience4j-spring-boot3 | Circuit Breaker |
| bucket4j-core | Rate Limiting |
| caffeine | Cache em memória (Rate Limit e prompts) |
| jsqlparser | Validação sintática de SQL |
| lombok | Redução de boilerplate |
| springdoc-openapi-starter-webmvc-ui | Documentação interativa (Swagger UI) |

---

# 💡 Visão Geral

O SQL Copilot funciona como uma camada entre o usuário e o banco de dados.

Ao invés de escrever SQL manualmente, o usuário descreve a consulta desejada em linguagem natural. A aplicação utiliza uma LLM para gerar a consulta SQL correspondente e aplica diversas validações antes de devolvê-la.

O principal objetivo é oferecer **segurança por padrão**, impedindo que comandos destrutivos sejam gerados.

---

# 🤖 Modelo de IA

O projeto utiliza o **OpenRouter** como gateway para acesso a modelos de linguagem (LLMs).

Atualmente, o SQL Copilot utiliza o seguinte modelo:

| Modelo | Identificador | Descrição |
|---------|---------------|-----------|
| **Poolside: Laguna XS.2 (free)** | `poolside/laguna-xs.2:free` | Modelo de alto desempenho voltado para workloads agentic, com suporte nativo a uso de ferramentas (tool use), contexto longo (*long context*), geração de código, automação de fluxos e execução de instruções complexas. |

> **Observação:** de acordo com o provedor, prompts e respostas podem ser registrados e utilizados para aprimorar o modelo.

Mais informações sobre os modelos disponíveis no OpenRouter: https://openrouter.ai/models

---

# 🏗️ Arquitetura

```text
src
├── controller
│   ├── ChatController
│   └── ViewController
│
├── documentation
│   └── ChatControllerDoc
│
├── service
│   ├── ChatService
│   ├── ActionRouter
│   └── SchemaIntrospectionService
│
├── client
│   └── feign
│       └── LLMClient
│
├── filter
│   └── RateLimitFilter
│
├── dto
├── enums
├── exception
│
└── shared
    ├── config
    └── utils
        ├── PromptGuardUtils
        └── SqlValidatorUtils
```

---

# 🔒 Camadas de Segurança

## 1. Rate Limit

Limita cada endereço IP a **10 requisições por minuto**.

---

## 2. Prompt Guard

Bloqueia solicitações com intenção destrutiva antes que sejam enviadas à LLM.

Exemplos:

- DELETE
- DROP
- UPDATE
- INSERT
- ALTER
- TRUNCATE
- Apagar tabela
- Remover registros

---

## 3. SQL Validator

Toda resposta gerada pela LLM é validada utilizando o **JSQLParser**.

A aplicação rejeita consultas que:

- Não sejam `SELECT`;
- Possuam múltiplas instruções;
- Contenham comandos proibidos.

---

## 4. Circuit Breaker

Protege a aplicação caso a LLM esteja indisponível ou lenta, evitando chamadas repetidas.

---

## 5. Cache de Prompts

Prompts idênticos retornam a resposta diretamente do cache em memória (Caffeine), **sem realizar uma nova chamada à LLM**.

Isso reduz latência, economiza tokens e protege contra uso repetitivo da API.

---

# ⚙️ Configuração

Copie o arquivo de exemplo:

```bash
cp env.example .env
```

Configure as seguintes variáveis:

| Variável | Descrição |
|----------|-----------|
| OPENROUTER_API_KEY | Chave da API do OpenRouter |
| LLM_MODEL | Modelo utilizado pela aplicação |
| SSE_THREAD_POOL_SIZE| Tamanho do pool de threads para SSE (Server-Sent Events) |

Exemplo:

```env
OPENROUTER_API_KEY=xxxxxxxxxxxxxxxxxxxxxxxx

LLM_MODEL=xxxxxxxxxxxxxxxxxxxxxxxx

SSE_THREAD_POOL_SIZE=xx
```

---

# ▶️ Executando Docker 🐳

## Pré-requisitos 

- [Docker](https://www.docker.com/) instalado 

## Comandos

| Comando                        | Descrição                         |
|--------------------------------|-----------------------------------|
| `docker-compose up --build -d` | Build da imagem e sobe o container |
| `docker-compose up -d`         | Sobe o container sem rebuild      |
| `docker-compose down`          | Para e remove o container         |
| `docker-compose logs -f`       | Exibe os logs do container        |

Após subir, acesse: `http://localhost:8080/chat`

---

# 📄 Documentação da API (Swagger UI)

A aplicação utiliza o **SpringDoc OpenAPI 2.8.16** para gerar automaticamente a documentação interativa dos endpoints.

| Interface | URL |
|-----------|-----|
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| OpenAPI YAML | `http://localhost:8080/v3/api-docs.yaml` |

## Como acessar

1. Suba a aplicação (via Docker ou localmente)
2. Acesse `http://localhost:8080/swagger-ui/index.html` no navegador
3. Explore e teste os endpoints diretamente pela interface

## Estrutura da documentação

A documentação dos endpoints é separada da implementação por meio de interfaces na pasta `documentation/`:

```text
src/main/java/com/api/sqlcopilot/documentation/
└── ChatControllerDoc.java    ← Contrato com anotações @Operation, @ApiResponses e @Tag
```

O `ChatController` implementa `ChatControllerDoc`, mantendo o código limpo e a documentação isolada.

---

# 📡 API

## POST `/api/chat`

Gera ou explica uma consulta SQL.

### Requisição

```json
{
  "action": "GENERATE",
  "message": "Liste os 10 clientes com maior saldo"
}
```

### Resposta

```json
{
  "response": "SELECT * FROM clientes ORDER BY saldo DESC LIMIT 10;"
}
```

---

## POST `/api/chat/stream`

Processa a mensagem e retorna os eventos em tempo real via **Server-Sent Events (SSE)**.

### Requisição

```json
{
  "action": "GENERATE",
  "message": "Liste os 10 clientes com maior saldo"
}
```

### Resposta

Fluxo de eventos SSE (`text/event-stream`) com os chunks da resposta sendo enviados progressivamente.

---

# 📖 Ações Disponíveis

| Ação | Status | Descrição |
|-------|--------|-----------|
| `GENERATE` | ✅ Disponível | Gera uma consulta SQL a partir de uma solicitação em linguagem natural. |
| `EXPLAIN` | ⏸️ Desativada | Funcionalidade temporariamente desabilitada. |

> **Observação:** A ação **`EXPLAIN`** foi desativada para reduzir o consumo de tokens da LLM. Como este é um projeto desenvolvido para fins de estudo e utiliza um modelo com limite de uso, foi priorizada a funcionalidade **`GENERATE`**, que demanda significativamente menos tokens. Essa decisão permite manter a aplicação disponível por mais tempo sem custos adicionais.

---

# 🗂️ Schema do Banco

O contexto enviado para a LLM é obtido do arquivo:

```text
src/main/resources/schema/schema.md
```

Nesse arquivo devem estar documentadas as tabelas, colunas e relacionamentos do banco de dados.

---

# 🎯 Objetivos

- Demonstrar integração entre Spring Boot e LLMs.
- Gerar consultas SQL seguras.
- Aplicar técnicas de Prompt Engineering.
- Utilizar arquitetura em camadas.
- Implementar mecanismos de segurança e resiliência.

---

# 📄 Licença

Este projeto foi desenvolvido para fins de estudo e demonstração.