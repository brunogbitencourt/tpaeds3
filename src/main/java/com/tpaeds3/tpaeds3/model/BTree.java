package com.tpaeds3.tpaeds3.model;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;

public class BTree {

    private final int order; // Ordem da árvore
    private BTreeNode root; // Raiz da árvore
    private final RandomAccessFile nodeFile; // Arquivo para armazenar os nós da árvore
    private long rootPosition; // Posição da raiz no arquivo
    private static final Logger logger = Logger.getLogger(BTree.class.getName());

    public BTree(int order, String indexFileName) throws FileNotFoundException, IOException {
        // Configuração do logger
        logger.setLevel(Level.ALL);
        FileHandler fileHandler = new FileHandler("./src/main/java/com/tpaeds3/tpaeds3/files_out/logTree.log", true);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);

        this.root = null;
        this.order = order;
        File indexFile = new File(indexFileName);
        if (indexFile.exists()) {
            // Apaga o arquivo se existir
            boolean deleted = indexFile.delete();
            if (deleted) {
                logger.info("Arquivo de índice existente foi apagado: " + indexFileName);
            } else {
                logger.warning("Não foi possível apagar o arquivo de índice: " + indexFileName);
            }
        }


        this.nodeFile = new RandomAccessFile(indexFileName, "rw");

        


