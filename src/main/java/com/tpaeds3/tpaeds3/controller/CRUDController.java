package com.tpaeds3.tpaeds3.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tpaeds3.tpaeds3.model.Index;
import com.tpaeds3.tpaeds3.model.IndexFileManager;
import com.tpaeds3.tpaeds3.model.IndexIdFileManager;
import com.tpaeds3.tpaeds3.model.Movie;
import com.tpaeds3.tpaeds3.model.MovieFileManager;

import io.swagger.v3.oas.annotations.tags.Tag;



@RestController
@RequestMapping("/CRUD")
@Tag(name = "02 - Operações de Atualização de Banco : ")
public class CRUDController {

    private static final String FILE_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/movies.db";
    private static final String INDEX1_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/index01.db";
    private static final String INDEX_BY_ID = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexById.db";
    private static final String INDEX_BY_GENRE = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexByGenre.db";

    /**
     * Método auxiliar para inicializar arquivos e gerenciadores.
     */
    private Map<String, Object> initializeFiles() throws FileNotFoundException {
        Map<String, Object> resources = new HashMap<>();
        
        resources.put("binaryFile", new RandomAccessFile(FILE_PATH, "rw"));
        resources.put("binaryIndexByIdFile", new RandomAccessFile(INDEX_BY_ID, "rw"));
        resources.put("binaryIndexFile", new RandomAccessFile(INDEX1_PATH, "rw"));

        resources.put("movieFileManager", new MovieFileManager((RandomAccessFile) resources.get("binaryFile")));
        resources.put("indexByIdFileManager", new IndexIdFileManager((RandomAccessFile) resources.get("binaryIndexByIdFile")));
        resources.put("indexFileManager", new IndexFileManager((RandomAccessFile) resources.get("binaryIndexFile")));

        return resources;
    }

    /**
     * Método auxiliar para fechar arquivos RandomAccessFile.
     */
    private void closeFiles(Map<String, Object> resources) {
        resources.values().forEach(resource -> {
            if (resource instanceof RandomAccessFile) {
                try {
                    ((RandomAccessFile) resource).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Tratamento centralizado de exceções de I/O.
     */
    private <T> ResponseEntity<T> handleIOException(IOException e, T body) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @PostMapping("/createMovie")
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        Map<String, Object> resources = null;
        try {
            resources = initializeFiles();
            MovieFileManager movieFileManager = (MovieFileManager) resources.get("movieFileManager");

            if (movieFileManager.writeMovie(movie) != -1) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .header("/entrega01", "/getMovie/" + movie.getId())
                        .body(movie);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(movie);
            }

        } catch (IOException e) {
            return handleIOException(e, movie);
        } finally {
            if (resources != null) closeFiles(resources);
        }
    }

    @DeleteMapping("/deleteMovie")
    public ResponseEntity<String> deleteMovie(@RequestParam("id") int id) {
        Map<String, Object> resources = null;
        try {
            resources = initializeFiles();
            MovieFileManager movieFileManager = (MovieFileManager) resources.get("movieFileManager");
            IndexIdFileManager indexByIdFileManager = (IndexIdFileManager) resources.get("indexByIdFileManager");
            IndexFileManager indexFileManager = (IndexFileManager) resources.get("indexFileManager");

            String deletedMovie = movieFileManager.deleteMovie(id);
            if (deletedMovie != null) {
                indexByIdFileManager.deleteIndex(id);         // Remove do índice por ID
                indexFileManager.deleteIndex(deletedMovie);   // Remove do índice por nome

                return ResponseEntity.ok("Filme excluído com sucesso.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Filme não encontrado.");
            }

        } catch (IOException e) {
            return handleIOException(e, "Erro ao excluir o filme.");
        } finally {
            if (resources != null) closeFiles(resources);
        }
    }

    @PatchMapping("/updateMovie/{id}")
    public ResponseEntity<String> updateMovie(@PathVariable int id, @RequestBody Map<String, Object> updates) {
        Map<String, Object> resources = null;
        try {
            resources = initializeFiles();
            MovieFileManager movieFileManager = (MovieFileManager) resources.get("movieFileManager");
            IndexIdFileManager indexByIdFileManager = (IndexIdFileManager) resources.get("indexByIdFileManager");
            IndexFileManager indexFileManager = (IndexFileManager) resources.get("indexFileManager");

            Index index = movieFileManager.updateMovie(id, updates);

            Long newPosition = index.getNewPosition();
            String lastName = index.getLastName();
            String newName = index.getNewName();

            if (newPosition != -1) {
                indexByIdFileManager.updateIndex(id, newPosition);            // Atualiza no índice por ID
                indexFileManager.updateIndex(lastName, newName, newPosition); // Atualiza no índice por nome

                return ResponseEntity.ok("Filme atualizado com sucesso.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Filme não encontrado.");
            }

        } catch (IOException e) {
            return handleIOException(e, "Erro ao atualizar o filme.");
        } finally {
            if (resources != null) closeFiles(resources);
        }
    }
}
