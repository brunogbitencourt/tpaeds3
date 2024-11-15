package com.tpaeds3.tpaeds3.model;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class LzwManager {

    private static final int DICTIONARY_SIZE = 256;
    private static final String COMPRESSED_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/compressed/";
    private static final String COMPRESSED_BASE_NAME = "moviesCompressed";
    private final RandomAccessFile file;

    public LzwManager(RandomAccessFile file) throws IOException {
        this.file = file;
    }

    /**
     * Compacta o conteúdo do arquivo usando o algoritmo LZW e salva em um novo
     * arquivo.
     * O arquivo comprimido será salvo no caminho especificado, com um nome que
     * inclui um número de versão.
     * 
     * @throws IOException caso ocorra um erro ao ler ou salvar o arquivo
     */
    public String compress() throws IOException {
        String data = readFileContent(file);
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < DICTIONARY_SIZE; i++) {
            dictionary.put("" + (char) i, i);
        }

        String current = "";
        List<Integer> compressedData = new ArrayList<>();
        int dictSize = DICTIONARY_SIZE;

        for (char symbol : data.toCharArray()) {
            String next = current + symbol;
            if (dictionary.containsKey(next)) {
                current = next;
            } else {
                compressedData.add(dictionary.get(current));
                dictionary.put(next, dictSize++);
                current = "" + symbol;
            }
        }

        if (!current.isEmpty()) {
            compressedData.add(dictionary.get(current));
        }

        byte[] compressedBytes = convertToByteArray(compressedData);

        // Gera o nome do arquivo incrementando a versão
        int version = getNextVersion();
        String compressedFileName = COMPRESSED_BASE_NAME + version + ".lzw";
        Path compressedFilePath = Paths.get(COMPRESSED_PATH + compressedFileName);

        // Salva os dados comprimidos no arquivo
        Files.createDirectories(compressedFilePath.getParent()); // Cria diretórios, se necessário
        Files.write(compressedFilePath, compressedBytes);

        return compressedFileName;
    }

    /**
     * Descompacta o conteúdo do arquivo compactado fornecido e retorna como string.
     * 
     * @param compressedData Dados compactados em bytes
     * @return A string original descompactada
     * @throws IOException caso ocorra um erro durante a descompactação
     */
    public String decompress(byte[] compressedData) throws IOException {
        Map<Integer, String> dictionary = new HashMap<>();
        for (int i = 0; i < DICTIONARY_SIZE; i++) {
            dictionary.put(i, "" + (char) i);
        }

        List<Integer> codes = new ArrayList<>();
        for (int i = 0; i < compressedData.length; i += 2) {
            int code = ((compressedData[i] & 0xFF) << 8) | (compressedData[i + 1] & 0xFF);
            codes.add(code);
        }

        StringBuilder result = new StringBuilder(dictionary.get(codes.remove(0)));
        String currentEntry = result.toString();
        int dictSize = DICTIONARY_SIZE;

        for (int code : codes) {
            String entry;
            if (dictionary.containsKey(code)) {
                entry = dictionary.get(code);
            } else if (code == dictSize) {
                entry = currentEntry + currentEntry.charAt(0);
            } else {
                throw new IOException("Código inválido durante a descompactação");
            }

            result.append(entry);

            dictionary.put(dictSize++, currentEntry + entry.charAt(0));
            currentEntry = entry;
        }

        return result.toString();
    }

    /**
     * Lê o conteúdo do arquivo recebido e retorna como uma string.
     * 
     * @param file O arquivo a ser lido
     * @return O conteúdo do arquivo em formato de string
     * @throws IOException caso ocorra um erro ao ler o arquivo
     */
    private String readFileContent(RandomAccessFile file) throws IOException {
        StringBuilder content = new StringBuilder();
        file.seek(0); // Garante que a leitura começa do início do arquivo
        String line;
        while ((line = file.readLine()) != null) {
            content.append(line).append("\n");
        }
        return content.toString();
    }

    /**
     * Converte a lista de códigos inteiros em um array de bytes.
     * 
     * @param compressedData Lista de códigos inteiros
     * @return Array de bytes representando os dados compactados
     */
    private byte[] convertToByteArray(List<Integer> compressedData) {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            for (int code : compressedData) {
                byteStream.write((code >> 8) & 0xFF); // Byte alto
                byteStream.write(code & 0xFF); // Byte baixo
            }
            return byteStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * Obtém o próximo número de versão para o arquivo de compressão.
     * 
     * @return Próximo número de versão disponível
     */
    private int getNextVersion() {
        int version = 1;
        File directory = new File(COMPRESSED_PATH);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.startsWith(COMPRESSED_BASE_NAME));
            if (files != null && files.length > 0) {
                version = Arrays.stream(files)
                        .map(File::getName)
                        .map(name -> name.replace(COMPRESSED_BASE_NAME, "").replace(".lzw", ""))
                        .mapToInt(Integer::parseInt)
                        .max()
                        .orElse(0) + 1;
            }
        }
        return version;
    }
}
