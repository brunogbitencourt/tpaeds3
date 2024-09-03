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

    public void writeMovie(Movie movie) throws IOException{
        try{            
            file.seek(0); // Set cursor to beginning
            int lastID = file.readInt();

            movie.setId(lastID + 1);

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


        }catch (Exception e) {
            e.printStackTrace();
        }
        
    }


    
}
