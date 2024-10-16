package com.tpaeds3.tpaeds3.model;

import java.io.IOException;

public class BTreeTest {
    public static void main(String[] args) {
        try {
            BTree bTree = new BTree(3, "./src/main/java/com/tpaeds3/tpaeds3/files_out/index.dat");

            bTree.InsertKey("Filme A", 0);
            bTree.InsertKey("Filme B", 1);
            bTree.InsertKey("Filme C", 2);
            bTree.InsertKey("Filme D", 3);
            bTree.InsertKey("Filme E", 4);
            bTree.InsertKey("Filme F", 5);

            bTree.printBTree(); 

            // Feche o arquivo da árvore B
            bTree.close(); // Use o método close
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
