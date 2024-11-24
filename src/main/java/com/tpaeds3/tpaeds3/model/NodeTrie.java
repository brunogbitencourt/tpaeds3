package com.tpaeds3.tpaeds3.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeTrie {
    Map<Byte, NodeTrie> children; // Transições para outros nós (filhos)
    NodeTrie failureLink; // Link de falha
    List<String> output; // Lista de padrões que terminam neste nó

    public NodeTrie() {
        this.children = new HashMap<>();
        this.failureLink = null;
        this.output = new ArrayList<>();
    }
}
