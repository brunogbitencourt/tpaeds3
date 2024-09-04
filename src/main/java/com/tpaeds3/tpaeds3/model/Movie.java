package com.tpaeds3.tpaeds3.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Movie {

    private int id;
    private String name;
    private Date date;    
    private double score;
    private List<String> genre;
    private String overview;
    private List<String> crew;
    private String originTitle;
    private String status;
    private String originLang;
    private double budget;
    private double revenue;
    private String country;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public double getScore() {
        return score;
    }
    public void setScore(double score) {
        this.score = score;
    }
    public List<String> getGenre() {
        return genre;
    }
    public void setGenre(List<String> genre) {
        this.genre = genre;
    }
    public String getOverview() {
        return overview;
    }
    public void setOverview(String overview) {
        this.overview = overview;
    }
    public List<String> getCrew() {
        return crew;
    }
    public void setCrew(List<String> crew) {
        this.crew = crew;
    }
    public String getOriginTitle() {
        return originTitle;
    }
    public void setOriginTitle(String originTitle) {
        this.originTitle = originTitle;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getOriginLang() {
        return originLang;
    }
    public void setOriginLang(String originLang) {
        this.originLang = originLang;
    }
    public double getBudget() {
        return budget;
    }
    public void setBudget(double budget) {
        this.budget = budget;
    }
    public double getRevenue() {
        return revenue;
    }
    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }  


    public byte[] toByteArray() throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id);
        dos.writeUTF(this.name);

        // Convert data to string (yyyy-MM-dd easier to sort)
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        dos.writeUTF(df.format(this.date));       

        dos.writeDouble(this.score);

        // Write count element of genre list
        // Write each element of the list        
        dos.writeInt(this.genre.size());
        for(String s : this.genre){
            dos.writeUTF(s);
        }

        dos.writeUTF(this.overview);
        
        // Write count element of crew list
        // Write each element of the list
        dos.writeInt(this.crew.size());
        for(String s : this.crew){
            dos.writeUTF(s);
        }

        dos.writeUTF(this.originTitle);
        dos.writeUTF(this.status);
        dos.writeUTF(this.originLang);
        dos.writeDouble(this.budget);
        dos.writeDouble(this.revenue);
        dos.writeUTF(this.country);     

        return baos.toByteArray();
    }

    public void fromByteArray(byte[] b) throws IOException{
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);

        this.id = dis.readInt();
        this.name = dis.readUTF();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            this.date = df.parse(dis.readUTF());
        } catch (ParseException e) {
            System.err.println("Error: Parse Date");
        }

        this.score = dis.readDouble();

        // Initialize lists before using them
        this.genre = new ArrayList<>();
        int genSize = dis.readInt();
        for(int i=0; i<genSize; i++){
            this.genre.add(dis.readUTF());
        }

        this.overview = dis.readUTF();

        // Initialize lists before using them
        this.crew = new ArrayList<>();
        int crewSize = dis.readInt();
        for(int i=0; i<crewSize; i++){
            this.crew.add(dis.readUTF());
        }

        this.originTitle = dis.readUTF();
        this.status = dis.readUTF();
        this.originLang = dis.readUTF();
        this.budget = dis.readDouble();
        this.revenue = dis.readDouble();
        this.country = dis.readUTF();
    }

    
}
