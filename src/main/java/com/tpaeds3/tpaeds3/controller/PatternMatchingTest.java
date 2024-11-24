package com.tpaeds3.tpaeds3.controller;

import java.io.RandomAccessFile;
import java.util.List;

import com.tpaeds3.tpaeds3.model.PatternMatchingManager;

public class PatternMatchingTest {

    public static void main(String[] args) {
        String fileName = "./src/main/java/com/tpaeds3/tpaeds3/files_out/movies.db";

        // O padrão que queremos encontrar (deve corresponder aos bytes após o marcador)
        byte[] pattern = {0x41, 0x76, 0x61, 0x74};

        try (RandomAccessFile file = new RandomAccessFile(fileName, "r")) {
            PatternMatchingManager manager = new PatternMatchingManager(file);
            List<Long> occurrences = manager.findPatternOccurrences(pattern);

            // Exibe as ocorrências encontradas
            if (occurrences.isEmpty()) {
                System.out.println("Nenhum padrão encontrado.");
            } else {
                System.out.println("Padrão encontrado nas posições:");
                for (Long position : occurrences) {
                    System.out.println(position);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
