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
    private static final String INDEX_BY_GENRE_MULTLIST = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexByGenreMultlist.db";
    /**
     * Método auxiliar para inicializar arquivos e gerenciadores.
     */
    private Map<String, Object> initializeFiles() throws FileNotFoundException {
        Map<String, Object> resources = new HashMap<>();

        resources.put("binaryFile", new RandomAccessFile(FILE_PATH, "rw"));
        resources.put("binaryIndexFile", new RandomAccessFile(INDEX1_PATH, "rw"));
        resources.put("binaryIndexByIdFile", new RandomAccessFile(INDEX_BY_ID, "rw"));
        resources.put("binaryIndexByGenreFile", new RandomAccessFile(INDEX_BY_GENRE, "rw"));
        resources.put("binaryIndexByGenreMultlistFile", new RandomAccessFile(INDEX_BY_GENRE_MULTLIST, "rw"));

        resources.put("movieFileManager", new MovieFileManager((RandomAccessFile) resources.get("binaryFile")));
        resources.put("indexFileManager", new IndexFileManager((RandomAccessFile) resources.get("binaryIndexFile")));
        resources.put("indexByIdFileManager", new IndexIdFileManager((RandomAccessFile) resources.get("binaryIndexByIdFile")));
        resources.put("indexByGenreFileManager", new IndexFileManager((RandomAccessFile) resources.get("binaryIndexByGenreFile")));
        resources.put("indexByGenreMultlistFile", new IndexFileManager((RandomAccessFile) resources.get("binaryIndexByGenreMultlistFile")));

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
            IndexFileManager indexFileManager = (IndexFileManager) resources.get("indexFileManager");
            IndexIdFileManager indexByIdFileManager = (IndexIdFileManager) resources.get("indexByIdFileManager");
            IndexFileManager indexByGenreFileManager = (IndexFileManager) resources.get("indexByGenreFileManager");
            IndexFileManager indexByGenreMultlistFile = (IndexFileManager) resources.get("indexByGenreMultlistFile");

            long position = movieFileManager.writeMovie(movie);
            if (position != -1) {
                int movieId = movie.getId();
                long indexIdPosition = indexByIdFileManager.writeIndex(movieId, position);
                indexFileManager.writeIndex(movie.getName(), movieId);
                for (String genre : movie.getGenre()) {
                    long genrePosition = indexByGenreFileManager.writeGenreIndex(genre);
                    long multilistHead = indexByGenreFileManager.getMultlistHead(genrePosition);
                    indexByGenreMultlistFile.writeMultlistIndex(movieId, multilistHead, indexIdPosition, genrePosition, (RandomAccessFile) resources.get("binaryIndexByGenreFile"));

                }


                return ResponseEntity.status(HttpStatus.CREATED)
                        .header("/CRUDByIndex", "/getMovie/" + movie.getId())
                        .body(movie);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(movie);
            }

        } catch (IOException e) {
            return handleIOException(e, movie);
        } finally {
            if (resources != null)
                closeFiles(resources);
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
                indexByIdFileManager.deleteIndex(id); // Remove do índice por ID
                indexFileManager.deleteIndex(deletedMovie); // Remove do índice por nome

                return ResponseEntity.ok("Filme excluído com sucesso.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Filme não encontrado.");
            }

        } catch (IOException e) {
            return handleIOException(e, "Erro ao excluir o filme.");
        } finally {
            if (resources != null)
                closeFiles(resources);
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
            IndexFileManager indexByGenreFileManager = (IndexFileManager) resources.get("indexByGenreFileManager");
            IndexFileManager indexByGenreMultlistFile = (IndexFileManager) resources.get("indexByGenreMultlistFile");
    
            // Obter o filme atual antes da atualização para capturar os gêneros antigos
            Movie currentMovie = movieFileManager.readMovie(id);
            if (currentMovie == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Filme não encontrado.");
            }
    
            // Lista de gêneros antigos
            List<String> oldGenres = new ArrayList<>(currentMovie.getGenre());
    
            // Atualizar o filme
            Index index = movieFileManager.updateMovie(id, updates);
            String lastName = index.getLastName();
            String newName = index.getNewName();
            long moviePosition = index.getNewPosition();
    
                
            // Atualiza o índice por ID e nome
            indexByIdFileManager.updateIndex(id, moviePosition);
            indexFileManager.updateIndex(lastName, newName, moviePosition);
    
            // Obter a nova lista de gêneros do filme atualizado
            Movie updatedMovie = movieFileManager.readMovie(id);
            List<String> newGenres = updatedMovie.getGenre();
    
            // Identificar os gêneros que precisam ser removidos e adicionados
            List<String> genresToRemove = new ArrayList<>(oldGenres);
            genresToRemove.removeAll(newGenres); // Gêneros que foram removidos
    
            List<String> genresToAdd = new ArrayList<>(newGenres);
            genresToAdd.removeAll(oldGenres); // Gêneros que foram adicionados
    
            // Remover o filme dos gêneros que não estão mais associados
            for (String genre : genresToRemove) {
                indexByGenreFileManager.removeMovieFromGenre(genre, id,
                        indexByGenreFileManager.getIndexFile(),
                        indexByGenreMultlistFile.getIndexFile());
            }
    
            // Adicionar o filme aos novos gêneros
            for (String genre : genresToAdd) {
                indexByGenreFileManager.updateGenreIndex(genre, id, moviePosition,
                        indexByGenreFileManager.getIndexFile(),
                        indexByGenreMultlistFile.getIndexFile());
            }
    
            return ResponseEntity.ok("Filme e índice de gênero atualizados com sucesso.");
    
        } catch (IOException e) {
            return handleIOException(e, "Erro ao atualizar o filme e índice de gênero.");
        } finally {
            if (resources != null) closeFiles(resources);
        }
    }
    

}
