package com.tpaeds3.tpaeds3.model;

import java.io.IOException;
import java.io.RandomAccessFile;

public class MovieFileManager {

    private RandomAccessFile file;
    private static final byte VALID_RECORD = 0x01;
    private static final byte DELETED_RECORD = 0x00;

    public MovieFileManager(RandomAccessFile file){
        this.file = file;
    }

    public boolean writeMovie(Movie movie) throws IOException {
        try {
            file.seek(0); // Set cursor to beginning
            int lastID = file.readInt();
            movie.setId(lastID + 1); // Define o novo ID automaticamente

            // Parse objet to byte array
            byte[] movieBytes = movie.toByteArray();

            // Set cursor to end of file
            file.seek(file.length());

            // Write Valit Record
            file.writeByte(VALID_RECORD);

            // Write size of movie object
            file.writeInt(movieBytes.length);

            // Write Object
            file.write(movieBytes);

            // Update last ID with last id
            file.seek(0);
            file.writeInt(movie.getId());
            return true;

        } catch (Exception e) {
            return false;
        }
    }    

    public Movie readMovie(int movieId) throws IOException {
        file.seek(4); // Pular o int do último ID

        while (file.getFilePointer() < file.length()) {
            byte recordStatus = file.readByte();
            int recordSize = file.readInt();

            long nextRecordPosition = file.getFilePointer() + recordSize;
    
            if (recordStatus == VALID_RECORD) {
                byte[] movieBytes = new byte[recordSize];
                file.readFully(movieBytes);
    
                Movie movie = new Movie();
                movie.fromByteArray(movieBytes);
    
                if (movie.getId() == movieId) {
                    return movie;
                }
            }
    
            file.seek(nextRecordPosition);
        }
    
        return null;
    }

    public boolean deleteMovie(int id) throws IOException {
        file.seek(4); // Pula o cabeçalho (último ID)

        while (file.getFilePointer() < file.length()) {
            long recordPosition = file.getFilePointer();
            byte recordStatus = file.readByte();

            int recordSize = file.readInt(); // Lê o tamanho do objeto Movie

            if (recordStatus == VALID_RECORD) {
                long currentPosition = file.getFilePointer();
                byte[] movieBytes = new byte[recordSize];
                file.readFully(movieBytes);

                Movie movie = new Movie();
                movie.fromByteArray(movieBytes);

                if (movie.getId() == id) {
                    file.seek(recordPosition);
                    file.writeByte(DELETED_RECORD); // Marca o registro como deletado
                    return true;
                }

                file.seek(currentPosition + recordSize); // Avança para o próximo registro
            } else {
                file.skipBytes(recordSize); // Pula o registro deletado
            }
        }

        return false; // Retorna false se o filme não for encontrado
    }
    
}
