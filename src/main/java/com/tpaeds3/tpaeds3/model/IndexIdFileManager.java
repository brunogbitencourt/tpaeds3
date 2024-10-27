package com.tpaeds3.tpaeds3.model;

import java.io.IOException;
import java.io.RandomAccessFile;

public class IndexIdFileManager {

    private RandomAccessFile indexFile;
    private static final byte VALID_RECORD = 0x01;
    private static final byte DELETED_RECORD = 0x00;
    private static final int RECORD_SIZE = Integer.BYTES + Long.BYTES + 1; // Tamanho fixo do registro: 4 + 8 + 1 = 13
                                                                           // bytes

    public IndexIdFileManager(RandomAccessFile indexFile) {
        this.indexFile = indexFile;
    }

    public long writeIndex(int key, long position) throws IOException {
        try {
            long positionInserted = indexFile.length();
            indexFile.seek(positionInserted);

            indexFile.writeByte(VALID_RECORD); // Escreve o status do registro (1 byte)
            indexFile.writeInt(key); // Escreve a chave (int) (4 bytes)
            indexFile.writeLong(position); // Escreve a posição (long) (8 bytes)

            return positionInserted;
        } catch (Exception e) {
            return -1;
        }
    }

    public long writeMultlistIndex(int key, long initialPosition, long indexIdPosition) throws IOException {
        long lastPosition = findEndingMultlist(initialPosition);

        // Define a posição para o final do arquivo
        long position = indexFile.length();

        // Se a lista tiver elementos, atualiza o nextRecord do último registro
        if (lastPosition != -1) {
            indexFile.seek(lastPosition + 13); // Pula a lápide (1 byte), o ID do filme (4 bytes) e o indexIdPosition (8
                                               // bytes)
            indexFile.writeLong(position); // Atualiza o ponteiro do último bloco para a nova posição
        }

        // Escreve o novo bloco na posição final do arquivo
        indexFile.seek(position);
        indexFile.writeByte(VALID_RECORD); // Marca o registro como válido
        indexFile.writeInt(key); // Escreve o ID do filme
        indexFile.writeLong(indexIdPosition); // Escreve a posição do filme no arquivo indice
        indexFile.writeLong(-1); // Define o próximo bloco como -1 (final da lista)

        return position; // Retorna a posição onde o novo bloco foi escrito
    }

    public long findEndingMultlist(long initialPosition) throws IOException {

        long position = initialPosition;
        long lastPosition = -1;
        // Enquanto posicao nao for -1
        while (position != -1) {
            indexFile.seek(position);
            indexFile.readByte();
            indexFile.readInt();
            indexFile.readLong();
            long nextRecord = indexFile.readLong();
            lastPosition = position;
            position = position + nextRecord;
        }

        return lastPosition;
    }

    public long findPositionById(int key) throws IOException {
        indexFile.seek(0); // Reinicia o ponteiro no início do arquivo

        while (indexFile.getFilePointer() < indexFile.length()) {
            long currentPosition = indexFile.getFilePointer();
            byte recordStatus = indexFile.readByte(); // Lê a lápide
            int storedKey = indexFile.readInt(); // Lê a chave
            long position = indexFile.readLong(); // Lê a posição

            if (recordStatus == VALID_RECORD && storedKey == key) {
                return position;
            }

            indexFile.seek(currentPosition + 13);
        }

        return -1;
    }

    public long findIndexPositionByKey(int key) throws IOException {
        indexFile.seek(0); // Começa do início do arquivo

        while (indexFile.getFilePointer() < indexFile.length()) {
            long currentPosition = indexFile.getFilePointer();
            byte recordStatus = indexFile.readByte();
            int storedKey = indexFile.readInt();
            long position = indexFile.readLong();

            if (recordStatus == VALID_RECORD && storedKey == key) {
                // Retorna a posição de armazenamento da chave
                return currentPosition - 13;
            }

            // Avança o ponteiro para o próximo registro (1 byte para o status + 4 bytes
            // para a chave + 8 bytes para a posição)
            indexFile.seek(currentPosition + 1 + 4 + 8);
        }

        return -1; // Retorna -1 se a chave não for encontrada
    }

    public long findDBPositionByIndexPosition(long position) throws IOException {
        indexFile.seek(position); // Começa do início do arquivo

        byte recordStatus = indexFile.readByte();
        int storedKey = indexFile.readInt();
        long positionDB = indexFile.readLong();

        if (recordStatus == VALID_RECORD && storedKey == storedKey) {
                // Retorna a posição de armazenamento da chave
                return positionDB;
        }


        return -1; // Retorna -1 se a chave não for encontrada
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
