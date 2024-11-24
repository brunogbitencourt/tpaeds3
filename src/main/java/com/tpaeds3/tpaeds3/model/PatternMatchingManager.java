package com.tpaeds3.tpaeds3.model;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PatternMatchingManager {

    private final RandomAccessFile file;
    private static final int MARKER = 0xABCDEF12; // Marcador para início do registro

    public PatternMatchingManager(RandomAccessFile file) {
        this.file = file;
    }

    /**
     * Encontra todas as ocorrências de um padrão no arquivo usando força bruta.
     *
     * @param pattern Padrão a ser buscado (em bytes).
     * @return Lista de posições onde o padrão foi encontrado.
     * @throws IOException Se houver erro ao acessar o arquivo.
     */
    public List<Long> findPatternOccurrences(byte[] pattern) throws IOException {
        List<Long> markerPositions = new ArrayList<>();
        Set<Long> uniqueMarkers = new HashSet<>(); // Controle para evitar duplicatas
        long fileLength = file.length();

        // Verifica se o arquivo tem conteúdo
        if (fileLength == 0) {
            throw new IOException("O arquivo está vazio.");
        }

        // Lê todo o conteúdo do arquivo em um buffer
        byte[] buffer = new byte[(int) fileLength];
        file.seek(0);
        file.readFully(buffer);

        // Busca por padrão
        int bufferLength = buffer.length;
        int patternLength = pattern.length;

        for (int i = 0; i <= bufferLength - patternLength; i++) {
            if (matchesPattern(buffer, pattern, i)) {
                System.out.printf("Padrão encontrado na posição %d%n", i);

                // Encontra o marcador anterior ao padrão
                long markerPosition = findPreviousMarker(buffer, i);
                if (markerPosition != -1 && uniqueMarkers.add(markerPosition)) {
                    // Adiciona apenas marcadores únicos
                    markerPositions.add(markerPosition);
                }
            }
        }

        return markerPositions;
    }

    /**
     * Encontra todas as ocorrências de um padrão no arquivo usando o algoritmo KMP.
     *
     * @param pattern Padrão a ser buscado (em bytes).
     * @return Lista de posições onde o padrão foi encontrado.
     * @throws IOException Se houver erro ao acessar o arquivo.
     */
    public List<Long> findPatternOccurrencesWithKMP(byte[] pattern) throws IOException {
        List<Long> markerPositions = new ArrayList<>();
        Set<Long> uniqueMarkers = new HashSet<>(); // Controle para evitar duplicatas
        long fileLength = file.length();

        // Verifica se o arquivo tem conteúdo
        if (fileLength == 0) {
            throw new IOException("O arquivo está vazio.");
        }

        // Lê todo o conteúdo do arquivo em um buffer
        byte[] buffer = new byte[(int) fileLength];
        file.seek(0);
        file.readFully(buffer);

        // Calcula a tabela de prefixos do padrão
        int[] prefixTable = computePrefixTable(pattern);

        // Executa a busca usando KMP
        int i = 0; // Posição no buffer
        int j = 0; // Posição no padrão

        while (i < buffer.length) {
            if ((buffer[i] & 0xFF) == (pattern[j] & 0xFF)) {
                i++;
                j++;

                // Verifica se o padrão foi completamente encontrado
                if (j == pattern.length) {
                    System.out.printf("Padrão encontrado na posição %d%n", i - j);

                    // Encontra o marcador anterior ao padrão
                    long markerPosition = findPreviousMarker(buffer, i - j);
                    if (markerPosition != -1 && uniqueMarkers.add(markerPosition)) {
                        markerPositions.add(markerPosition);
                    }

                    // Continua buscando
                    j = prefixTable[j - 1];
                }
            } else {
                if (j != 0) {
                    j = prefixTable[j - 1];
                } else {
                    i++;
                }
            }
        }

        return markerPositions;
    }

    /**
     * Gera a tabela de prefixos para o algoritmo KMP.
     *
     * @param pattern Padrão a ser buscado.
     * @return Tabela de prefixos.
     */
    private int[] computePrefixTable(byte[] pattern) {
        int[] prefixTable = new int[pattern.length];
        int length = 0; // Comprimento do prefixo atual
        int i = 1;

        while (i < pattern.length) {
            if ((pattern[i] & 0xFF) == (pattern[length] & 0xFF)) {
                length++;
                prefixTable[i] = length;
                i++;
            } else {
                if (length != 0) {
                    length = prefixTable[length - 1];
                } else {
                    prefixTable[i] = 0;
                    i++;
                }
            }
        }

        return prefixTable;
    }

    /**
     * Procura o marcador anterior à posição do padrão.
     *
     * @param buffer  Dados do arquivo.
     * @param position Posição do padrão encontrado.
     * @return A posição do marcador anterior ou -1 se não encontrado.
     */
    private long findPreviousMarker(byte[] buffer, int position) {
        for (int i = position - 1; i >= 0; i--) {
            if (matchesMarker(buffer, i)) {
                System.out.printf("Marcador encontrado na posição %d%n", i);
                return (long) i;
            }
        }
        return -1; // Nenhum marcador encontrado antes do padrão
    }

    /**
     * Verifica se o marcador está presente em uma posição específica do buffer.
     *
     * @param buffer Dados do arquivo.
     * @param index  Posição no buffer.
     * @return True se o marcador estiver presente, False caso contrário.
     */
    private boolean matchesMarker(byte[] buffer, int index) {
        if (index + 4 > buffer.length) return false;

        int marker = ((buffer[index] & 0xFF) << 24) |
                     ((buffer[index + 1] & 0xFF) << 16) |
                     ((buffer[index + 2] & 0xFF) << 8) |
                     (buffer[index + 3] & 0xFF);

        return marker == 0xABCDEF12; // Valor do marcador
    }

    /**
     * Verifica se o padrão está presente em uma posição específica do buffer.
     *
     * @param buffer  Dados do arquivo.
     * @param pattern Padrão a ser buscado.
     * @param index   Posição inicial no buffer.
     * @return True se o padrão for encontrado, False caso contrário.
     */
    private boolean matchesPattern(byte[] buffer, byte[] pattern, int index) {
        int patternLength = pattern.length;

        if (index + patternLength > buffer.length) return false;

        for (int j = 0; j < patternLength; j++) {
            if ((buffer[index + j] & 0xFF) != (pattern[j] & 0xFF)) {
                return false;
            }
        }

        return true;
    }


    /**
     * Busca múltiplos padrões no arquivo usando Aho-Corasick.
     *
     * @param patterns Lista de padrões a serem buscados.
     * @return Lista de posições dos marcadores associados aos padrões encontrados.
     * @throws IOException Se houver erro ao acessar o arquivo.
     */
    public List<Long> findPatternOccurrencesWithAhoCorasick(List<String> patterns) throws IOException {
        Trie trie = new Trie();

        // Adiciona os padrões à Trie
        for (String pattern : patterns) {
            trie.addPattern(pattern);
        }

        // Constrói os links de falha
        trie.buildFailureLinks();

        // Lê todo o conteúdo do arquivo em um buffer
        long fileLength = file.length();
        byte[] buffer = new byte[(int) fileLength];
        file.seek(0);
        file.readFully(buffer);

        // Executa a busca
        List<Long> markerPositions = new ArrayList<>();
        Set<Long> uniqueMarkers = new HashSet<>();
        NodeTrie current = trie.getRoot();

        for (int i = 0; i < buffer.length; i++) {
            while (current != trie.getRoot() && !current.children.containsKey(buffer[i])) {
                current = current.failureLink; // Segue o link de falha
            }

            current = current.children.getOrDefault(buffer[i], trie.getRoot());

            // Verifica se padrões foram encontrados neste nó
            for (String pattern : current.output) {
                System.out.printf("Padrão '%s' encontrado na posição %d%n", pattern, i - pattern.length() + 1);

                // Encontra o marcador anterior
                long markerPosition = findPreviousMarker(buffer, i - pattern.length() + 1);
                if (markerPosition != -1 && uniqueMarkers.add(markerPosition)) {
                    markerPositions.add(markerPosition);
                }
            }
        }

        return markerPositions;
    }
}
