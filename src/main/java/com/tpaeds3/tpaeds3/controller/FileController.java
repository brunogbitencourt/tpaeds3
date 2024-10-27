package com.tpaeds3.tpaeds3.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.tpaeds3.tpaeds3.model.IndexFileManager;
import com.tpaeds3.tpaeds3.model.IndexIdFileManager;
import com.tpaeds3.tpaeds3.model.Movie;
import com.tpaeds3.tpaeds3.model.MovieFileManager;

import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/FileCreation")
@Tag(name = "01 - Criação do Banco de Dados: ")
public class FileController {

    private static final String FILE_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/movies.db";
    private static final String INDEX1_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/index01.db";
    private static final String INDEX_BY_ID = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexById.db";
    private static final String INDEX_BY_GENRE = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexByGenre.db";
    private static final String INDEX_BY_GENRE_MULTLIST = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexByGenreMultlist.db";

    /**
     * Inicializa os arquivos e gerenciadores necessários.
     */
    private Map<String, Object> initializeFiles() throws FileNotFoundException {
        Map<String, Object> resources = new HashMap<>();

        resources.put("binaryDataFile", new RandomAccessFile(FILE_PATH, "rw"));
        resources.put("binaryIndexFile", new RandomAccessFile(INDEX1_PATH, "rw"));
        resources.put("binaryIndexByIdFile", new RandomAccessFile(INDEX_BY_ID, "rw"));
        resources.put("binaryIndexByGenreFile", new RandomAccessFile(INDEX_BY_GENRE, "rw"));
        resources.put("binaryIndexByGenreMultlistFile", new RandomAccessFile(INDEX_BY_GENRE_MULTLIST, "rw"));

        resources.put("movieFileManager", new MovieFileManager((RandomAccessFile) resources.get("binaryDataFile")));
        resources.put("indexFileManager", new IndexFileManager((RandomAccessFile) resources.get("binaryIndexFile")));
        resources.put("indexByIdFileManager", new IndexIdFileManager((RandomAccessFile) resources.get("binaryIndexByIdFile")));
        resources.put("indexByGenreFileManager", new IndexFileManager((RandomAccessFile) resources.get("binaryIndexByGenreFile")));
        resources.put("indexByGenreMultlistFile", new IndexFileManager((RandomAccessFile) resources.get("binaryIndexByGenreMultlistFile")));

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

    /**
     * Método para configuração de resposta de download.
     */
    private ResponseEntity<Resource> configureDownloadResponse(File fileToReturn) throws FileNotFoundException {
        InputStreamResource resource = new InputStreamResource(new FileInputStream(fileToReturn));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileToReturn.getName());
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(fileToReturn.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/createDatabase")
    public ResponseEntity<Resource> createDataBase(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Map<String, Object> resources = null;
        try {
            resources = initializeFiles();

            MovieFileManager movieFileManager = (MovieFileManager) resources.get("movieFileManager");
            IndexFileManager indexFileManager = (IndexFileManager) resources.get("indexFileManager");
            IndexIdFileManager indexByIdFileManager = (IndexIdFileManager) resources.get("indexByIdFileManager");
            IndexFileManager indexByGenreFileManager = (IndexFileManager) resources.get("indexByGenreFileManager");
            IndexFileManager indexByGenreMultlistFile = (IndexFileManager) resources.get("indexByGenreMultlistFile");

            List<Movie> movies = parseCSV(file);
            RandomAccessFile binaryDataFile = (RandomAccessFile) resources.get("binaryDataFile");
            binaryDataFile.seek(0); // Cursor no início
            binaryDataFile.writeInt(0); // Escreve o Header

            for (Movie movie : movies) {
                long position = movieFileManager.writeMovie(movie);
                int movieId = movie.getId();
                long indexIdPosition = indexByIdFileManager.writeIndex(movieId, position);
                indexFileManager.writeIndex(movie.getName(), movieId);
                for (String genre : movie.getGenre()) {
                    long genrePosition = indexByGenreFileManager.writeGenreIndex(genre);
                    long multilistHead = indexByGenreFileManager.getMultlistHead(genrePosition);
                    indexByGenreMultlistFile.writeMultlistIndex(movieId, multilistHead, indexIdPosition, genrePosition, (RandomAccessFile) resources.get("binaryIndexByGenreFile"));

                }
            }

            // Retorna o arquivo binário
            return configureDownloadResponse(new File(FILE_PATH));

        } catch (IOException e) {
            return handleIOException(e, null);
        } finally {
            if (resources != null) closeFiles(resources);
        }
    }

    @GetMapping("/getDatabase")
    public ResponseEntity<Resource> getDatabaseFile() {
        try {
            return configureDownloadResponse(new File(FILE_PATH));
        } catch (FileNotFoundException e) {
            return handleIOException(e, null);
        }
    }

    @GetMapping("/getDatabaseIndex")
    public ResponseEntity<Resource> getIndexFile() {
        try {
            return configureDownloadResponse(new File(INDEX1_PATH));
        } catch (FileNotFoundException e) {
            return handleIOException(e, null);
        }
    }

    private List<Movie> parseCSV(MultipartFile file) throws IOException {
        List<Movie> movies = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String[] line;
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            int idCount = 1;

            while ((line = reader.readNext()) != null) {
                Movie movie = new Movie();
                movie.setId(idCount);
                movie.setName(line[0]);
                try {
                    movie.setDate(dateFormat.parse(line[1]));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                movie.setScore(Double.parseDouble(line[2]));
                movie.setGenre(Arrays.asList(line[3].split(",")));
                movie.setOverview(line[4]);
                movie.setCrew(Arrays.asList(line[5].split(",")));
                movie.setOriginTitle(line[6]);
                movie.setStatus(line[7]);
                movie.setOriginLang(line[8]);
                movie.setBudget(Double.parseDouble(line[9]));
                movie.setRevenue(Double.parseDouble(line[10]));
                movie.setCountry(line[11]);

                movies.add(movie);
                idCount++;
            }
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
        return movies;
    }
}
