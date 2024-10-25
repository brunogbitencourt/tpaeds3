package com.tpaeds3.tpaeds3.model;

import java.io.IOException;
import java.io.RandomAccessFile;

public class IndexIdFileManager {

    private RandomAccessFile indexFile;
    private static final byte VALID_RECORD = 0x01;
    private static final byte DELETED_RECORD = 0x00;
    private static final int RECORD_SIZE = Integer.BYTES + Long.BYTES + 1; // Tamanho fixo do registro: 4 + 8 + 1 = 13 bytes


    public IndexIdFileManager(RandomAccessFile indexFile){
        this.indexFile = indexFile;
    }


    public boolean writeIndex(int key, long position) throws IOException {
        try {
            indexFile.seek(indexFile.length());

            indexFile.writeByte(VALID_RECORD);       // Escreve o status do registro (1 byte)
            indexFile.writeInt(key);                 // Escreve a chave (int) (4 bytes)
            indexFile.writeLong(position);           // Escreve a posição (long) (8 bytes)
    
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    

    public long findPositionById(int key) throws IOException {
        indexFile.seek(0); // Reinicia o ponteiro no início do arquivo

        while (indexFile.getFilePointer() < indexFile.length()) {
            byte recordStatus = indexFile.readByte(); // Lê a lápide
            int storedKey = indexFile.readInt();       // Lê a chave
            long position = indexFile.readLong();      // Lê a posição

            if (recordStatus == VALID_RECORD && storedKey == key) {
                return position;
            }

            indexFile.seek(indexFile.getFilePointer() + (RECORD_SIZE - (1 + Integer.BYTES + Long.BYTES)));
        }

        return -1;
    }

    public long findIndexPositionByKey(int key) throws IOException {
        indexFile.seek(0);

        while (indexFile.getFilePointer() < indexFile.length()) {
            byte recordStatus = indexFile.readByte();
            int storedKey = indexFile.readInt();
            long position = indexFile.readLong();

            if (recordStatus == VALID_RECORD && storedKey == key) {
                // Retorna a posição do início do registro
                return indexFile.getFilePointer() - RECORD_SIZE;
            }
        }

        return -1;
    }
    
    
    public boolean updateIndex(int movieId, long newPosition) throws IOException {
        long position = findIndexPositionByKey(movieId);

        if (position == -1) {
            return false;
        }

        indexFile.seek(position);

        indexFile.writeByte(VALID_RECORD);// Escreve o status do registro (1 byte)
        indexFile.writeInt(movieId);// Escreve a chave (int) (4 bytes)
        indexFile.writeLong(newPosition);// Escreve a posição (long) (8 bytes)

        return true;
    }
    
    public boolean deleteIndex(int key) throws IOException {
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
