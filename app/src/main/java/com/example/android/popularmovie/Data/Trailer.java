package com.example.android.popularmovie.Data;

/**
 * Created by susanoo on 19/04/16.
 */
public class Trailer {
    private String id;
    private String key;
    private String name;
    private String site;
    private String type;

    public Trailer(String id, String key, String name, String site, String type){
        setId(id);
        setKey(key);
        setName(name);
        setSite(site);
        setType(type);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

