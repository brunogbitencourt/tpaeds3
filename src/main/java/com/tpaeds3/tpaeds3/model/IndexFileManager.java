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


    public boolean writeIndex(String key, long position) throws IOException {
        try {
            int keyLength = key.getBytes(StandardCharsets.UTF_8).length;
            
            // Soma dois bytes para o tamanho da string
            // Padrão da biblioteca de read write do arquivo
            int recordSize = keyLength + Long.BYTES + 2; 
    
            indexFile.seek(indexFile.length()); 
    
            indexFile.writeByte(VALID_RECORD);
    
            indexFile.writeInt(recordSize);
    
            indexFile.writeUTF(key);
    
            indexFile.writeLong(position);
    
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    

    public long findPositionById(String key) throws IOException {
        while (indexFile.getFilePointer() < indexFile.length()) {
            
            byte recordStatus = indexFile.readByte();  // Lê a lápide
            int recordSize = indexFile.readInt();      // Lê o tamanho do registro    
            
            long currentPointer = indexFile.getFilePointer();

            // Lê a chave (nome do filme)
            String movieName = indexFile.readUTF();
    
            // Lê a posição (endereço)
            long position = indexFile.readLong();
    
            // Posição do próximo registro:
            long nextRecordPosition = currentPointer + recordSize;
    
            // Se o registro for válido e a chave for igual, retorne a posição
            if (recordStatus == VALID_RECORD && movieName.equals(key)) {
                return position;
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
    
            // Lê a posição (endereço)
            @SuppressWarnings("unused")
            long position = indexFile.readLong();
    
            // Se o registro for válido e a chave for igual, retorne a posição
            if (recordStatus == VALID_RECORD && movieName.equals(key)) {
                // Retorna a posição do início do registro
                return indexFile.getFilePointer() - (Long.BYTES + movieName.getBytes(StandardCharsets.UTF_8).length + 2 + 4 + 1);
            }
    
            // Move o ponteiro para o próximo registro
            indexFile.skipBytes(recordSize - (Long.BYTES + movieName.getBytes(StandardCharsets.UTF_8).length + 2)); 
        }
    
        return -1;  // Retorna -1 se a chave não for encontrada
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
        
        // Lê o tamanho do registro atual
        int currentRecordSize = indexFile.readInt();
        
        // Lê a chave antiga para comparação
        String oldKeyValue = indexFile.readUTF(); // Supondo que a chave é uma String
    
        int newKeyLength = newKey.getBytes(StandardCharsets.UTF_8).length;
        int newRecordSize = newKeyLength + Long.BYTES + 2; // Calcula o novo tamanho do registro (nova chave e posição)
    
        // Verifica se a nova chave é maior que a antiga
        if (newKeyLength > oldKeyValue.getBytes(StandardCharsets.UTF_8).length) {
            // Marca o registro atual como deletado
            indexFile.seek(position); // Volta para o início do registro
            indexFile.writeByte(DELETED_RECORD); // Marca o registro como deletado
    
            // Cria um novo registro no final do arquivo
            writeIndex(newKey, newPosition);
        } else {
            // Se a nova chave couber no espaço atual, atualiza diretamente
            if (newRecordSize <= currentRecordSize) {
                indexFile.seek(indexFile.getFilePointer() - currentRecordSize); // Volta para o início do registro atual
                indexFile.writeByte(VALID_RECORD); // Escreve a lápide como válida
                indexFile.writeInt(newRecordSize); // Atualiza o tamanho do registro
                indexFile.writeUTF(newKey); // Escreve a nova chave
                indexFile.writeLong(newPosition); // Escreve a nova posição
            } else {
                // Se a nova chave for maior e não couber, marca como deletado e escreve um novo
                indexFile.seek(position); // Volta para o início do registro
                indexFile.writeByte(DELETED_RECORD); // Marca o registro como deletado
                writeIndex(newKey, newPosition); // Cria um novo registro com a nova chave
            }
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
    
    

    
}
