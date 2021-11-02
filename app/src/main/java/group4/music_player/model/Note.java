package group4.music_player.model;

public class Note {
    private String url;
    private String note;

    public Note(String url, String note) {
        this.url = url;
        this.note = note;
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

    @Override
    public String toString() {
        return "Note{" +
                "url='" + url + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
