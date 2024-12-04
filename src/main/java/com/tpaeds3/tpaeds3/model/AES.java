package com.tpaeds3.tpaeds3.model;

import java.util.Base64;

public class AES {
    private final byte[] key;

    // Construtor: Recebe uma chave de 16 caracteres e converte para bytes
    public AES(String key) {
        if (key.length() != 16) { // Garante que a chave tenha o tamanho correto
            throw new IllegalArgumentException("A chave deve ter exatamente 16 caracteres.");
        }
        this.key = key.getBytes(); // Converte a chave para um array de bytes
    }

    // Método para criptografar
    public String encrypt(String plainText) {
        byte[] plainBytes = plainText.getBytes(); // Converte o texto plano para bytes
        byte[] encryptedBytes = xor(plainBytes, key); // Aplica XOR no texto plano com a chave
        return Base64.getEncoder().encodeToString(encryptedBytes); // Codifica o resultado em Base64
    }

    // Método para descriptografar
    public String decrypt(String cipherText) {
        byte[] encryptedBytes = Base64.getDecoder().decode(cipherText); // Decodifica o texto cifrado de Base64 para bytes
        byte[] decryptedBytes = xor(encryptedBytes, key); // Aplica XOR novamente para reverter a cifragem
        return new String(decryptedBytes); // Converte os bytes de volta para uma string
    }

    // Função XOR: Realiza a operação bit a bit entre os dados e a chave
    private byte[] xor(byte[] data, byte[] key) {
        byte[] result = new byte[data.length]; // Array para armazenar o resultado
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]); // XOR entre o byte do dado e o byte correspondente da chave
        }
        return result; // Retorna o array com os dados criptografados/descriptografados
    }
}
