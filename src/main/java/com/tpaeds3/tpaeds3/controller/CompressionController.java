package com.tpaeds3.tpaeds3.controller;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tpaeds3.tpaeds3.model.LzwManager;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lzw")
@Tag(name = "IV - Operações de Compressão de Arquivo (LZW)")
public class CompressionController {

    private static final String MOVIE_DB_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/movies.db";
    private static final String COMPRESSED_FILE_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/compressed/";

    @GetMapping("/compress")
    @Operation(summary = "Compactar arquivo", description = "Compacta o arquivo de filmes `movies.db` utilizando o algoritmo LZW e retorna o tempo de execução e a taxa de compressão.")
    @ApiResponse(responseCode = "200", description = "Arquivo compactado com sucesso.", content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Tempo de execução: 120 ms. Compressão: 75.00%")))
    @ApiResponse(responseCode = "500", description = "Erro durante a compressão do arquivo.", content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Erro na compressão do arquivo.")))
    public ResponseEntity<String> compressFile() {
        long startTime = System.nanoTime();
        try (RandomAccessFile binaryFile = new RandomAccessFile(MOVIE_DB_PATH, "r")) {
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
    @Operation(summary = "Descompactar arquivo", description = "Descompacta o arquivo compactado na versão especificada utilizando o algoritmo LZW e substitui o arquivo original `movies.db`.")
    @ApiResponse(responseCode = "200", description = "Arquivo descompactado com sucesso.", content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Arquivo descompactado e substituído com sucesso!")))
    @ApiResponse(responseCode = "404", description = "Arquivo compactado não encontrado para a versão especificada.", content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Arquivo comprimido não encontrado para a versão 1.")))
    @ApiResponse(responseCode = "500", description = "Erro durante a descompressão do arquivo.", content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Erro ao descompactar o arquivo.")))
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

     // Método para buscar todas as versões e suas respectivas datas de modificação
    @GetMapping("/versions")
    @Operation(summary = "Obter todas as versões disponíveis", description = "Retorna as versões dos arquivos comprimidos junto com as datas da última modificação.")
    @ApiResponse(responseCode = "200", description = "Versões obtidas com sucesso.", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "Nenhuma versão encontrada.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<Map<Integer, String>> getVersions() {
        Map<Integer, String> versions = new HashMap<>();
        File directory = new File(COMPRESSED_FILE_PATH);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.startsWith("moviesCompressed") && name.endsWith(".lzw"));
            
            if (files != null && files.length > 0) {
                versions = Arrays.stream(files)
                    .map(File::getName)
                    .map(name -> name.replace("moviesCompressed", "").replace(".lzw", ""))
                    .mapToInt(Integer::parseInt)
                    .boxed()
                    .collect(Collectors.toMap(
                        version -> version, 
                        version -> getFileLastModifiedTime("moviesCompressed" + version + ".lzw")
                    ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyMap());
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyMap());
        }

        return ResponseEntity.ok(versions);
    }

    private String getFileLastModifiedTime(String fileName) {
        File file = new File(COMPRESSED_FILE_PATH + fileName);
        if (file.exists()) {
            long lastModifiedMillis = file.lastModified();
            Date lastModifiedDate = new Date(lastModifiedMillis);
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(lastModifiedDate);
        }
        return "Data não disponível";
    }
}
