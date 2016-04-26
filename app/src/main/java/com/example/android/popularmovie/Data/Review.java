package com.example.android.popularmovie.Data;

/**
 * Created by susanoo on 19/04/16.
 */
public class Review {

    private String id;
    private String author;
    private String content;

    public Review(String id, String author, String content){
        setId(id);
        setAuthor(author);
        setContent(content);
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
