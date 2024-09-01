package com.tpaeds3.tpaeds3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trabalho Prático - AEDS 3 - API")
                        .version("1.0")
                        .description("API para Implementação dos métodos\n\n" +
                                     "Contatos:\n" +
                                     "Bruno Guimarães Bitencourt - brunogbitencourt@hotmail.com\n" +
                                     "Oscar Dias - oscardias@gmail.com")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .addTagsItem(new Tag().name("Entrega 01: ").description("Operações CRUD com o arquivo"));
                //.addTagsItem(new Tag().name("Entrega 02: ").description("??"));
                //.addTagsItem(new Tag().name("Entrega 03: ").description("??"));
                //.addTagsItem(new Tag().name("Entrega 04: ").description("??"));            
            }
}