package jack.village;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.TextView;

/**
 * Created by jack on 01/02/2018.
 */

public class NoteViewHolder extends ViewHolder {

    //View Holder for the notes
    View view;

    TextView textTitle;
    TextView textTime;
    TextView textContent;
    CardView noteCard;

    public NoteViewHolder(View itemView) {
        super(itemView);

        view = itemView;

        //Text views are set to the views on the single_note_layout.xml
        textTime = view .findViewById(R.id.noteCardTime);
        textTitle = view.findViewById(R.id.noteCardTitle);
        textContent = view.findViewById(R.id.noteCardContent);
        noteCard = view.findViewById(R.id.noteCard);

    }

    //Sets the title, content and time for each single note
    public void setNoteTitle(String title){
        textTitle.setText(title);
    }

    public void setNoteContent(String content) {
        textContent.setText(content);
    }

    public void setNoteTime(String time){
        textTime.setText(time);
    }
}
