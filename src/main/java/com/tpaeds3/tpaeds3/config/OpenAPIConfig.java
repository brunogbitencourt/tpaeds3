package com.tpaeds3.tpaeds3.config;

import java.util.Map;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
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
                        .description("""
                                    API para gerenciamento de filmes em formato binário.

                                    **Contatos:**
                                    - Bruno Guimarães Bitencourt: brunogbitencourt@hotmail.com
                                    - Oscar Dias: dias_oscar@hotmail.com
                                """)
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
                        Schema<?> fileSchema;
                        fileSchema = new Schema<>()
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
                }
            }
            return operation;
        };
    }
}
