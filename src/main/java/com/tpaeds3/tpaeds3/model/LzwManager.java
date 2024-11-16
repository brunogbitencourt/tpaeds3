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

    public String compress() throws IOException {
        byte[] data = readFileContent(file);
        // log("****Dados originais*****: ", Arrays.toString(data));

        Map<List<Byte>, Integer> dictionary = initializeDictionary();
        // log("Dicionário inicializado com os primeiros 256 bytes", null);

        List<Integer> compressedData = new ArrayList<>();
        List<Byte> current = new ArrayList<>();

        for (byte symbol : data) {
            List<Byte> combined = new ArrayList<>(current);
            combined.add(symbol);

            if (dictionary.containsKey(combined)) {
                current = combined;
            } else {
                compressedData.add(dictionary.get(current));
                dictionary.put(combined, dictionary.size());
                current = List.of(symbol);
            }
        }

        if (!current.isEmpty()) {
            compressedData.add(dictionary.get(current));
        }

        // log("Códigos compactados:", compressedData.toString());

        byte[] compressedBytes = convertToBytes(compressedData);
        String compressedFileName = writeCompressedFile(compressedBytes);

        // log("Arquivo compactado gerado:", compressedFileName);
        return compressedFileName;
    }

    public byte[] decompress(byte[] compressedData) throws IOException {
        // log("Bytes para descompactar: ", Arrays.toString(compressedData));

        Map<Integer, List<Byte>> dictionary = initializeReverseDictionary();
        // log("Dicionário reverso inicializado.", null);

        List<Integer> codes = convertToIntList(compressedData);
        // log("Códigos extraídos:", codes.toString());

        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
        List<Byte> previous = dictionary.get(codes.remove(0));
        resultStream.write(toPrimitive(previous));

        for (int code : codes) {
            List<Byte> entry;
            if (dictionary.containsKey(code)) {
                entry = dictionary.get(code);
            } else {
                entry = new ArrayList<>(previous);
                entry.add(previous.get(0));
            }

            resultStream.write(toPrimitive(entry));
            List<Byte> newEntry = new ArrayList<>(previous);
            newEntry.add(entry.get(0));
            dictionary.put(dictionary.size(), newEntry);
            previous = entry;
        }

        byte[] result = resultStream.toByteArray();
        // log("Dados descompactados:", Arrays.toString(result));
        return result;
    }

    private byte[] readFileContent(RandomAccessFile file) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        file.seek(0);
        int byteRead;
        while ((byteRead = file.read()) != -1) {
            byteStream.write(byteRead);
        }
        return byteStream.toByteArray();
    }

    private Map<List<Byte>, Integer> initializeDictionary() {
        Map<List<Byte>, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < DICTIONARY_SIZE; i++) {
            dictionary.put(List.of((byte) i), i);
        }
        return dictionary;
    }

    private Map<Integer, List<Byte>> initializeReverseDictionary() {
        Map<Integer, List<Byte>> dictionary = new HashMap<>();
        for (int i = 0; i < DICTIONARY_SIZE; i++) {
            dictionary.put(i, List.of((byte) i));
        }
        return dictionary;
    }

    private List<Integer> convertToIntList(byte[] data) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < data.length; i += 2) {
            int highByte = (data[i] & 0xFF) << 8;
            int lowByte = data[i + 1] & 0xFF;
            int code = highByte | lowByte;
            result.add(code);
        }
        return result;
    }

    private byte[] convertToBytes(List<Integer> codes) {
        if (codes == null || codes.isEmpty()) {
            throw new IllegalArgumentException("A lista de códigos está vazia ou nula.");
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        for (int code : codes) {
            byteStream.write((code >> 8) & 0xFF);
            byteStream.write(code & 0xFF);
        }

        return byteStream.toByteArray();
    }

    private String writeCompressedFile(byte[] data) throws IOException {
        int version = getNextVersion();
        String fileName = COMPRESSED_BASE_NAME + version + ".lzw";
        Path path = Paths.get(COMPRESSED_PATH + fileName);

        Files.createDirectories(path.getParent());
        Files.write(path, data);

        return fileName;
    }

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

    private byte[] toPrimitive(List<Byte> byteList) {
        byte[] result = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            result[i] = byteList.get(i);
        }
        return result;
    }

    // private void log(String message, String data) {
    //     System.out.println(message + (data != null ? " " + data : ""));
    // }
}
