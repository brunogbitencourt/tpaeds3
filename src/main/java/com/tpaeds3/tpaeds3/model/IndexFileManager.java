package com.tpaeds3.tpaeds3.model;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class IndexFileManager {

    private RandomAccessFile indexFile;
    private static final byte VALID_RECORD = 0x01;
    private static final byte DELETED_RECORD = 0x00;


    public IndexFileManager(RandomAccessFile indexFile){
        this.indexFile = indexFile;
    }

    public RandomAccessFile getIndexFile() {
        return this.indexFile;
    }


    public long writeIndex(String key, int movieId) throws IOException {
        try {
            int keyLength = key.getBytes(StandardCharsets.UTF_8).length;
            
            // Soma dois bytes para o tamanho da string
            // Padrão da biblioteca de read write do arquivo
            int recordSize = keyLength + Integer.BYTES + 2; 
            long positionInserted = indexFile.length();

            indexFile.seek(positionInserted); 
    
            indexFile.writeByte(VALID_RECORD);
    
            indexFile.writeInt(recordSize);
    
            indexFile.writeUTF(key);
    
            indexFile.writeInt(movieId);
    
            return positionInserted;
        } catch (Exception e) {
            return -1;
        }
    }
    
    

    public int findIdByMovieName(String name) throws IOException {
        while (indexFile.getFilePointer() < indexFile.length()) {
            
            byte recordStatus = indexFile.readByte();  // Lê a lápide
            int recordSize = indexFile.readInt();      // Lê o tamanho do registro    
            
            long currentPointer = indexFile.getFilePointer();

            // Lê a chave (nome do filme)
            String movieName = indexFile.readUTF();
    
            // Lê o id do filme
            int movieId = indexFile.readInt();
    
            // Posição do próximo registro:
            long nextRecordPosition = currentPointer + recordSize;
    
            // Se o registro for válido e a chave for igual, retorne a posição
            if (recordStatus == VALID_RECORD && movieName.equals(name)) {
                return movieId;
            }
    
            // Move o ponteiro para o próximo registro 
            indexFile.seek(nextRecordPosition);
        }
    
        return -1;  // Retorna -1 se a chave não for encontrada
    }

    
    public long findIndexPositionByKey(String key) throws IOException {
        // Reinicia o ponteiro para o início do arquivo de índice
        indexFile.seek(0); 
    
        while (indexFile.getFilePointer() < indexFile.length()) {
            byte recordStatus = indexFile.readByte();  // Lê a lápide
            int recordSize = indexFile.readInt();      // Lê o tamanho do registro    
    
            // Lê a chave (nome do filme)
            String movieName = indexFile.readUTF();
    
            // Lê o id do filme 
            @SuppressWarnings("unused")
            int position = indexFile.readInt();
    
            // Se o registro for válido e a chave for igual, retorne a posição
            if (recordStatus == VALID_RECORD && movieName.equals(key)) {
                // Retorna a posição do início do registro
                return indexFile.getFilePointer() - (Integer.BYTES + movieName.getBytes(StandardCharsets.UTF_8).length + 2 + 4 + 1);
            }
    
            // Move o ponteiro para o próximo registro
            indexFile.skipBytes(recordSize - (Integer.BYTES + movieName.getBytes(StandardCharsets.UTF_8).length + 2)); 
        }
    
        return -1;  // Retorna -1 se a chave não for encontrada
    }

    public long updateGenreIndex(String genre, int movieId, long movieIndexPosition, RandomAccessFile genreIndexFile, RandomAccessFile genreMultlistFile) throws IOException {
        long genrePosition  = findGenrePosition(genre);

        if(genrePosition == -1){
            genrePosition = writeNewGenre(genre, genreIndexFile);
        }

        long listHead = getMultlistHead(genrePosition);

        long newListPosition = writeMultlistIndex(movieId, listHead, movieIndexPosition, genrePosition, genreIndexFile, genreMultlistFile);

        updateGenreListPointer(genreIndexFile, genrePosition, newListPosition);

        return newListPosition;

    }


    public void removeMovieFromGenre(String genre, int movieId, RandomAccessFile genreIndexFile, RandomAccessFile genreMultlistFile) throws IOException {
        long genrePosition = findGenrePosition(genre);
        if (genrePosition == -1) return;  // Gênero não existe no índice
    
        // Obter o cabeçalho da lista encadeada
        long currentPosition = getMultlistHead(genrePosition);
        long previousPosition = -1;
    
        while (currentPosition != -1) {
            genreMultlistFile.seek(currentPosition);
            byte isValid = genreMultlistFile.readByte();
            int currentMovieId = genreMultlistFile.readInt();
            long indexPosition = genreMultlistFile.readLong();  
            long nextPosition = genreMultlistFile.readLong();
            
            if (isValid == VALID_RECORD && currentMovieId == movieId) {
                // Remover o filme, ajustando o ponteiro do nó anterior
                if (previousPosition == -1) {
                    // Atualizar o cabeçalho da lista no índice de gênero
                    updateGenreListPointer(genreIndexFile, genrePosition, nextPosition);
                } else {
                    // Atualizar o próximo do nó anterior
                    genreMultlistFile.seek(previousPosition + 13); // Pula lápide, ID do filme e posição do índice do filme
                    genreMultlistFile.writeLong(nextPosition);
                }
                return; // Filme removido da lista
            }
            // Continuar para o próximo nó
            previousPosition = currentPosition;
            currentPosition = nextPosition;
        }
    }
    

    private long writeNewGenre(String genre, RandomAccessFile genreIndexFile) throws IOException {
        long position = genreIndexFile.length();
        genreIndexFile.seek(position);
    
        genreIndexFile.writeByte(VALID_RECORD); // Marca como válido
        genreIndexFile.writeUTF(genre);         // Escreve o gênero
        genreIndexFile.writeLong(-1);           // Cabeçalho da lista (nenhum filme ainda)
        genreIndexFile.writeInt(0);             // Contador de elementos
    
        return position;
    }
    
    
    
    public boolean updateIndex(String oldKey, String newKey, long newPosition) throws IOException {
        long position = findIndexPositionByKey(oldKey);
        
        if (position == -1) {
            return false; // Registro não encontrado
        }
    
        indexFile.seek(position); // Volta para o início do registro
    
        // Lê a lápide
        byte recordStatus = indexFile.readByte(); // Lê a lápide
        // Valida se a lápide é válida
        if (recordStatus != VALID_RECORD) {
            return false; // Registro não é válido, não pode ser atualizado
        }
        
        // Lê o id do filme
        int movieId = indexFile.readInt();

        int newKeyLength = newKey.getBytes(StandardCharsets.UTF_8).length;
        int oldKeyLength = oldKey.getBytes(StandardCharsets.UTF_8).length;
        int newRecordSize = newKeyLength + Integer.BYTES + 2; // Calcula o novo tamanho do registro (nova chave e posição)
    
        // Verifica se a nova chave é maior que a antiga
        if (newKeyLength > oldKeyLength) {
            // Marca o registro atual como deletado
            indexFile.seek(position); // Volta para o início do registro
            indexFile.writeByte(DELETED_RECORD); // Marca o registro como deletado
    
            // Cria um novo registro no final do arquivo
            writeIndex(newKey, movieId);
        } else {
            indexFile.seek(position); // Volta para o início do registro atual
            indexFile.writeByte(VALID_RECORD); // Escreve a lápide como válida
            indexFile.writeInt(newRecordSize); // Atualiza o tamanho do registro
            indexFile.writeUTF(newKey); // Escreve a nova chave
            indexFile.writeInt(movieId); // Reescreve o id do filme
        }
    
        return true;
    }
    
    public boolean deleteIndex(String key) throws IOException {
        // Encontra a posição do registro a ser deletado
        long position = findIndexPositionByKey(key);
        
        if (position == -1) {
            return false; // Registro não encontrado
        }
    
        // Volta para o início do registro
        indexFile.seek(position);
            
        // Marca o registro como deletado
        indexFile.writeByte(DELETED_RECORD); // Marca o registro como deletado
    
        return true; // Deleção bem-sucedida
    }
    

    public long writeMultlistIndex(int movieId, long genreListHeadPosition, long movieIDIndexPosition, long genreIndexPosition, RandomAccessFile genreIndexFile) throws IOException {
        // Encontra o último bloco da lista encadeada para o gênero
        long lastPosition = findEndingMultlist(genreListHeadPosition);
    
        // Define a posição para o novo bloco no final do arquivo
        long position = indexFile.length();
    
        // Se a lista já possui elementos, atualiza o ponteiro do último bloco
        if (lastPosition != -1) {
            indexFile.seek(lastPosition + 13); // Pula a lápide (1 byte), o ID do filme (4 bytes) e o movieIndexPosition (8 bytes)
            indexFile.writeLong(position);     // Atualiza o ponteiro do último bloco para a nova posição
        } else {
            // Atualiza o ponteiro no índice de gêneros para o primeiro elemento da lista
            updateGenreListPointer(genreIndexFile, genreIndexPosition, position);
        }
    
        // Escreve o novo bloco na posição final do arquivo
        indexFile.seek(position);
        indexFile.writeByte(VALID_RECORD);    // Marca o registro como válido
        indexFile.writeInt(movieId);          // Escreve o ID do filme
        indexFile.writeLong(movieIDIndexPosition); // Escreve a posição do filme no arquivo índice de ID
        indexFile.writeLong(-1);              // Define o próximo bloco como -1 (final da lista)
    
        return position; // Retorna a posição onde o novo bloco foi escrito
    }


    public long writeMultlistIndex(int movieId, long genreListHeadPosition, long movieIDIndexPosition, long genreIndexPosition, RandomAccessFile genreIndexFile, RandomAccessFile genreIndexMultilistFile) throws IOException {
        // Encontra o último bloco da lista encadeada para o gênero
        long lastPosition = findEndingMultlist(genreListHeadPosition);
    
        // Define a posição para o novo bloco no final do arquivo
        long position = genreIndexMultilistFile.length();
    
        // Se a lista já possui elementos, atualiza o ponteiro do último bloco
        if (lastPosition != -1) {
            genreIndexMultilistFile.seek(lastPosition + 13); // Pula a lápide (1 byte), o ID do filme (4 bytes) e o movieIndexPosition (8 bytes)
            genreIndexMultilistFile.writeLong(position);     // Atualiza o ponteiro do último bloco para a nova posição
        } else {
            // Atualiza o ponteiro no índice de gêneros para o primeiro elemento da lista
            updateGenreListPointer(genreIndexFile, genreIndexPosition, position);
        }
    
        // Escreve o novo bloco na posição final do arquivo
        genreIndexMultilistFile.seek(position);
        genreIndexMultilistFile.writeByte(VALID_RECORD);    // Marca o registro como válido
        genreIndexMultilistFile.writeInt(movieId);          // Escreve o ID do filme
        genreIndexMultilistFile.writeLong(movieIDIndexPosition); // Escreve a posição do filme no arquivo índice de ID
        genreIndexMultilistFile.writeLong(-1);              // Define o próximo bloco como -1 (final da lista)
    
        return position; // Retorna a posição onde o novo bloco foi escrito
    }
    
    

    public long findEndingMultlist(long initialPosition) throws IOException{

        long position = initialPosition;
        long lastPosition = -1;
        // Enquanto posicao nao for -1
        while(position != -1){
            indexFile.seek(position);
            indexFile.readByte();
            indexFile.readInt();
            indexFile.readLong();
            long nextRecord = indexFile.readLong();
            lastPosition = position;
            position = nextRecord;
        }    

    
        return lastPosition;
    }


    public long writeGenreIndex(String genre) throws IOException{

        long existingPosition = findGenrePosition(genre);
        if (existingPosition != -1) {
            return existingPosition; // Se o gênero já existe, retorna sua posição
        }
    
        // Define a posição para o final do arquivo
        long position = indexFile.length();

        indexFile.seek(position);
        indexFile.writeByte(VALID_RECORD);    // Marca o registro como válido
        indexFile.writeUTF(genre);            // Escreve o Genero do Filme
        indexFile.writeLong(-1);              // Foi criado o genero que nao aponta para nenhuma posição (lista vazia)
        indexFile.writeInt(0);                // Define o próximo bloco como -1 (final da lista)                  
        

        return position;
    }
    

    public long findGenrePosition(String genre) throws IOException {
        indexFile.seek(0); // Inicia no início do arquivo
    
        // Percorre o arquivo até o final
        while (indexFile.getFilePointer() < indexFile.length()) {
            long position = indexFile.getFilePointer(); // Salva a posição atual
    
            byte isValid = indexFile.readByte(); // Lê a lápide
            String genreSearch = indexFile.readUTF(); // Lê o nome do gênero
            
            // Verifica se o registro é válido e se o gênero é o que estamos procurando
            if (isValid == VALID_RECORD && genreSearch.equals(genre)) {
                return position; // Retorna a posição do gênero encontrado
            }
            
            // Pula os campos restantes: Endereço da Lista (8 bytes) e Contador de Elementos (4 bytes)
            indexFile.skipBytes(8 + 4); // Pula 12 bytes
        }
    
        return -1; // Gênero não encontrado
    }

    public long getMultlistHead(long genrePosition) throws IOException{
        indexFile.seek(genrePosition);

        byte isValid = indexFile.readByte();    // Marca o registro como válido
        String genre = indexFile.readUTF();            // Escreve o Genero do Filme
        long multilistHead = indexFile.readLong();              // Foi criado o genero que nao aponta para nenhuma posição (lista vazia)
        

        return multilistHead;      
    } 

    private void updateGenreListPointer( RandomAccessFile genreIndexFile, long genrePosition, long position) throws IOException {
        
        genreIndexFile.seek(genrePosition);

        genreIndexFile.readByte();
        genreIndexFile.readUTF();
        
        // Atualiza posicao
        genreIndexFile.writeLong(position);
        genreIndexFile.writeInt(0);
    }
    
    

    
}
