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
     * Método para realizar a compressão de dados utilizando o algoritmo LZW.
     * 
     * @return O nome do arquivo comprimido gerado.
     * @throws IOException Se ocorrer algum erro durante a leitura ou gravação de
     *                     dados.
     */
    public String compress() throws IOException {
        // Lê o conteúdo do arquivo de entrada como um array de bytes
        byte[] data = readFileContent(file);

        // Inicializa o dicionário com os primeiros 256 bytes
        Map<List<Byte>, Integer> dictionary = initializeDictionary();

        List<Integer> compressedData = new ArrayList<>();
        List<Byte> current = new ArrayList<>();

        // Itera sobre os bytes do arquivo e realiza a compressão
        for (byte symbol : data) {
            List<Byte> combined = new ArrayList<>(current);
            combined.add(symbol);

            if (dictionary.containsKey(combined)) {
                // Se a sequência já existe no dicionário, continua com a sequência atual
                current = combined;
            } else {
                // Caso contrário, adiciona o código da sequência atual no resultado e adiciona
                // a nova sequência no dicionário
                compressedData.add(dictionary.get(current));
                dictionary.put(combined, dictionary.size());
                current = List.of(symbol); // Reinicia a sequência com o novo byte
            }
        }

        // Adiciona o último código ao resultado
        if (!current.isEmpty()) {
            compressedData.add(dictionary.get(current));
        }

        // Converte os códigos comprimidos para bytes
        byte[] compressedBytes = convertToBytes(compressedData);
        // Escreve os bytes comprimidos em um arquivo
        String compressedFileName = writeCompressedFile(compressedBytes);

        return compressedFileName;
    }

    /**
     * Método para descomprimir os dados utilizando o algoritmo LZW.
     * 
     * @param compressedData Dados comprimidos a serem descomprimidos.
     * @return O conteúdo descomprimido como um array de bytes.
     * @throws IOException Se ocorrer algum erro durante a leitura dos dados
     *                     comprimidos.
     */
    public byte[] decompress(byte[] compressedData) throws IOException {
        // Inicializa o dicionário reverso (códigos -> sequência de bytes)
        Map<Integer, List<Byte>> dictionary = initializeReverseDictionary();

        // Converte os dados comprimidos de volta para uma lista de códigos
        List<Integer> codes = convertToIntList(compressedData);

        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
        List<Byte> previous = dictionary.get(codes.remove(0)); // A primeira sequência é inicializada com o primeiro
                                                               // código
        resultStream.write(toPrimitive(previous)); // Escreve o primeiro código descomprimido no resultado

        // Descomprime os dados com base nos códigos
        for (int code : codes) {
            List<Byte> entry;
            if (dictionary.containsKey(code)) {
                entry = dictionary.get(code);
            } else {
                // Se o código não existe no dicionário, cria uma nova entrada
                entry = new ArrayList<>(previous);
                entry.add(previous.get(0)); // Repete o primeiro byte da sequência anterior
            }

            resultStream.write(toPrimitive(entry)); // Escreve a sequência descomprimida no resultado
            // Adiciona a nova entrada ao dicionário
            List<Byte> newEntry = new ArrayList<>(previous);
            newEntry.add(entry.get(0));
            dictionary.put(dictionary.size(), newEntry);
            previous = entry; // Atualiza a sequência anterior
        }

        byte[] result = resultStream.toByteArray();
        return result;
    }

    /**
     * Lê o conteúdo de um arquivo e o converte para um array de bytes.
     * 
     * @param file O arquivo a ser lido.
     * @return O conteúdo do arquivo como um array de bytes.
     * @throws IOException Se ocorrer erro ao ler o arquivo.
     */
    private byte[] readFileContent(RandomAccessFile file) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        file.seek(0); // Move o ponteiro para o início do arquivo
        int byteRead;
        // Lê o arquivo byte a byte e escreve no ByteArrayOutputStream
        while ((byteRead = file.read()) != -1) {
            byteStream.write(byteRead);
        }
        return byteStream.toByteArray();
    }

    /**
     * Inicializa o dicionário de compressão, com os primeiros 256 bytes (todos os
     * valores possíveis de 0 a 255).
     * 
     * @return O dicionário inicial.
     */
    private Map<List<Byte>, Integer> initializeDictionary() {
        Map<List<Byte>, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < DICTIONARY_SIZE; i++) {
            dictionary.put(List.of((byte) i), i); // Adiciona os primeiros 256 bytes no dicionário
        }
        return dictionary;
    }

    /**
     * Inicializa o dicionário reverso, com os valores de código (inteiros) e as
     * sequências de bytes correspondentes.
     * 
     * @return O dicionário reverso.
     */
    private Map<Integer, List<Byte>> initializeReverseDictionary() {
        Map<Integer, List<Byte>> dictionary = new HashMap<>();
        for (int i = 0; i < DICTIONARY_SIZE; i++) {
            dictionary.put(i, List.of((byte) i)); // Adiciona os códigos de 0 a 255 no dicionário reverso
        }
        return dictionary;
    }

    /**
     * Converte um array de bytes em uma lista de códigos inteiros.
     * 
     * @param data Dados comprimidos a serem convertidos.
     * @return A lista de códigos inteiros extraídos.
     */
    private List<Integer> convertToIntList(byte[] data) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < data.length; i += 2) {
            int highByte = (data[i] & 0xFF) << 8;
            int lowByte = data[i + 1] & 0xFF;
            int code = highByte | lowByte; // Combina os dois bytes em um código inteiro
            result.add(code);
        }
        return result;
    }

    /**
     * Converte uma lista de códigos inteiros em um array de bytes.
     * 
     * @param codes Lista de códigos a ser convertida.
     * @return O array de bytes correspondente aos códigos.
     */
    private byte[] convertToBytes(List<Integer> codes) {
        if (codes == null || codes.isEmpty()) {
            throw new IllegalArgumentException("A lista de códigos está vazia ou nula.");
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        for (int code : codes) {
            byteStream.write((code >> 8) & 0xFF); // Escreve o byte alto do código
            byteStream.write(code & 0xFF); // Escreve o byte baixo do código
        }

        return byteStream.toByteArray();
    }

    /**
     * Grava os dados comprimidos em um arquivo.
     * 
     * @param data Os dados comprimidos a serem gravados.
     * @return O nome do arquivo gerado.
     * @throws IOException Se ocorrer erro ao gravar o arquivo.
     */
    private String writeCompressedFile(byte[] data) throws IOException {
        int version = getNextVersion(); // Obtém o número da versão do arquivo
        String fileName = COMPRESSED_BASE_NAME + version + ".lzw"; // Cria o nome do arquivo comprimido
        Path path = Paths.get(COMPRESSED_PATH + fileName);

        Files.createDirectories(path.getParent()); // Cria os diretórios necessários
        Files.write(path, data); // Grava os dados no arquivo

        return fileName;
    }

    /**
     * Obtém o próximo número de versão disponível para o arquivo comprimido.
     * 
     * @return O número da próxima versão.
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

    /**
     * Converte uma lista de bytes em um array primitivo.
     * 
     * @param list A lista de bytes a ser convertida.
     * @return O array de bytes.
     */
    private byte[] toPrimitive(List<Byte> byteList) {
        byte[] result = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            result[i] = byteList.get(i);
        }
        return result;
    }
}
