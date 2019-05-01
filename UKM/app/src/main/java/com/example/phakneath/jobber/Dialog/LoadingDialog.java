package com.example.phakneath.jobber.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.example.phakneath.jobber.R;

public class LoadingDialog extends DialogFragment {

    private ProgressBar progressBar;
    private View view;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        view = LayoutInflater.from(getActivity()).inflate(R.layout.loading_dialog, null);
        builder.setCancelable(false);
        builder.setView(view);
        initView();
        return builder.create();
    }

    private void initView() {
        progressBar = view.findViewById(R.id.progress);
    }

}
