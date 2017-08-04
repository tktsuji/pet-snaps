package blackbox.petsnaps;

import android.app.Activity;
import android.app.Dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CreateCommentFragment extends DialogFragment {
    Button submitBttn;
    EditText commentET;
    TextView commentCharCountTV;
    ProgressDialog progressDialog;
    private SubmitButtonClick sbcInterface;
    public interface SubmitButtonClick {
        void submitButtonClick(String message);
    }

    @Override
    public void onAttach(Context context) {
        sbcInterface = (SubmitButtonClick) context;
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
        View view = inflater.inflate(R.layout.fragment_create_comment, container);

        progressDialog = new ProgressDialog(getActivity());
        commentET = (EditText) view.findViewById(R.id.comment_et);
        commentCharCountTV = (TextView) view.findViewById(R.id.comment_char_count_tv);
        submitBttn = (Button) view.findViewById(R.id.post_comment_bttn);

        setUpEditTexts();

        submitBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = commentET.getText().toString().trim();
                sbcInterface.submitButtonClick(message);
                dismiss();
            }
        });
        return view;
    }

    private void setUpEditTexts() {
        commentET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(150)});
        commentET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CharSequence charSeq = Integer.toString(s.length()) + "/150";
                commentCharCountTV.setText(charSeq);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // do nothing
            }
        });
    }


}
