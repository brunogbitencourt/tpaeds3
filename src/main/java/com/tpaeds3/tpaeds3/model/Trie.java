package com.tpaeds3.tpaeds3.model;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Trie {
    private final NodeTrie root;

    public Trie() {
        this.root = new NodeTrie();
    }

    /**
     * Adiciona um padrão à Trie.
     * 
     * @param pattern Padrão a ser adicionado.
     */
    public void addPattern(String pattern) {
        NodeTrie current = root;
        byte[] bytes = pattern.getBytes();

        for (byte b : bytes) {
            current = current.children.computeIfAbsent(b, k -> new NodeTrie());
        }

        current.output.add(pattern); // Marca o padrão como terminando neste nó
    }

    /**
     * Constrói os links de falha após adicionar os padrões.
     */
    public void buildFailureLinks() {
        Queue<NodeTrie> queue = new LinkedList<>();
        root.failureLink = root;

        // Inicializa links de falha para filhos diretos do root
        for (NodeTrie child : root.children.values()) {
            child.failureLink = root;
            queue.add(child);
        }

        // Processa a árvore para definir os links de falha
        while (!queue.isEmpty()) {
            NodeTrie current = queue.poll();

            for (Map.Entry<Byte, NodeTrie> entry : current.children.entrySet()) {
                byte b = entry.getKey();
                NodeTrie child = entry.getValue();

                NodeTrie fail = current.failureLink;
                while (fail != root && !fail.children.containsKey(b)) {
                    fail = fail.failureLink;
                }

                if (fail.children.containsKey(b) && fail.children.get(b) != child) {
                    child.failureLink = fail.children.get(b);
                } else {
                    child.failureLink = root;
                }

                child.output.addAll(child.failureLink.output); // Herda padrões do nó de falha
                queue.add(child);
            }
        }
    }

    /**
     * Retorna o nó raiz da Trie.
     */
    public NodeTrie getRoot() {
        return root;
    }
}
