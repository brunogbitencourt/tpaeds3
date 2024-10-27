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

import com.tpaeds3.tpaeds3.model.IndexFileManager;
import com.tpaeds3.tpaeds3.model.IndexIdFileManager;
import com.tpaeds3.tpaeds3.model.Movie;
import com.tpaeds3.tpaeds3.model.MovieFileManager;

import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/CRUDByIndex")
@Tag(name = "03 - Operações de Consulta: ")
public class SearchController {

    private static final String FILE_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/movies.db";
    private static final String INDEX1_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/index01.db";
    private static final String INDEX_BY_ID = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexById.db";
    private static final String INDEX_BY_GENRE = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexByGenre.db";
    private static final String INDEX_BY_GENRE_MULTLIST = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexByGenreMultlist.db";

    // Método Auxilia na inicialização de arquivosde índice
    private Map<String, Object> initializeFiles() throws FileNotFoundException {
        Map<String, Object> resources = new HashMap<>();

        resources.put("binaryFile", new RandomAccessFile(FILE_PATH, "r"));
        resources.put("binaryIndexFile", new RandomAccessFile(INDEX1_PATH, "r"));
        resources.put("binaryIndexByIdFile", new RandomAccessFile(INDEX_BY_ID, "r"));
        resources.put("binaryIndexByGenreFile", new RandomAccessFile(INDEX_BY_GENRE, "r"));
        resources.put("binaryIndexByGenreMultlistFile", new RandomAccessFile(INDEX_BY_GENRE_MULTLIST, "r"));

        resources.put("movieFileManager", new MovieFileManager((RandomAccessFile) resources.get("binaryFile")));
        resources.put("indexFileManager", new IndexFileManager((RandomAccessFile) resources.get("binaryIndexFile")));
        resources.put("indexByIdFileManager", new IndexIdFileManager((RandomAccessFile) resources.get("binaryIndexByIdFile")));
        resources.put("indexByGenreFileManager", new IndexFileManager((RandomAccessFile) resources.get("binaryIndexByGenreFile")));
        resources.put("indexByGenreMultlistFile", new IndexFileManager((RandomAccessFile) resources.get("binaryIndexByGenreMultlistFile")));

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

    @GetMapping("/getMovie")
    public ResponseEntity<Movie> getMovie(@RequestParam("id") int id) {
        try (RandomAccessFile binaryFile = new RandomAccessFile(FILE_PATH, "r")) {
            MovieFileManager movieFileManager = new MovieFileManager(binaryFile);
            Movie movie = movieFileManager.readMovie(id);

            return movie != null ? ResponseEntity.ok(movie) :
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        } catch (IOException e) {
            return (ResponseEntity<Movie>) handleIOException(e);
        }
    }

    @GetMapping("/getMoviesByIds")
    public ResponseEntity<Map<String, Object>> getMoviesByIds(@RequestParam List<Integer> ids) {
        Map<String, Object> resources = null;
        try {
            resources = initializeFiles();
            MovieFileManager movieFileManager = (MovieFileManager) resources.get("movieFileManager");
            List<Movie> movies = movieFileManager.readMoviesByIds(new ArrayList<>(ids));

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
            MovieFileManager movieFileManager = (MovieFileManager) resources.get("movieFileManager");
            List<Movie> movies = movieFileManager.readAllMovies(page, size);

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

            IndexFileManager indexFileManager = (IndexFileManager) resources.get("indexFileManager");
            IndexIdFileManager indexByIdFileManager = (IndexIdFileManager) resources.get("indexByIdFileManager");
            MovieFileManager movieFileManager = (MovieFileManager) resources.get("movieFileManager");

            int movieId = indexFileManager.findIdByMovieName(param);
            if (movieId == -1) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            long moviePosition = indexByIdFileManager.findPositionById(movieId);
            if (moviePosition == -1) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            Movie movie = movieFileManager.getMovieByPosition(moviePosition);
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
            IndexIdFileManager indexByIdFileManager = (IndexIdFileManager) resources.get("indexByIdFileManager");
            MovieFileManager movieFileManager = (MovieFileManager) resources.get("movieFileManager");

            long position = indexByIdFileManager.findPositionById(id);
            if (position == -1) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            Movie movie = movieFileManager.getMovieByPosition(position);
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

            MovieFileManager movieFileManager = (MovieFileManager) resources.get("movieFileManager");
            IndexFileManager indexByGenreFileManager = (IndexFileManager) resources.get("indexByGenreFileManager");
            IndexFileManager indexByGenreMultlistFile = (IndexFileManager) resources.get("indexByGenreMultlistFile");
            IndexIdFileManager indexByIdFileManager = (IndexIdFileManager) resources.get("indexByIdFileManager");

            List<Movie> movies = movieFileManager.readMoviesByGenre(genre, indexByGenreFileManager, indexByGenreMultlistFile, indexByIdFileManager);

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
