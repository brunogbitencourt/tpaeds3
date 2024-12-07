package com.tpaeds3.tpaeds3.model;

public class VigenereCipher {
    private final String key;

    /**
     * Construtor que recebe a chave para cifragem e decifragem.
     * @param key A chave usada para criptografar e descriptografar.
     */
    public VigenereCipher(String key) {
        this.key = generateKey(key);
    }

    /**
     * Método para criptografar o texto plano.
     * @param plainText O texto a ser criptografado.
     * @return O texto criptografado.
     */
    public String encrypt(String plainText) {
        StringBuilder cipherText = new StringBuilder();

        for (int i = 0; i < plainText.length(); i++) {
            char plainChar = plainText.charAt(i);
            char keyChar = key.charAt(i % key.length());

            // Aplica o deslocamento baseado na tabela ASCII
            char encryptedChar = (char) ((plainChar + keyChar) % 256);
            cipherText.append(encryptedChar);
        }

        return cipherText.toString();
    }

    /**
     * Método para descriptografar o texto cifrado.
     * @param cipherText O texto a ser descriptografado.
     * @return O texto original.
     */
    public String decrypt(String cipherText) {
        StringBuilder plainText = new StringBuilder();

        for (int i = 0; i < cipherText.length(); i++) {
            char cipherChar = cipherText.charAt(i);
            char keyChar = key.charAt(i % key.length());

            // Reverte o deslocamento baseado na tabela ASCII
            char decryptedChar = (char) ((cipherChar - keyChar + 256) % 256);
            plainText.append(decryptedChar);
        }

        return plainText.toString();
    }

    /**
     * Método auxiliar para garantir que a chave contenha apenas caracteres válidos.
     * @param key A chave original.
     * @return A chave sanitizada.
     */
    private String generateKey(String key) {
        return key.replaceAll("[^\\x00-\\xFF]", ""); // Remove caracteres fora da tabela ASCII
    }
}