        // Verifica se o arquivo contém a posição da raiz
        if (nodeFile.length() == 0) {
            this.root = null;
            this.rootPosition = -1;
        } else {
            // Lê a posição da raiz armazenada
            nodeFile.seek(0); // O rootPosition está armazenado na primeira posição
            rootPosition = nodeFile.readLong(); // Lê a posição da raiz
            if (rootPosition != -1) {
                root = loadNodeFromFile(rootPosition); // Carrega a raiz a partir do arquivo
            }
        }
    }

    // Insere uma chave em um nó que não está completo
    // Insere uma chave em um nó que não está completo
    // Insere uma chave em um nó que não está completo
    public void InsertNonCompleteNode(BTreeNode node, long nodePosition, String key, long position) throws IOException {
        int i = node.totalElements - 1;
    
        logger.info("Inserindo chave: " + key + " no nó com totalElements: " + node.totalElements);
    
        if (node.isLeaf) {
            // Insere a chave em um nó folha
            while (i >= 0 && key.compareTo(node.keys[i]) < 0) {
                node.keys[i + 1] = node.keys[i];
                node.positions[i + 1] = node.positions[i];
                i--;
            }
            node.keys[i + 1] = key;
            node.positions[i + 1] = position;
            node.totalElements++;
    
            // Salva o nó atualizado
            saveNodeToFile(node, nodePosition);
            logger.info("Chave inserida no nó folha: " + key);
        } else {
            // Encontra o índice do filho a ser visitado
            while (i >= 0 && key.compareTo(node.keys[i]) < 0) {
                i--;
            }
            i++; // Acha o filho correto
    
            logger.info("Carregando filho no índice: " + i);
            BTreeNode childNode = loadNodeFromFile(node.children[i]);
    
            if (childNode == null) {
                logger.severe("Tentativa de acessar um nó filho nulo no índice: " + i);
                return; // Evita chamada recursiva com nó nulo
            }
    
            // Se o nó filho está cheio, dividimos o nó
            if (childNode.totalElements == order - 1) {
                splitChild(node, i, childNode);
    
                // Após a divisão, atualiza o índice do filho correto
                if (key.compareTo(node.keys[i]) > 0) {
                    i++;
                }
            }
    
            // Carrega o filho atualizado após a divisão
            BTreeNode updatedChildNode = loadNodeFromFile(node.children[i]);
            if (updatedChildNode != null) {
                InsertNonCompleteNode(updatedChildNode, node.children[i], key, position);
            } else {
                logger.severe("O nó filho atualizado é nulo após a divisão.");
            }
        }
    }
    
    
    public void splitChild(BTreeNode parent, int i, BTreeNode fullChild) throws IOException {
        BTreeNode newChild = new BTreeNode(order, fullChild.isLeaf);
        int middle = (order - 1) / 2; // Índice do meio
    
        // Calcula o número de elementos que vão para o novo nó (direito)
        newChild.totalElements = fullChild.totalElements - (middle + 1); // total - (middle + 1)
    
        // Verifica se newChild.totalElements é negativo
        if (newChild.totalElements < 0) {
            newChild.totalElements = 0; // Garantindo que não fique negativo
        }
    
        // Copia as chaves e posições para o novo nó (direito)
        for (int j = 0; j < newChild.totalElements; j++) {
            newChild.keys[j] = fullChild.keys[middle + 1 + j]; // Começa após o middle
            newChild.positions[j] = fullChild.positions[middle + 1 + j];
        }
    
        // Copia os filhos, se não for folha
        if (!fullChild.isLeaf) {
            for (int j = 0; j <= newChild.totalElements; j++) {
                newChild.children[j] = fullChild.children[middle + 1 + j]; // Filhos correspondentes ao novo nó
            }
        }
    
        // Atualiza o totalElements do nó cheio (esquerdo) e remove a chave do meio
        fullChild.totalElements = middle; // O nó cheio (esquerdo) agora tem o número correto de elementos
    
        // Remove a chave do meio do nó cheio
        for (int j = middle + 1; j < fullChild.totalElements + 1; j++) {
            if (j - 1 < fullChild.totalElements) { // Adiciona verificação para evitar acesso fora dos limites
                fullChild.keys[j - 1] = fullChild.keys[j]; // Desloca as chaves para a esquerda
                fullChild.positions[j - 1] = fullChild.positions[j]; // Desloca as posições para a esquerda
            }
        }
    
        // Move os filhos do pai para abrir espaço para o novo nó
        for (int j = parent.totalElements; j >= i + 1; j--) {
            parent.children[j + 1] = parent.children[j];
        }
    
        long newChildPos = saveNodeToFile(newChild, -1); // Salva o novo filho no arquivo
        parent.children[i + 1] = newChildPos; // Atualiza com a posição do novo filho
    
        // Mover as chaves do pai para abrir espaço
        for (int j = parent.totalElements - 1; j >= i; j--) {
            parent.keys[j + 1] = parent.keys[j];
            parent.positions[j + 1] = parent.positions[j];
        }
    
        // A chave do meio sobe para o pai
        parent.keys[i] = fullChild.keys[middle]; // A chave que sobe
        parent.positions[i] = fullChild.positions[middle];
        parent.totalElements++;
    
        // Salvar os nós
        saveNodeToFile(fullChild, -1); // Salva o nó esquerdo (atualizado)
        saveNodeToFile(parent, -1);    // Salva o nó pai
    }
    
    
    
    
    

    // Salva um nó no arquivo e retorna sua posição
    private long saveNodeToFile(BTreeNode node, long position) throws IOException {
        if (position == -1) {
            position = nodeFile.length();  // Novo nó, posicione no fim do arquivo
        }
        nodeFile.seek(position);
    
        logger.info("Salvando nó com totalElements: " + node.totalElements + " na posição: " + position);
    
        // Salva os dados do nó
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(byteStream)) {
            node.writeNodeData(dos);
            nodeFile.write(byteStream.toByteArray()); // Escreve os bytes diretamente no RandomAccessFile
        }
    
        return position;
    }
    

    // Insere uma chave na árvore
    public void InsertKey(String key, long position) throws IOException {
        // Raiz está vazia
        if (root == null) {
            root = new BTreeNode(this.order, true);
            root.keys[0] = key;
            root.positions[0] = position;
            root.totalElements++;
            rootPosition = saveNodeToFile(root, -1); // Raiz -1
            updateRootPosition(rootPosition);
            logger.info("Criando nova raiz com a chave: " + key);
        } else {
            // Se a raiz está cheia
            if (root.totalElements == order - 1) {
                // Verifica se o filho direto da raiz tem espaço antes de dividir a raiz
                BTreeNode childNode = loadNodeFromFile(root.children[0]);
    
                if (childNode != null && childNode.totalElements == order - 1) {
                    // Dividir a raiz apenas se o filho também estiver cheio
                    BTreeNode newRoot = new BTreeNode(order, false);
                    long newRootPos = saveNodeToFile(newRoot, -1); // Grava um novo nó raiz
    
                    // Antigo Nó Raiz se torna filho do novo nó
                    newRoot.children[0] = rootPosition;
    
                    // Divide a raiz antiga
                    splitChild(newRoot, 0, root);
    
                    // Agora o novo nó raiz terá dois filhos. Precisamos inserir a nova chave no filho correto.
                    int i = 0;
                    if (newRoot.keys[0].compareTo(key) < 0) {
                        i++;
                    }
    
                    InsertNonCompleteNode(newRoot, newRoot.children[i], key, position);
    
                    // Atualiza a raiz para o novo nó
                    root = newRoot;
                    rootPosition = newRootPos;
                    updateRootPosition(rootPosition);
                    logger.info("Nova raiz criada após divisão.");
                } else {
                    // Caso o filho ainda tenha espaço, insere diretamente no filho
                    InsertNonCompleteNode(root, rootPosition, key, position);
                }
            } else {
                // Se a raiz não está cheia, insere diretamente
                InsertNonCompleteNode(root, rootPosition, key, position);
            }
        }
    }
    

    // Carrega um nó a partir do arquivo
    private BTreeNode loadNodeFromFile(long position) throws IOException {
        nodeFile.seek(position);
    
        byte[] buffer = new byte[(int)(nodeFile.length() - position)];
        nodeFile.read(buffer);
    
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer);
             DataInputStream dis = new DataInputStream(byteStream)) {
            BTreeNode node = new BTreeNode(order, true);  // Temporário, será corrigido no readNodeData
            node.readNodeData(dis);
    
            logger.info("Carregado nó com totalElements: " + node.totalElements + " da posição: " + position);
            
            return node;
        }
    }
    

    // Imprime a árvore B
    public void printBTree() {
        if (root != null) {
            printNode(rootPosition);
        } else {
            logger.info("A árvore está vazia.");
        }
    }

    // Imprime um nó e seus filhos
    private void printNode(long position) {
        try {
            BTreeNode node = loadNodeFromFile(position);
            if (node == null) {
                logger.severe("Nó carregado é nulo na posição: " + position);
                return; // Retorna se o nó carregado for nulo
            }
    
            System.out.println("------------------------------------------------");
            System.out.println("Order: " + this.order);
            System.out.println("Nó (total de elementos: " + node.totalElements + ")");
            System.out.println("É folha: " + node.isLeaf);
            
            // Imprime as chaves e posições
            System.out.print("Chaves / Pos: ");
            for (int i = 0; i < node.totalElements; i++) {
                System.out.print(node.keys[i] + " / " + node.positions[i] + " ");
            }
            System.out.println();
    
            // Imprime os filhos se houver
            if (!node.isLeaf) {
                System.out.print("Filhos: ");
                for (int i = 0; i <= node.totalElements; i++) {
                    // Verifique se a posição do filho é válida antes de chamar recursivamente
                    if (node.children[i] != 0) { // A posição 0 pode indicar um filho nulo
                        System.out.print(node.children[i] + " ");
                        printNode(node.children[i]); // Chamada recursiva
                    } else {
                        System.out.println("Filho nulo na posição " + i);
                    }
                }
                System.out.println();
            }
    
            System.out.println("------------------------------------------------");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro ao imprimir nó: ", e);
        }
    }
    
    
    

    // Fecha o arquivo
    public void close() throws IOException {
        if (nodeFile != null) {
            nodeFile.close();
        }
    }

    // Atualiza a posição da raiz
    private void updateRootPosition(long rootPosition) throws IOException {
        // Atualiza a posição da raiz na primeira linha do arquivo
        nodeFile.seek(0);
        nodeFile.writeLong(rootPosition);
    }
}
