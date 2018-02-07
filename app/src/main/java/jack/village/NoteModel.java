package jack.village;

/**
 * Created by jack on 01/02/2018.
 */

public class NoteModel {

    //Set up a model for the notes

    public String noteTitle;
    public String noteContent;
    public String noteTime;

    public NoteModel(){

    }

    public NoteModel(String noteTitle, String noteContent, String noteTime) {
        this.noteTitle = noteTitle;
        this.noteContent = noteContent;
        this.noteTime = noteTime;
    }

    //Public get and set methods
    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent
            (String noteContent) {
        this.noteContent = noteContent;
    }

    public String getNoteTime() {
        return noteTime;
    }

    public void setNoteTime(String noteTime) {
        this.noteTime = noteTime;
    }
}
