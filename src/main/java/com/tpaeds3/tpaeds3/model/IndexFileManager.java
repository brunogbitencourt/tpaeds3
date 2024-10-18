package com.tpaeds3.tpaeds3.model;

import java.io.IOException;
import java.io.RandomAccessFile;

public class IndexFileManager {

    private RandomAccessFile indexFile;
    private static final byte VALID_RECORD = 0x01;
    private static final byte DELETED_RECORD = 0x00;


    public IndexFileManager(RandomAccessFile indexFile){
        this.indexFile = indexFile;
    }


    public boolean writeIndex(String key, long position) throws IOException{
        try {
            // Posiciona o ponteiro para o fim do arquivo
            indexFile.seek(indexFile.length());
            indexFile.writeByte(VALID_RECORD); // Lápide
            indexFile.writeUTF(key); // Chave
            indexFile.writeLong(position); // Endereço 
            return true;       
        }
        catch (Exception e){
            return false;
        }

    }


    
}
