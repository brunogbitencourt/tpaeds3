package com.tpaeds3.tpaeds3.config;

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
                        .description("API para Implementação dos métodos")
                        .contact(new Contact()
                                .name("Bruno Guimarães Bitencourt")
                                .email("brunogbitencourt@hotmail.com"))
                        .contact(new Contact()
                                .name("Oscar Dias")
                                .email("oscardias@gmail.com"))
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
                    case "uploadCSV" -> {
                        operation.setSummary("Faz upload de um arquivo CSV");
                        operation.setDescription("Este endpoint permite o upload de um arquivo CSV.");
                        Schema<?> fileSchema = new Schema<>()
                                .type("object")
                                .addProperties("file", new Schema<>()
                                        .type("string")
                                        .format("binary"));
                        operation.setRequestBody(new RequestBody()
                                .description("Arquivo CSV a ser carregado")
                                .required(true)
                                .content(new Content()
                                        .addMediaType("multipart/form-data", new MediaType()
                                                .schema(fileSchema))));
                        operation.setResponses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse().description("Arquivo CSV processado com sucesso"))
                                .addApiResponse("400", new ApiResponse().description("Arquivo CSV inválido"))
                                .addApiResponse("500", new ApiResponse().description("Erro ao processar arquivo")));
                    }
                    default -> {
                    }
                }
            }
            return operation;
        };
    }
}
