package blackbox.petsnaps;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import java.util.ArrayList;

public class AddTagsFragment extends DialogFragment {
    Button submitBttn;
    private static final int numTags = 6;
    ArrayList<Boolean> tags;

    private TagsListener tagsListener;

    public interface TagsListener {
        void addTags(ArrayList<Boolean> tags);
    }

    @Override
    public void onAttach(Context context) {
        tagsListener = (TagsListener) context;
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_tags, container);

        final AppCompatCheckBox dogCheckBox = (AppCompatCheckBox) view.findViewById(R.id.checkBoxDog);
        final AppCompatCheckBox catCheckBox = (AppCompatCheckBox) view.findViewById(R.id.checkBoxCat);
        final AppCompatCheckBox birdCheckBox = (AppCompatCheckBox) view.findViewById(R.id.checkBoxBird);
        final AppCompatCheckBox rabbitCheckBox = (AppCompatCheckBox) view.findViewById(R.id.checkBoxRabbit);
        final AppCompatCheckBox reptileCheckBox = (AppCompatCheckBox) view.findViewById(R.id.checkBoxReptile);
        final AppCompatCheckBox rodentCheckBox = (AppCompatCheckBox) view.findViewById(R.id.checkBoxRodent);
        tags = new ArrayList<Boolean>(numTags);

        submitBttn = (Button) view.findViewById(R.id.add_tags_bttn);
        submitBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tags.add(0, dogCheckBox.isChecked());
                tags.add(1, catCheckBox.isChecked());
                tags.add(2, birdCheckBox.isChecked());
                tags.add(3, rabbitCheckBox.isChecked());
                tags.add(4, reptileCheckBox.isChecked());
                tags.add(5, rodentCheckBox.isChecked());
                tagsListener.addTags(tags);
            }
        });

        return view;
    }
}