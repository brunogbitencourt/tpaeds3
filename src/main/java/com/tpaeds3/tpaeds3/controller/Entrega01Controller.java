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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    private static final String FILE_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/movies.db";

    @PostMapping("/convertCSVToBinary")
    public ResponseEntity<Resource> convertCSVToBinary(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        // try with resources, fecha o arquivo automaticamante ao final do bloco
        try (RandomAccessFile binaryFile = new RandomAccessFile(FILE_PATH, "rw")) {
            MovieFileManager movieFileManager = new MovieFileManager(binaryFile);
            List<Movie> movies = parseCSV(file); // Parse file to object list

            binaryFile.seek(0); // Set cursor to beginning
            binaryFile.writeInt(0); // Write Header initializing with 0

            for (Movie movie : movies) {
                if(!movieFileManager.writeMovie(movie)){
                    System.out.println("Erro na inserção do arquivo");              
                }
            }

            // Retorn bynary file to API
            File fileToReturn = new File(FILE_PATH);
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

    @GetMapping("/getMovie")
    public ResponseEntity<Movie> getMovie(@RequestParam("id") int id) {
        try (RandomAccessFile binaryFile = new RandomAccessFile(FILE_PATH, "r")) {
            MovieFileManager movieFileManager = new MovieFileManager(binaryFile);
            Movie movie = movieFileManager.readMovie(id);

            if (movie != null) {
                return ResponseEntity.ok(movie);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Retorna erro 500
        }
    }

    @GetMapping("/getAllMovies")
    public ResponseEntity<Map<String, Object>>  getAllMovie(
        @RequestParam(defaultValue = "0") int page, 
        @RequestParam(defaultValue = "10") int size
    ) {
        try (RandomAccessFile binaryFile = new RandomAccessFile(FILE_PATH, "r")) {
            MovieFileManager movieFileManager = new MovieFileManager(binaryFile);
            List<Movie> movies = movieFileManager.readAllMovies(page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("records", movies);
            response.put("totalRecords", movies.size());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Retorna erro 500
        }
    }

    
    @PostMapping("/createMovie")
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
         
        try (RandomAccessFile binaryFile = new RandomAccessFile(FILE_PATH, "rw")){
            MovieFileManager movieFileManager = new MovieFileManager(binaryFile);
            
            if(movieFileManager.writeMovie(movie)){
                return ResponseEntity.status(HttpStatus.CREATED)
                .header("/entrega01", "/getMovie/" + movie.getId())
                .body(movie);
            }
            else{
                return new ResponseEntity<>(movie, HttpStatus.BAD_REQUEST);
            }
            
        } catch (Exception e) {
            return new ResponseEntity<>(movie, HttpStatus.INTERNAL_SERVER_ERROR);
        }     
       
    }    

    @DeleteMapping("/deleteMovie")
    public ResponseEntity<String> deleteMovie(@RequestParam("id") int id) {
        try (RandomAccessFile binaryFile = new RandomAccessFile(FILE_PATH, "rw")) {
            MovieFileManager movieFileManager = new MovieFileManager(binaryFile);

            if (movieFileManager.deleteMovie(id)) {
                return ResponseEntity.ok("Filme excluído com sucesso.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Filme não encontrado.");
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao excluir o filme."); // Retorna
                                                                                                             // erro 500
        }
    }

    private List<Movie> parseCSV(MultipartFile file) throws IOException {
        List<Movie> movies = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String[] line;
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

            while ((line = reader.readNext()) != null) {
                Movie movie = new Movie();
                // movie.setId(Integer.parseInt(line[0]));
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
