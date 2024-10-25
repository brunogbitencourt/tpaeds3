package com.tpaeds3.tpaeds3.controller;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tpaeds3.tpaeds3.model.Index;
import com.tpaeds3.tpaeds3.model.IndexFileManager;
import com.tpaeds3.tpaeds3.model.Movie;
import com.tpaeds3.tpaeds3.model.MovieFileManager;

import io.swagger.v3.oas.annotations.tags.Tag;



@RestController
@RequestMapping("/entrega01")
@Tag(name = "Entrega 01: ")
public class Entrega01Controller {

    private static final String FILE_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/movies.db";
    private static final String INDEX1_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/index01.db";


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

    @GetMapping("/getMoviesByIds")
    public ResponseEntity<Map<String, Object>> getMoviesByIds(@RequestParam List<Integer> ids) {
        try (RandomAccessFile binaryFile = new RandomAccessFile(FILE_PATH, "r")) {
            MovieFileManager movieFileManager = new MovieFileManager(binaryFile);
            List<Movie> movies = movieFileManager.readMoviesByIds(new ArrayList<>(ids));

            Map<String, Object> response = new HashMap<>();
            response.put("records", movies);
            response.put("totalRecords", movies.size());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Retorna erro 500
        }
    }

    @GetMapping("/getAllMovies")
    public ResponseEntity<Map<String, Object>> getAllMovie(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
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

        try (RandomAccessFile binaryFile = new RandomAccessFile(FILE_PATH, "rw")) {
            MovieFileManager movieFileManager = new MovieFileManager(binaryFile);

            if (movieFileManager.writeMovie(movie)!= -1) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .header("/entrega01", "/getMovie/" + movie.getId())
                        .body(movie);
            } else {
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

            String deletedMovie = movieFileManager.deleteMovie(id);

            if (deletedMovie != null) {
                RandomAccessFile binaryIndexFile = new RandomAccessFile(INDEX1_PATH, "rw");
                IndexFileManager indexFileManager = new IndexFileManager(binaryIndexFile);

                indexFileManager.deleteIndex(deletedMovie);

                return ResponseEntity.ok("Filme excluído com sucesso.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Filme não encontrado.");
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao excluir o filme."); // Retorna
                                                                                                             // erro 500
        }
    }

    @PatchMapping("/updateMovie/{id}")
    public ResponseEntity<String> updateMovie(@PathVariable int id, @RequestBody Map<String, Object> updates) {
        try (RandomAccessFile binaryFile = new RandomAccessFile(FILE_PATH, "rw")) {
            MovieFileManager movieFileManager = new MovieFileManager(binaryFile);

            Index index = movieFileManager.updateMovie(id, updates);

            Long newPosition = index.getNewPosition();

            if (newPosition != -1) {
                RandomAccessFile binaryIndexFile = new RandomAccessFile(INDEX1_PATH, "rw");
                IndexFileManager indexFileManager = new IndexFileManager(binaryIndexFile);

                indexFileManager.updateIndex(index.getLastName(), index.getNewName(), index.getNewPosition());

                return ResponseEntity.ok("Filme atualizado com sucesso.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Filme não encontrado.");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao atualizar o filme.");
        }
    }
   

}
