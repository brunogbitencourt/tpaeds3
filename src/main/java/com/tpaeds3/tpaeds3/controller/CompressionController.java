package com.tpaeds3.tpaeds3.controller;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.tpaeds3.tpaeds3.model.LzwManager;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/compression")
@Tag(name = "04 - LZW: Compressão de Arquivo")
public class CompressionController {

    private static final String MOVIE_DB_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/movies.db";
    private static final String COMPRESSED_FILE_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/compressed/";

    @GetMapping("/compress")
    public ResponseEntity<String> compressFile() {
        long startTime = System.nanoTime();
        try (RandomAccessFile binaryFile = new RandomAccessFile(MOVIE_DB_PATH, "r")){
            // Compacta o arquivo usando LZW
            LzwManager lzwManager = new LzwManager(binaryFile);
            String compressedFileName = lzwManager.compress();

            // Calcula o tempo de execução em milissegundos
            long endTime = System.nanoTime();
            long durationInMillis = (endTime - startTime) / 1_000_000;

            // Calcula o tamanho dos arquivos original e comprimido
            File originalFile = new File(MOVIE_DB_PATH);
            File compressedFile = new File(COMPRESSED_FILE_PATH + compressedFileName);
            long originalSize = originalFile.length();
            long compressedSize = compressedFile.length();

            // Calcula o percentual de compressão
            double compressionRate = 100.0 * (originalSize - compressedSize) / originalSize;
            String resultMessage = String.format("Tempo de execução: %d ms. Compressão: %.2f%%",
                    durationInMillis, compressionRate);

            return ResponseEntity.ok(resultMessage);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro na compressão do arquivo.");
        } finally {
            // if (resources != null) closeFiles(resources);
        }
    }

    @GetMapping("/decompress")
    public ResponseEntity<String> decompressFile(@RequestParam int version) {
        try {
            // Define o nome do arquivo comprimido com base na versão
            String compressedFileName = "moviesCompressed" + version + ".lzw";
            File compressedFile = new File(COMPRESSED_FILE_PATH + compressedFileName);

            // Verifica se o arquivo existe
            if (!compressedFile.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Arquivo comprimido não encontrado para a versão " + version);
            }

            // Descompacta o arquivo
            LzwManager lzwManager = new LzwManager(null);
            byte[] compressedData = Files.readAllBytes(compressedFile.toPath());
            byte[] decompressedData = lzwManager.decompress(compressedData);

            // Substitui o arquivo movies.db pelo conteúdo descompactado
            Files.write(Paths.get(MOVIE_DB_PATH), decompressedData);

            return ResponseEntity.ok("Arquivo descompactado e substituído com sucesso!");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao descompactar o arquivo.");
        }
    }
}
