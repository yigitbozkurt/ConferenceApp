package com.yigit.conferenceapp.data.model.seminar;

public class SeminarModel {
    //Firebaseden gelen semineri tutmak için bu modeli kullanıyoruz.
    private String id;
    private String title;
    private String description;
    private String date;
    private String location;
    private String thumbnail;
    private String video;
    private String speaker_name;
    private String speaker_photo;

    public SeminarModel() {
    }

    public SeminarModel(String id, String title, String description, String date, String location, String thumbnail, String video, String speaker_name, String speaker_photo) {
        this.id=id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.thumbnail = thumbnail;
        this.video = video;
        this.speaker_name = speaker_name;
        this.speaker_photo = speaker_photo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getSpeaker_name() {
        return speaker_name;
    }

    public void setSpeaker_name(String speaker_name) {
        this.speaker_name = speaker_name;
    }

    public String getSpeaker_photo() {
        return speaker_photo;
    }

    public void setSpeaker_photo(String speaker_photo) {
        this.speaker_photo = speaker_photo;
    }
}
