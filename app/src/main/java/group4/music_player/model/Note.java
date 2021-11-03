package group4.music_player.model;

public class Note {
    private String url;
    private String note;
    private boolean  like;
    public Note(String url, String note,boolean like) {
        this.url = url;
        this.note = note;
        this.like = like;
    }

    public Note() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }

    @Override
    public String toString() {
        return "Note{" +
                "url='" + url + '\'' +
                ", note='" + note + '\'' +
                ", like=" + like +
                '}';
    }
}
