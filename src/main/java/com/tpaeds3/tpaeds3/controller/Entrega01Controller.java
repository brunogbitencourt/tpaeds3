package com.tpaeds3.tpaeds3.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.tpaeds3.tpaeds3.model.Movie;
import com.tpaeds3.tpaeds3.model.MovieFileManager;

import io.swagger.v3.oas.annotations.tags.Tag;



@RestController
@RequestMapping("/entrega01")
@Tag(name = "Entrega 01: ")
public class Entrega01Controller {
   
    @PostMapping("/convertCSVToBinary")
    public ResponseEntity<Resource> convertCSVToBinary(@RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
        return ResponseEntity.badRequest().body(null);
    }

    try {
        RandomAccessFile bynaryFile = new RandomAccessFile("./src/main/java/com/tpaeds3/tpaeds3/files_out/movies.db", "rw");
        MovieFileManager movieFileManager = new MovieFileManager(bynaryFile);
        List<Movie> movies = parseCSV(file); // Parse file to object list

        bynaryFile.seek(0); // Set cursor to beginning
        bynaryFile.writeInt(0); // Write Header initializing with 0

        for (Movie movie : movies) {
            movieFileManager.writeMovie(movie);
        }

        // Close file
        bynaryFile.close();

        // Retorn bynary file to API
        File fileToReturn = new File("./src/main/java/com/tpaeds3/tpaeds3/files_out/movies.db");
        InputStreamResource resource = new InputStreamResource(new FileInputStream(fileToReturn));

        // Configure HTTP Response
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileToReturn.getName());
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(fileToReturn.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);

    } catch (IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Return error 500
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
                movie.fromByteArray(b);
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
