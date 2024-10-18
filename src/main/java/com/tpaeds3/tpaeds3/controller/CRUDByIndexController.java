package com.tpaeds3.tpaeds3.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tpaeds3.tpaeds3.model.IndexFileManager;
import com.tpaeds3.tpaeds3.model.Movie;
import com.tpaeds3.tpaeds3.model.MovieFileManager;

import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/CRUDByIndex")
@Tag(name = "03 - Operações CRUD com Índice: ")
public class CRUDByIndexController {

    private static final String FILE_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/movies.db";
    private static final String INDEX1_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/index01.db";

    
    @GetMapping("/getMovieByName")
    public ResponseEntity<Movie> getMovieByName(@RequestParam String param) throws FileNotFoundException {
        try{
            RandomAccessFile binaryIndexFile = new RandomAccessFile(INDEX1_PATH, "r");
            IndexFileManager indexFileManager =  new IndexFileManager(binaryIndexFile);
           

            long position = indexFileManager.findPositionById(param);
            if (position == -1 )
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            RandomAccessFile binaryMovieFile = new RandomAccessFile(FILE_PATH, "r");            
            MovieFileManager movieFileManager = new MovieFileManager(binaryMovieFile);

            Movie movie = movieFileManager.getMoveByPosition(position); 

            return ResponseEntity.ok(movie);
        
        
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

}