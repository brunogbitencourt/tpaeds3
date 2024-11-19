package com.tpaeds3.tpaeds3.model;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class MovieDBFileManager {

    private static final String FILE_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/movies.db";
    private static final String INDEX1_PATH = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/index01.db";
    private static final String INDEX_BY_ID = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexById.db";
    private static final String INDEX_BY_GENRE = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexByGenre.db";
    private static final String INDEX_BY_GENRE_MULTLIST = "./src/main/java/com/tpaeds3/tpaeds3/files_out/index/indexByGenreMultlist.db";


    private RandomAccessFile file;
    private static final int HEADER_RECORD = 0xABCDEF12;    
    private static final byte VALID_RECORD = 0x01;
    private static final byte DELETED_RECORD = 0x00;
    
    public MovieDBFileManager(RandomAccessFile file){
        this.file = file;
    }

    
    public long writeMovie(Movie movie) throws IOException {
        return writeMovie(movie, null); // Chama a versão com ID passando null
    }

    public long writeMovie(Movie movie, Integer id) throws IOException {
        long position = 1;
        try {
            file.seek(0); // Set cursor to beginning
            int lastID = file.readInt();

            // Se o ID for nulo, define um novo ID automaticamente
            if (id == null) {
                movie.setId(lastID + 1); // Define o novo ID automaticamente
            } else {
                movie.setId(id); // Usa o ID fornecido
            }

            
            // Parse objet to byte array
            byte[] movieBytes = movie.toByteArray();

            // Set cursor to end of file
            file.seek(file.length());
            position =  file.length();

            file.writeInt(HEADER_RECORD);
            // Write Valit Record
            file.writeByte(VALID_RECORD);

            // Write size of movie object
            file.writeInt(movieBytes.length);

            // Write Object
            file.write(movieBytes);

            // Update last ID with last id
            file.seek(0);
            if (id == null) {
                file.writeInt(movie.getId());
            }
            return position;

        } catch (Exception e) {
            return -1;
        }
    }    

    public Movie readMovie(int movieId) throws IOException {
        file.seek(4); // Pular o int do último ID

        while (file.getFilePointer() < file.length()) {
            int headerRecord = file.readInt();
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
            int headerRecord = file.readInt();
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
            int headerRecord = file.readInt();
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

    public String deleteMovie(int id) throws IOException {
        file.seek(4); // Pula o cabeçalho (último ID)

        while (file.getFilePointer() < file.length()) {
            long recordPosition = file.getFilePointer();

            int headerRecord = file.readInt();
            byte recordStatus = file.readByte();
            int recordSize = file.readInt(); // Lê o tamanho do objeto Movie

            if (recordStatus == VALID_RECORD) {
                long currentPosition = file.getFilePointer();
                byte[] movieBytes = new byte[recordSize];
                file.readFully(movieBytes);

                Movie movie = new Movie();
                movie.fromByteArray(movieBytes);

                if (movie.getId() == id) {
                    String movieName = movie.getName(); // Captura o nome do filme antes de deletar
                    file.seek(recordPosition);
                    file.writeByte(DELETED_RECORD); // Marca o registro como deletado
                    return movieName;
                }

                file.seek(currentPosition + recordSize); // Avança para o próximo registro
            } else {
                file.skipBytes(recordSize); // Pula o registro deletado
            }
        }

        return null; // Retorna null se o filme não for encontrado
    }
 
    public Index updateMovie(int movieId, Map<String, Object> updates) throws IOException {
        file.seek(4); // Pular o cabeçalho (último ID)
        Index index = null;
    
        while (file.getFilePointer() < file.length()) {
            int headerRecord = file.readInt();
            long recordPosition = file.getFilePointer();
            byte recordStatus = file.readByte();
            int recordSize = file.readInt();
    
            if (recordStatus == VALID_RECORD) {
                byte[] movieBytes = new byte[recordSize];
                file.readFully(movieBytes);
    
                Movie movie = new Movie();
                movie.fromByteArray(movieBytes);
    
                if (movie.getId() == movieId) {
                    // Aplicar atualizações ao filme e capturar lastName e newName
                    index = applyUpdates(movie, updates);
                    
                    byte[] updatedMovieBytes = movie.toByteArray();
                    if (updatedMovieBytes.length <= recordSize) {
                        // Posicionar o ponteiro para o início do registro
                        file.seek(recordPosition + 1 + 4); // Pular o byte de status e o int do tamanho
                        file.write(updatedMovieBytes);
                        index.setNewPosition(recordPosition);
                    } else {
                        // Marcar o registro original como deletado
                        file.seek(recordPosition);
                        file.writeByte(DELETED_RECORD); // Tenta marcar como deletado
    
                        // Escreve o novo registro no final do arquivo
                        long newPosition = writeMovie(movie, movie.getId()); // Supondo que writeMovie retorna a posição final
                        index.setNewPosition(newPosition);
                    }
    
                    return index;
                }
            } else {
                // Pular o registro deletado
                file.skipBytes(recordSize);
            }
        }
        return index; // Retorna null se o filme não for encontrado
    }
    

    public  Movie getMovieByPosition(long position) throws IOException{

        file.seek(position);
        int headerRecord = file.readInt();
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

    public List<Movie> readMoviesByGenre(String genre, IndexByNameFileManager genreIndexFile,IndexByNameFileManager multListGenreFile, IndexByIdFileManager idIndexFile) throws IOException {
        List<Movie> movies = new ArrayList<>();
        
        RandomAccessFile binaryDataFile = new RandomAccessFile(FILE_PATH, "rw");
        RandomAccessFile binaryIndexFile = new RandomAccessFile(INDEX1_PATH, "rw");
        RandomAccessFile binaryIndexByIdFile = new RandomAccessFile(INDEX_BY_ID, "rw");
        RandomAccessFile binaryIndexByGenreFile = new RandomAccessFile(INDEX_BY_GENRE, "rw");
        RandomAccessFile binaryIndexByGenreMultlistFile = new RandomAccessFile(INDEX_BY_GENRE_MULTLIST, "rw");
            
        MovieDBFileManager movieFileManager = new MovieDBFileManager(binaryDataFile);
        IndexByNameFileManager indexFileManager = new IndexByNameFileManager(binaryIndexFile);
        IndexByIdFileManager indexByIdFileManager = new IndexByIdFileManager(binaryIndexByIdFile);
        IndexByNameFileManager indexByGenreFileManager = new IndexByNameFileManager(binaryIndexByGenreFile);
        IndexByNameFileManager indexByGenreMultlistFile = new IndexByNameFileManager(binaryIndexByGenreMultlistFile);

        long genrePosition = indexByGenreFileManager.findGenrePosition(genre);
        long nextRecordPosition = indexByGenreFileManager.getMultlistHead(genrePosition);

        while(nextRecordPosition != -1){
            binaryIndexByGenreMultlistFile.seek(nextRecordPosition); 
            byte isValid = binaryIndexByGenreMultlistFile.readByte();
            int  moveID  = binaryIndexByGenreMultlistFile.readInt();
            long example = binaryIndexByGenreMultlistFile.readLong();
            nextRecordPosition = binaryIndexByGenreMultlistFile.readLong();
            //int  count = binaryIndexByGenreMultlistFile.readInt();

            if(isValid == VALID_RECORD){                    
                    long indexPosition = indexByIdFileManager.findIndexPositionByKey(moveID);
                    long dbPosition = indexByIdFileManager.findDBPositionByIndexPosition(indexPosition + 13);
                    file.seek(dbPosition);
                    
                    int headerRecord = file.readInt();
                    byte recordStatus = file.readByte();
                    int recordSize = file.readInt();

                    if (recordStatus == VALID_RECORD) {
                        byte[] movieBytes = new byte[recordSize];
                        file.readFully(movieBytes);
                        Movie movie = new Movie();
                        movie.fromByteArray(movieBytes);
                        movies.add(movie);
                    }
            }         
        }
        
        return movies;
    }


    private Index applyUpdates(Movie movie, Map<String, Object> updates) {
        Index index = new Index();
        index.setNewPosition((long) -1);

        if (updates.containsKey("name")) {
            index.setLastName(movie.getName());
            String newName = (String) updates.get("name");
            index.setNewName(newName);
            movie.setName(newName);
        } else {
            String name = movie.getName();
            index.setLastName(name);
            index.setNewName(name);
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

        // Retorna o Map com lastName e newName (se a chave "name" foi atualizada)
        return index;
    }

    
}
