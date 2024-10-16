package com.tpaeds3.tpaeds3.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BTreeNode {
   
    int order; // Ordem da árvore
    boolean isLeaf; // Indica se é ou não folha 
    int totalElements; // Número de Elementos do nó
    long[] children; // Ponteros para armazenar filhos do nó
    String[] keys; // Chave usada para busca
    long [] positions; // Posição no arquivo base 


    public BTreeNode(int order, boolean  isLeaf){
        this.order = order;
        this.isLeaf = isLeaf;
        this.totalElements = 0;
        this.children = new long[order]; // order child
        this.keys = new String[order -1]; 
        this.positions = new long[order-1];
    }

    public void writeNodeData(DataOutputStream dos) throws IOException{
        
        dos.writeBoolean(isLeaf); // Grava se nó é folha ou não
        dos.writeInt(totalElements); // Grava total de elementos
        for(int i=0; i < totalElements; i++){// Escreve a chave
            dos.writeUTF(keys[i]); 
            dos.writeLong(positions[i]);
        }

        if(!isLeaf){
            for(int i=0; i < totalElements; i++){
                dos.writeLong(children[i]);
            }
        }
    }

    public void readNodeData(DataInputStream dis) throws IOException{

        this.isLeaf = dis.readBoolean();
        this.totalElements = dis.readInt();
        for(int i = 0; i < totalElements; i++){
            this.keys[i] = dis.readUTF();
            this.positions[i] = dis.readLong();
        }
        if(!isLeaf){
            for(int i = 0; i < totalElements; i++){
                this.children[i] = dis.readLong();
            }
        }       

    }


    public void printNode() {
        System.out.println("------------------------------------------------");
        System.out.println("Order: "+ order);
        System.out.println("Nó (total de elementos: " + totalElements + ")");
        System.out.println("É folha: " + isLeaf);
        
        // Imprime as chaves do nó (nomes dos filmes)
        System.out.print("Chaves / Pos: ");
        for (int i = 0; i < totalElements; i++) {
            System.out.print(keys[i] + " / " + positions[i] + " || ");
        }
        System.out.println();
     
        
        // Se não for folha, imprime as posições dos filhos
        if (!isLeaf) {
            System.out.print("Filhos: ");
            for (int i = 0; i <= totalElements; i++) {
                System.out.print(children[i] + " ");
            }
            System.out.println();
        }
        System.out.println("------------------------------------------------");
    }


   

    // public void printInOrder() {
    //     int i;
    
    //     // Percorre todas as chaves e filhos
    //     for (i = 0; i < totalElements; i++) {
    //         // Se não for folha, imprime os filhos antes da chave
    //         if (!isLeaf) {
    //             children[i].printInOrder();
    //         }
    //         System.out.print(keys[i] + " "); // Imprime a chave
    //     }
    
    //     // Imprime o último filho (à direita da última chave)
    //     if (!isLeaf) {
    //         children[i].printInOrder();
    //     }
    // }
    




}
