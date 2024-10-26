package com.tpaeds3.tpaeds3.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
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
@Tag(name = "03 - Operações CRUD com Índice: ")
public class CRUDByIndexController {

    private static final String FILE_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/movies.db";
    private static final String INDEX1_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/index01.db";
    private static final String INDEX_BY_ID = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexById.db";
    private static final String INDEX_BY_GENRE = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexByGenre.db";
    private static final String INDEX_BY_GENRE_MULTLIST = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexByGenreMultlist.db";

    
    @GetMapping("/getMovieByName")
    public ResponseEntity<Movie> getMovieByName(@RequestParam String param) throws FileNotFoundException {
        try{
            // Abre o arquivo de indice indireto Nome -> id
            RandomAccessFile binaryIndexFile = new RandomAccessFile(INDEX1_PATH, "r");
            IndexFileManager indexFileManager =  new IndexFileManager(binaryIndexFile);
           
            // Busca o id do filme pelo nome no arquivo indireto
            int movieId = indexFileManager.findIdByMovieName(param);
            if (movieId == -1 )
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            // Abre o arquivo de índice para leitura
            RandomAccessFile binaryIndexByIdFile = new RandomAccessFile(INDEX_BY_ID, "r");
            IndexIdFileManager indexByIdFileManager = new IndexIdFileManager(binaryIndexByIdFile);

            // Busca a posição do filme pelo ID
            long moviePosition = indexByIdFileManager.findPositionById(movieId);
            if (moviePosition == -1) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Abre o banco de dados
            RandomAccessFile binaryMovieFile = new RandomAccessFile(FILE_PATH, "r");            
            MovieFileManager movieFileManager = new MovieFileManager(binaryMovieFile);

            // Busca o filme pela posição
            Movie movie = movieFileManager.getMovieByPosition(moviePosition); 

            return ResponseEntity.ok(movie);
        
        
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @GetMapping("/getMovieById")
    public ResponseEntity<Movie> getMovieById(@RequestParam int id) throws FileNotFoundException {
        try {
            // Abre o arquivo de índice para leitura
            RandomAccessFile binaryIndexByIdFile = new RandomAccessFile(INDEX_BY_ID, "r");
            IndexIdFileManager indexByIdFileManager = new IndexIdFileManager(binaryIndexByIdFile);

            // Busca a posição do filme pelo ID
            long position = indexByIdFileManager.findPositionById(id);
            if (position == -1) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Abre o arquivo de filmes para leitura
            RandomAccessFile binaryMovieFile = new RandomAccessFile(FILE_PATH, "r");
            MovieFileManager movieFileManager = new MovieFileManager(binaryMovieFile);

            // Recupera o filme pela posição encontrada
            Movie movie = movieFileManager.getMovieByPosition(position);

            return ResponseEntity.ok(movie);
        
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/getMoviesByGenre")
    public ResponseEntity<Map<String, Object>> getAllMovie(
            @RequestParam String genre) {
        try {
            
            RandomAccessFile binaryDataFile = new RandomAccessFile(FILE_PATH, "rw");
            RandomAccessFile binaryIndexFile = new RandomAccessFile(INDEX1_PATH, "rw");
            RandomAccessFile binaryIndexByIdFile = new RandomAccessFile(INDEX_BY_ID, "rw");
            RandomAccessFile binaryIndexByGenreFile = new RandomAccessFile(INDEX_BY_GENRE, "rw");
            RandomAccessFile binaryIndexByGenreMultlistFile = new RandomAccessFile(INDEX_BY_GENRE_MULTLIST, "rw");
            
            MovieFileManager movieFileManager = new MovieFileManager(binaryDataFile);
            IndexFileManager indexFileManager = new IndexFileManager(binaryIndexFile);
            IndexIdFileManager indexByIdFileManager = new IndexIdFileManager(binaryIndexByIdFile);
            IndexFileManager indexByGenreFileManager = new IndexFileManager(binaryIndexByGenreFile);
            IndexFileManager indexByGenreMultlistFile = new IndexFileManager(binaryIndexByGenreMultlistFile);


            List<Movie> movies = movieFileManager.readMoviesByGenre(genre, indexByGenreFileManager, indexByGenreMultlistFile, indexByIdFileManager);

            Map<String, Object> response = new HashMap<>();
            response.put("records", movies);
            response.put("totalRecords", movies.size());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Retorna erro 500
        }
    }


}
