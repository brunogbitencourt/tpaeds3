# Trabalho Prático de Algoritmos e Estrutura de Dados III

Autores: 
    Bruno Guimarães Bitencourt 
    Oscar Dias

# Documentação da API

## Visão Geral

Esta documentação descreve a API para o gerenciamento de filmes em formato binário. A API permite a criação, leitura, atualização e exclusão de filmes, além da conversão de arquivos CSV para um formato binário.

## Informações da API

- **Título:** Trabalho Prático - AEDS 3 - API
- **Versão:** 1.0
- **Descrição:** API para gerenciamento de filmes em formato binário.
- **Contato:**
  - **Nome:** Bruno Guimarães Bitencourt
  - **Email:** brunogbitencourt@hotmail.com
  - **Nome:** Oscar Dias
  - **Email:** oscardias@gmail.com
- **Licença:** Apache 2.0 ([Link](http://springdoc.org))

## Endpoints

### Converter CSV para Arquivo Binário

- **Método:** `POST`
- **Endpoint:** `/createDataBase`
- **Descrição:** Converte um arquivo CSV de filmes para um arquivo binário `.db`.
- **Parâmetros:**
  - **file** (multipart/form-data): Arquivo CSV contendo informações sobre filmes.
- **Respostas:**
  - **200 OK:** Arquivo binário criado com sucesso.
  - **400 Bad Request:** Arquivo CSV inválido.
  - **500 Internal Server Error:** Erro ao processar o arquivo.

### Obter Filme por ID

- **Método:** `GET`
- **Endpoint:** `/getMovie`
- **Descrição:** Retorna um filme específico com base no ID fornecido.
- **Parâmetros:**
  - **id** (query): ID do filme a ser obtido.
- **Respostas:**
  - **200 OK:** Filme encontrado.
  - **404 Not Found:** Filme não encontrado.
  - **500 Internal Server Error:** Erro ao obter o filme.

### Obter Filmes por IDs

- **Método:** `GET`
- **Endpoint:** `/getMoviesByIds`
- **Descrição:** Retorna uma lista de filmes com base em uma lista de IDs fornecidos. IDs não encontrados serão ignorados.
- **Parâmetros:**
  - **ids** (query): Lista de IDs dos filmes a serem obtidos.
- **Respostas:**
  - **200 OK:** Lista de filmes encontrada.
  - **404 Not Found:** Nenhum filme encontrado para os IDs fornecidos.
  - **500 Internal Server Error:** Erro ao obter a lista de filmes.

### Obter Todos os Filmes com Paginação

- **Método:** `GET`
- **Endpoint:** `/getAllMovie`
- **Descrição:** Retorna uma lista de filmes com base na paginação fornecida.
- **Parâmetros:**
  - **page** (query, opcional): Número da página para a paginação.
  - **size** (query, opcional): Número de filmes por página.
- **Respostas:**
  - **200 OK:** Lista de filmes retornada com sucesso.
  - **500 Internal Server Error:** Erro ao obter a lista de filmes.

### Criar Novo Filme

- **Método:** `POST`
- **Endpoint:** `/createMovie`
- **Descrição:** Adiciona um novo filme ao banco de dados.
- **Corpo da Requisição:**
  - **application/json:**
    ```json
    {
      "id": 1,
      "name": "Creed III",
      "date": "03/02/2023",
      "score": 73.0,
      "genre": ["Drama", "Action"],
      "overview": "After dominating the boxing world...",
      "crew": ["Michael B. Jordan", "Adonis Creed", "..."],
      "originTitle": "Creed III",
      "status": "Released",
      "originLang": "English",
      "budget": 75000000.0,
      "revenue": 271616668.0,
      "country": "AU"
    }
    ```
- **Respostas:**
  - **201 Created:** Filme criado com sucesso.
  - **400 Bad Request:** Dados do filme inválidos.
  - **500 Internal Server Error:** Erro ao criar o filme.

### Excluir Filme por ID

- **Método:** `DELETE`
- **Endpoint:** `/deleteMovie`
- **Descrição:** Exclui um filme específico com base no ID fornecido.
- **Parâmetros:**
  - **id** (query): ID do filme a ser excluído.
- **Respostas:**
  - **200 OK:** Filme excluído com sucesso.
  - **404 Not Found:** Filme não encontrado.
  - **500 Internal Server Error:** Erro ao excluir o filme.

## Esquema do Modelo `Movie`

Aqui está o esquema detalhado do modelo `Movie` para referência:

- **id**: integer
- **name**: string
- **date**: short (número de dias desde "01/01/1970")
- **score**: double
- **genre**: array of strings
- **overview**: string
- **crew**: array of strings
- **originTitle**: string
- **status**: string
- **originLang**: string
- **budget**: double
- **revenue**: double
- **country**: string


