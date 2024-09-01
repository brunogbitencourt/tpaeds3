package com.tpaeds3.tpaeds3.model;

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


    
}
