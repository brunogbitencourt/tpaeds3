package com.tpaeds3.tpaeds3.controller;

import java.io.FileOutputStream;
import java.io.IOException;

public class TestFileGenerator {
    public static void main(String[] args) {
        String fileName = "./src/main/java/com/tpaeds3/tpaeds3/files_out/casamento_teste.db";

        
        // Conteúdo: marcador seguido de um padrão
        byte[] data = {
            (byte) 0xAB, (byte) 0xCD, (byte) 0xEF, (byte) 0x12, // Marcador
            0x41, 0x76, 0x61, 0x74,                             // Padrão "Avat"
            (byte) 0xAB, (byte) 0xCD, (byte) 0xEF, (byte) 0x12, // Marcador novamente
            0x42, 0x79, 0x65, 0x73                              // Outro padrão
        };

        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(data);
            System.out.println("Arquivo de teste criado com sucesso: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
