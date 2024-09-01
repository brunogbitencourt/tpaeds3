package com.tpaeds3.tpaeds3.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.tpaeds3.tpaeds3.model.Movie;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;



@RestController
@RequestMapping("/entrega01")
@Tag(name = "Entrega 01: ")
public class Entrega01Controller {
   
    @PostMapping("/uploadCSV")
    @Operation(summary = "Faz upload de um arquivo CSV", description = "Este endpoint permite o upload de um arquivo CSV.")
    /*@ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arquivo CSV processado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo CSV inválido"),
            @ApiResponse(responseCode = "500", description = "Erro ao processar arquivo")
    })*/
    public ResponseEntity<List<Movie>> uploadCSV(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) { 
            return ResponseEntity.badRequest().body(null); 
        }
        
        try {
            List<Movie> movies = parseCSV(file); // Converte Arquivo em Lista de Objetos
            return ResponseEntity.ok(movies); 
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Retorna erro 500 
        }
    }

    private List<Movie> parseCSV(MultipartFile file) throws IOException {
        List<Movie> movies = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String[] line;
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy"); // 
            reader.skip(1); // Pula o cabeçalho

            while ((line = reader.readNext()) != null) {
                Movie movie = new Movie();
                movie.setId(Integer.parseInt(line[0]));
                movie.setName(line[1]);
                try {
                    movie.setDate(dateFormat.parse(line[2]));
                } catch (ParseException e) {
                    e.printStackTrace(); 
                }
                movie.setScore(Double.parseDouble(line[3]));
                movie.setGenre(Arrays.asList(line[4].split(",")));
                movie.setOverview(line[5]);
                movie.setCrew(Arrays.asList(line[6].split(",")));
                movie.setOriginTitle(line[7]);
                movie.setStatus(line[8]);
                movie.setOriginLang(line[9]);
                movie.setBudget(Double.parseDouble(line[10]));
                movie.setRevenue(Double.parseDouble(line[11]));
                movie.setCountry(line[12]);

                movies.add(movie);
            }
        } catch (CsvValidationException e) {
            e.printStackTrace(); 
        }
        return movies;
    }





}
