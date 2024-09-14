package com.tpaeds3.tpaeds3.config;

import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Trabalho Prático - AEDS 3 - API")
                .version("1.0")
                .description("API para gerenciamento de filmes em formato binário.")
                .contact(new Contact()
                        .name("Bruno Guimarães Bitencourt")
                        .email("brunogbitencourt@hotmail.com"))
                .contact(new Contact()
                        .name("Oscar Dias")
                        .email("dias_oscar@hotmail.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://springdoc.org")));
    }

    @Bean
    public OperationCustomizer globalOperationCustomizer() {
        return (operation, handlerMethod) -> {
            String path = handlerMethod.getMethod().getName();
            if (path != null) {
                switch (path) {
                    case "createDataBase" -> {
                        operation.setSummary("Converte um arquivo CSV em um arquivo binário");
                        operation.setDescription("Este endpoint permite a conversão de um arquivo CSV de filmes para um arquivo binário .db.");
                        Schema<?> fileSchema = new Schema<>()
                                .type("object")
                                .properties(Map.of("file", new Schema<>()
                                        .type("string")
                                        .format("binary")));
                        operation.setRequestBody(new RequestBody()
                                .description("Arquivo CSV contendo informações sobre filmes.")
                                .required(true)
                                .content(new Content()
                                        .addMediaType("multipart/form-data", new MediaType()
                                                .schema(fileSchema))));
                        operation.setResponses(new ApiResponses()
                                .addApiResponse("200",
                                        new ApiResponse().description("Arquivo binário criado com sucesso."))
                                .addApiResponse("400", new ApiResponse().description("Arquivo CSV inválido."))
                                .addApiResponse("500", new ApiResponse().description("Erro ao processar o arquivo.")));
                    }
                    case "getMovie" -> {
                        operation.setSummary("Obtém um filme por ID");
                        operation.setDescription("Este endpoint retorna um filme específico com base no ID fornecido.");
                        operation.setParameters(List.of(
                                new Parameter()
                                        .name("id")
                                        .description("ID do filme a ser obtido")
                                        .required(true)
                                        .in("query") 
                                        .schema(new Schema<>().type("integer"))));
                        operation.setResponses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse().description("Filme encontrado."))
                                .addApiResponse("404", new ApiResponse().description("Filme não encontrado."))
                                .addApiResponse("500", new ApiResponse().description("Erro ao obter o filme.")));
                    }
                    case "getMoviesByIds" -> {
                        operation.setSummary("Obtém uma lista de filmes por IDs");
                        operation.setDescription("Este endpoint retorna uma lista de filmes com base em uma lista de IDs fornecidos. IDs não encontrados serão ignorados.");
                        operation.setParameters(List.of(
                                new Parameter()
                                        .name("ids")
                                        .description("Lista de IDs dos filmes a serem obtidos")
                                        .required(true)
                                        .in("query") 
                                        .schema(new Schema<>().type("array").items(new Schema<>().type("integer")))));
                        operation.setResponses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse().description("Lista de filmes encontrada."))
                                .addApiResponse("404",
                                        new ApiResponse()
                                                .description("Nenhum filme encontrado para os IDs fornecidos."))
                                .addApiResponse("500",
                                        new ApiResponse().description("Erro ao obter a lista de filmes.")));
                    }
                    case "getAllMovie" -> {
                        operation.setSummary("Obtém todos os filmes com paginação");
                        operation.setDescription("Este endpoint retorna uma lista de filmes com base na paginação fornecida.");
                        operation.setParameters(List.of(
                                new Parameter()
                                        .name("page")
                                        .description("Número da página para a paginação")
                                        .required(false)
                                        .in("query") 
                                        .schema(new Schema<>().type("integer"))
                                        .example(0),
                                new Parameter()
                                        .name("size")
                                        .description("Número de filmes por página")
                                        .required(false)
                                        .in("query") 
                                        .schema(new Schema<>().type("integer"))
                                        .example(10)));
                        operation.setResponses(new ApiResponses()
                                .addApiResponse("200",
                                        new ApiResponse().description("Lista de filmes retornada com sucesso."))
                                .addApiResponse("500",
                                        new ApiResponse().description("Erro ao obter a lista de filmes.")));
                    }
                    case "createMovie" -> {
                        operation.setSummary("Cria um novo filme");
                        operation.setDescription("Este endpoint adiciona um novo filme ao banco de dados.");

                        // Definição manual do schema Movie com base no CSV
                        Schema<?> movieSchema = new Schema<>()
                                .type("object")
                                .properties(Map.ofEntries(
                                        Map.entry("id", new Schema<>().type("integer")),
                                        Map.entry("name", new Schema<>().type("string")),
                                        Map.entry("date", new Schema<>().type("string").format("date")),
                                        Map.entry("score", new Schema<>().type("number").format("double")),
                                        Map.entry("genre", new Schema<>().type("array").items(new Schema<>().type("string"))),
                                        Map.entry("overview", new Schema<>().type("string")),
                                        Map.entry("crew", new Schema<>().type("array").items(new Schema<>().type("string"))),
                                        Map.entry("originTitle", new Schema<>().type("string")),
                                        Map.entry("status", new Schema<>().type("string")),
                                        Map.entry("originLang", new Schema<>().type("string")),
                                        Map.entry("budget", new Schema<>().type("number").format("double")),
                                        Map.entry("revenue", new Schema<>().type("number").format("double")),
                                        Map.entry("country", new Schema<>().type("string"))));

                        operation.setRequestBody(new RequestBody()
                                .description("Informações do filme a ser criado.")
                                .required(true)
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(movieSchema))));
                        operation.setResponses(new ApiResponses()
                                .addApiResponse("201", new ApiResponse().description("Filme criado com sucesso."))
                                .addApiResponse("400", new ApiResponse().description("Dados do filme inválidos."))
                                .addApiResponse("500", new ApiResponse().description("Erro ao criar o filme.")));
                    }
                    case "updateMovie" -> {
                        operation.setSummary("Atualiza um filme por ID");
                        operation.setDescription("Este endpoint atualiza as informações de um filme com base no ID fornecido.");
                    
                        Schema<?> movieSchema = new Schema<>()
                                .type("object")
                                .properties(Map.ofEntries(
                                        Map.entry("id", new Schema<>().type("integer")),
                                        Map.entry("name", new Schema<>().type("string")),
                                        Map.entry("date", new Schema<>().type("string").format("date")),
                                        Map.entry("score", new Schema<>().type("number").format("double")),
                                        Map.entry("genre", new Schema<>().type("array").items(new Schema<>().type("string"))),
                                        Map.entry("overview", new Schema<>().type("string")),
                                        Map.entry("crew", new Schema<>().type("array").items(new Schema<>().type("string"))),
                                        Map.entry("originTitle", new Schema<>().type("string")),
                                        Map.entry("status", new Schema<>().type("string")),
                                        Map.entry("originLang", new Schema<>().type("string")),
                                        Map.entry("budget", new Schema<>().type("number").format("double")),
                                        Map.entry("revenue", new Schema<>().type("number").format("double")),
                                        Map.entry("country", new Schema<>().type("string"))));
                    
                        operation.setRequestBody(new RequestBody()
                                .description("Informações atualizadas do filme.")
                                .required(true)
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(movieSchema))));
                        operation.setParameters(List.of(
                                new Parameter()
                                        .name("id")
                                        .description("ID do filme a ser atualizado")
                                        .required(true)
                                        .in("path") 
                                        .schema(new Schema<>().type("integer"))));
                    
                        operation.setResponses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse().description("Filme atualizado com sucesso."))
                                .addApiResponse("400", new ApiResponse().description("Dados do filme inválidos ou incompletos."))
                                .addApiResponse("404", new ApiResponse().description("Filme não encontrado."))
                                .addApiResponse("500", new ApiResponse().description("Erro ao atualizar o filme.")));
                    }                    
                    case "deleteMovie" -> {
                        operation.setSummary("Exclui um filme por ID");
                        operation.setDescription("Este endpoint exclui um filme específico com base no ID fornecido.");
                        operation.setParameters(List.of(
                                new Parameter()
                                        .name("id")
                                        .description("ID do filme a ser excluído")
                                        .required(true)
                                        .in("query") 
                                        .schema(new Schema<>().type("integer"))));
                        operation.setResponses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse().description("Filme excluído com sucesso."))
                                .addApiResponse("404", new ApiResponse().description("Filme não encontrado."))
                                .addApiResponse("500", new ApiResponse().description("Erro ao excluir o filme.")));
                    }
                    default -> {
                    }
                }
            }
            return operation;
        };
    }
}
