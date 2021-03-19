package com.example.memo_fragmentversion;

public class MemoItem {
    int _id;
    String picture; // 사진의 경로
    String contents;
    String date; // 글 저장된 날짜

    public MemoItem(int _id, String picture, String contents, String date) {
        this._id = _id;
        this.picture = picture;
        this.contents = contents;
        this.date = date;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
