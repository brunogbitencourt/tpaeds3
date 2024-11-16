package com.tpaeds3.tpaeds3.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tpaeds3.tpaeds3.model.IndexByNameFileManager;
import com.tpaeds3.tpaeds3.model.IndexByIdFileManager;
import com.tpaeds3.tpaeds3.model.Movie;
import com.tpaeds3.tpaeds3.model.MovieDBFileManager;

import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/CRUDByIndex")
@Tag(name = "03 - Operações de Consulta: ")
public class SearchController {

    private static final String MOVIE_DB_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/movies.db";
    private static final String INDEX_BY_NAME_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexByName.db";
    private static final String INDEX_BY_ID_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexById.db";
    private static final String INDEX_BY_GENRE_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexByGenre.db";
    private static final String INDEX_BY_GENRE_MULTLIST_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexByGenreMultlist.db";

    // Método Auxilia na inicialização de arquivosde índice
    private Map<String, Object> initializeFiles() throws FileNotFoundException {
        Map<String, Object> resources = new HashMap<>();

        resources.put("binaryMovieDBFile", new RandomAccessFile(MOVIE_DB_PATH, "r"));
        resources.put("binaryIndexByNameFile", new RandomAccessFile(INDEX_BY_NAME_PATH, "r"));
        resources.put("binaryIndexByIdFile", new RandomAccessFile(INDEX_BY_ID_PATH, "r"));
        resources.put("binaryIndexByGenreFile", new RandomAccessFile(INDEX_BY_GENRE_PATH, "r"));
        resources.put("binaryIndexByGenreMultlistFile", new RandomAccessFile(INDEX_BY_GENRE_MULTLIST_PATH, "r"));

        resources.put("movieDBFileManager", new MovieDBFileManager((RandomAccessFile) resources.get("binaryMovieDBFile")));
        resources.put("indexByNameFileManager", new IndexByNameFileManager((RandomAccessFile) resources.get("binaryIndexByNameFile")));
        resources.put("indexByIdFileManager", new IndexByIdFileManager((RandomAccessFile) resources.get("binaryIndexByIdFile")));
        resources.put("indexByGenreFileManager", new IndexByNameFileManager((RandomAccessFile) resources.get("binaryIndexByGenreFile")));
        resources.put("indexByGenreMultlistFile", new IndexByNameFileManager((RandomAccessFile) resources.get("binaryIndexByGenreMultlistFile")));

        return resources;
    }

    /**
     * Fecha todos os arquivos RandomAccessFile para liberar os recursos.
     */
    private void closeFiles(Map<String, Object> resources) {
        for (Object obj : resources.values()) {
            if (obj instanceof RandomAccessFile) {
                try {
                    ((RandomAccessFile) obj).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Trata exceções de I/O.
     */
    private ResponseEntity<?> handleIOException(IOException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @GetMapping("/getMoviesByIds")
    public ResponseEntity<Map<String, Object>> getMoviesByIds(@RequestParam List<Integer> ids) {
        Map<String, Object> resources = null;
        try {
            resources = initializeFiles();
            MovieDBFileManager movieDBFileManager = (MovieDBFileManager) resources.get("movieDBFileManager");
            List<Movie> movies = movieDBFileManager.readMoviesByIds(new ArrayList<>(ids));

            Map<String, Object> response = new HashMap<>();
            response.put("records", movies);
            response.put("totalRecords", movies.size());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return (ResponseEntity<Map<String, Object>>) handleIOException(e);
        } finally {
            if (resources != null) closeFiles(resources);
        }
    }

    @GetMapping("/getAllMovies")
    public ResponseEntity<Map<String, Object>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> resources = null;
        try {
            resources = initializeFiles();
            MovieDBFileManager movieDBFileManager = (MovieDBFileManager) resources.get("movieDBFileManager");
            List<Movie> movies = movieDBFileManager.readAllMovies(page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("records", movies);
            response.put("totalRecords", movies.size());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return (ResponseEntity<Map<String, Object>>) handleIOException(e);
        } finally {
            if (resources != null) closeFiles(resources);
        }
    }

    @GetMapping("/getMovieByName")
    public ResponseEntity<Movie> getMovieByName(@RequestParam String param) {
        Map<String, Object> resources = null;
        try {
            resources = initializeFiles();

            IndexByNameFileManager indexByNameFileManager = (IndexByNameFileManager) resources.get("indexByNameFileManager");
            IndexByIdFileManager indexByIdFileManager = (IndexByIdFileManager) resources.get("indexByIdFileManager");
            MovieDBFileManager movieDBFileManager = (MovieDBFileManager) resources.get("movieDBFileManager");

            int movieId = indexByNameFileManager.findIdByMovieName(param);
            if (movieId == -1) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            long moviePosition = indexByIdFileManager.findPositionById(movieId);
            if (moviePosition == -1) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            Movie movie = movieDBFileManager.getMovieByPosition(moviePosition);
            return ResponseEntity.ok(movie);

        } catch (IOException e) {
            return (ResponseEntity<Movie>) handleIOException(e);
        } finally {
            if (resources != null) closeFiles(resources);
        }
    }

    @GetMapping("/getMovieById")
    public ResponseEntity<Movie> getMovieById(@RequestParam int id) {
        Map<String, Object> resources = null;
        try {
            resources = initializeFiles();
            IndexByIdFileManager indexByIdFileManager = (IndexByIdFileManager) resources.get("indexByIdFileManager");
            MovieDBFileManager movieDBFileManager = (MovieDBFileManager) resources.get("movieDBFileManager");

            long position = indexByIdFileManager.findPositionById(id);
            if (position == -1) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            Movie movie = movieDBFileManager.getMovieByPosition(position);
            return ResponseEntity.ok(movie);

        } catch (IOException e) {
            return (ResponseEntity<Movie>) handleIOException(e);
        } finally {
            if (resources != null) closeFiles(resources);
        }
    }

    @GetMapping("/getMoviesByGenre")
    public ResponseEntity<Map<String, Object>> getMoviesByGenre(@RequestParam String genre) {
        Map<String, Object> resources = null;
        try {
            resources = initializeFiles();

            MovieDBFileManager movieDBFileManager = (MovieDBFileManager) resources.get("movieDBFileManager");
            IndexByNameFileManager indexByGenreFileManager = (IndexByNameFileManager) resources.get("indexByGenreFileManager");
            IndexByNameFileManager indexByGenreMultlistFile = (IndexByNameFileManager) resources.get("indexByGenreMultlistFile");
            IndexByIdFileManager indexByIdFileManager = (IndexByIdFileManager) resources.get("indexByIdFileManager");

            List<Movie> movies = movieDBFileManager.readMoviesByGenre(genre, indexByGenreFileManager, indexByGenreMultlistFile, indexByIdFileManager);

            Map<String, Object> response = new HashMap<>();
            response.put("records", movies);
            response.put("totalRecords", movies.size());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return (ResponseEntity<Map<String, Object>>) handleIOException(e);
        } finally {
            if (resources != null) closeFiles(resources);
        }
    }
}
