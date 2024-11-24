package com.tpaeds3.tpaeds3.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tpaeds3.tpaeds3.model.Movie;
import com.tpaeds3.tpaeds3.model.MovieDBFileManager;
import com.tpaeds3.tpaeds3.model.PatternMatchingManager;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/PatternMatching")
@Tag(name = "V - Casamento de Padrões")
public class PatternMatchingController {

    private static final String MOVIE_DB_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/movies.db";

    /**
     * Inicializa os arquivos e gerenciadores necessários.
     */
    private Map<String, Object> initializeFiles() throws FileNotFoundException {
        Map<String, Object> resources = new HashMap<>();

        // Inicializa o arquivo binário de filmes
        resources.put("binaryMovieDBFile", new RandomAccessFile(MOVIE_DB_PATH, "r"));
        resources.put("movieDBFileManager",
                new MovieDBFileManager((RandomAccessFile) resources.get("binaryMovieDBFile")));

        return resources;
    }

    /**
     * Fecha todos os arquivos `RandomAccessFile`.
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
     * Tratamento centralizado de exceções.
     */
    private <T> ResponseEntity<T> handleIOException(IOException e, T body) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @Operation(summary = "Busca filmes pelo padrão")
    @GetMapping("/findMoviesByPattern")
    public ResponseEntity<?> findMoviesByPattern(@RequestParam("pattern") String pattern) {
        Map<String, Object> resources = null;
        try {
            resources = initializeFiles();
    
            RandomAccessFile binaryMovieDBFile = (RandomAccessFile) resources.get("binaryMovieDBFile");
            MovieDBFileManager movieDBFileManager = (MovieDBFileManager) resources.get("movieDBFileManager");
    
            // Lista para armazenar informações de diferentes algoritmos
            List<Map<String, Object>> algorithms = new ArrayList<>();
    
            // Filmes encontrados (evitar duplicatas)
            Set<Long> uniqueMarkerPositions = new HashSet<>();
            List<Movie> movies = new ArrayList<>();
    
            // Algoritmo 1: BrutalForce
            PatternMatchingManager patternMatchingManager = new PatternMatchingManager(binaryMovieDBFile);
            byte[] patternBytes = pattern.getBytes(StandardCharsets.UTF_8);
    
            long startTime = System.currentTimeMillis();
            List<Long> brutalForcePositions = patternMatchingManager.findPatternOccurrences(patternBytes);
            long endTime = System.currentTimeMillis();
    
            // Adiciona informações do algoritmo "BrutalForce"
            Map<String, Object> brutalForceInfo = new HashMap<>();
            brutalForceInfo.put("name", "BrutalForce");
            brutalForceInfo.put("executionTime", (endTime - startTime) + " ms");
            brutalForceInfo.put("totalMovies", brutalForcePositions.size());
            algorithms.add(brutalForceInfo);


            // Algoritmo 2: KMP
            long startTimeKMP = System.currentTimeMillis();
            List<Long> kmpPositions = patternMatchingManager.findPatternOccurrencesWithKMP(patternBytes);
            long endTimeKMP = System.currentTimeMillis();

            // Adiciona informações do algoritmo "KMP"
            Map<String, Object> kmpInfo = new HashMap<>();
            kmpInfo.put("name", "KMP");
            kmpInfo.put("executionTime", (endTimeKMP - startTimeKMP) + " ms");
            kmpInfo.put("totalMovies", kmpPositions.size());
            algorithms.add(kmpInfo);

            // Algoritmo 3: Aho-Corasick
            long startTimeAhoCorasick = System.currentTimeMillis();
            List<Long> ahoCorasickPositions = patternMatchingManager.findPatternOccurrencesWithAhoCorasick(List.of(pattern));
            long endTimeAhoCorasick = System.currentTimeMillis();

            Map<String, Object> ahoCorasickInfo = new HashMap<>();
            ahoCorasickInfo.put("name", "Aho-Corasick");
            ahoCorasickInfo.put("executionTime", (endTimeAhoCorasick - startTimeAhoCorasick) + " ms");
            ahoCorasickInfo.put("totalMovies", ahoCorasickPositions.size());
            algorithms.add(ahoCorasickInfo);

            uniqueMarkerPositions.addAll(ahoCorasickPositions);

            // Adiciona as posições encontradas pelo KMP ao conjunto de marcadores únicos  
            //uniqueMarkerPositions.addAll(kmpPositions);
    
            // Recupera os filmes a partir dos marcadores únicos
            for (Long position : uniqueMarkerPositions) {
                try {
                    Movie movie = movieDBFileManager.getMovieByPosition(position);
                    movies.add(movie);
                } catch (Exception e) {
                    System.err.printf("Erro ao recuperar filme na posição %d: %s%n", position, e.getMessage());
                }
            }
    
            // Construindo a resposta final com LinkedHashMap para garantir a ordem
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("algorithms", algorithms); // Informações dos algoritmos aparecem primeiro
            response.put("movies", movies);         // Lista única de filmes encontrados
    
            return ResponseEntity.ok(response);
    
        } catch (IOException e) {
            return handleIOException(e, null);
        } finally {
            if (resources != null) {
                closeFiles(resources);
            }
        }
    }    

}
