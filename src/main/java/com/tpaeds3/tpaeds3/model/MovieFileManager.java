package com.tpaeds3.tpaeds3.model;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class MovieFileManager {

    private RandomAccessFile file;
    private static final byte VALID_RECORD = 0x01;
    private static final byte DELETED_RECORD = 0x00;

    public MovieFileManager(RandomAccessFile file){
        this.file = file;
    }

    public long writeMovie(Movie movie) throws IOException {
        long position = 1;
        try {
            file.seek(0); // Set cursor to beginning
            int lastID = file.readInt();
            movie.setId(lastID + 1); // Define o novo ID automaticamente

            // Parse objet to byte array
            byte[] movieBytes = movie.toByteArray();

            // Set cursor to end of file
            file.seek(file.length());
            position =  file.length();

            // Write Valit Record
            file.writeByte(VALID_RECORD);

            // Write size of movie object
            file.writeInt(movieBytes.length);

            // Write Object
            file.write(movieBytes);

            // Update last ID with last id
            file.seek(0);
            file.writeInt(movie.getId());
            return position;

        } catch (Exception e) {
            return -1;
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

    public List<Movie> readMoviesByIds(List<Integer> ids) throws IOException {
        List<Movie> movies = new ArrayList<>();

        // Se a lista de IDs estiver vazia, retorne uma lista vazia
        if (ids == null || ids.isEmpty()) {
            return movies;
        }

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

                if (ids.contains(movie.getId())) {
                    movies.add(movie);
                    ids.remove(Integer.valueOf(movie.getId())); // Remover ID da lista após encontrar o filme
                }
            }

            file.seek(nextRecordPosition);
        }

        return movies;
    }

    public List<Movie> readAllMovies(int page, int size) throws IOException {
        List<Movie> movies = new ArrayList<>();
        file.seek(4); // Pular o int do último ID

        int start = page * size;
        int end = start + size;
        int count = 0;

        while (file.getFilePointer() < file.length() && count < end) {
            byte recordStatus = file.readByte();
            int recordSize = file.readInt();
            
            long nextRecordPosition = file.getFilePointer() + recordSize;
            
            if (recordStatus == VALID_RECORD) {
                if (count >= start && count < end) {
                    byte[] movieBytes = new byte[recordSize];
                    file.readFully(movieBytes);
        
                    Movie movie = new Movie();
                    movie.fromByteArray(movieBytes);
        
                    movies.add(movie);
                }
                count++;
            }
    
            file.seek(nextRecordPosition);
        }
    
        return movies;
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
 
    public boolean updateMovie(int movieId, Map<String, Object> updates) throws IOException {
        file.seek(4); // Pular o cabeçalho (último ID)

        while (file.getFilePointer() < file.length()) {
            long recordPosition = file.getFilePointer();
            byte recordStatus = file.readByte();
            int recordSize = file.readInt();

            if (recordStatus == VALID_RECORD) {
                byte[] movieBytes = new byte[recordSize];
                file.readFully(movieBytes);

                Movie movie = new Movie();
                movie.fromByteArray(movieBytes);

                if (movie.getId() == movieId) {
                    // Aplicar atualizações ao filme
                    applyUpdates(movie, updates);
                    
                    byte[] updatedMovieBytes = movie.toByteArray();

                    // Se o novo filme couber no espaço do registro original, apenas sobrescreva
                    if (updatedMovieBytes.length <= recordSize) {
                        // Posicionar o ponteiro para o início do registro
                        file.seek(recordPosition + 1 + 4); // Pular o byte de status e o int do tamanho

                        // Escrever o filme atualizado no mesmo espaço
                        file.write(updatedMovieBytes);
                    } else {
                        // Se o filme atualizado não couber, marcar como deletado e escrever um novo registro no final
                        file.seek(recordPosition);
                        file.writeByte(DELETED_RECORD); // Marca como deletado

                        // Escreve o novo registro no final do arquivo
                        movie.setId(movieId); // Mantém o ID original
                        return writeMovie(movie) == -1  ? false : true ; // Escreve o novo filme no final
                    }
                    return true;
                }
            } else {
                file.skipBytes(recordSize); // Pular os registros deletados
            }
        }
        return false; // Retorna false se o filme não for encontrado
    }


    public  Movie getMoveByPosition(long position) throws IOException{

        file.seek(position);

        byte recordStatus = file.readByte();
        int recordSize = file.readInt();

        if (recordStatus == VALID_RECORD) {
            byte[] movieBytes = new byte[recordSize];
            file.readFully(movieBytes);
            Movie movie = new Movie();
            movie.fromByteArray(movieBytes);
  
            return movie;
        }
    
        return null;
    }

    private void applyUpdates(Movie movie, Map<String, Object> updates) {
        if (updates.containsKey("name")) {
            movie.setName((String) updates.get("name"));
        }
        if (updates.containsKey("date")) {
        String dateString = (String) updates.get("date");
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Ajuste o formato conforme necessário
            Date date = dateFormat.parse(dateString);
            movie.setDate(date);
        } catch (ParseException e) {
            // Log e tratar erro de conversão de data
            e.printStackTrace();
        }
    }
        if (updates.containsKey("score")) {
            movie.setScore((Double) updates.get("score"));
        }
        if (updates.containsKey("genre")) {
            movie.setGenre((List<String>) updates.get("genre"));
        }
        if (updates.containsKey("overview")) {
            movie.setOverview((String) updates.get("overview"));
        }
        if (updates.containsKey("crew")) {
            movie.setCrew((List<String>) updates.get("crew"));
        }
        if (updates.containsKey("originTitle")) {
            movie.setOriginTitle((String) updates.get("originTitle"));
        }
        if (updates.containsKey("status")) {
            movie.setStatus((String) updates.get("status"));
        }
        if (updates.containsKey("originLang")) {
            movie.setOriginLang((String) updates.get("originLang"));
        }
        if (updates.containsKey("budget")) {
            movie.setBudget((Double) updates.get("budget"));
        }
        if (updates.containsKey("revenue")) {
            movie.setRevenue((Double) updates.get("revenue"));
        }
        if (updates.containsKey("country")) {
            movie.setCountry((String) updates.get("country"));
        }
    }
    
}
