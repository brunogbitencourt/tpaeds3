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
    
    

    
}
