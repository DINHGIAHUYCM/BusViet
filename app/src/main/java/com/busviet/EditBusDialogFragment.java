package com.busviet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.FirebaseDatabase;

public class EditBusDialogFragment extends DialogFragment {

    private EditText etStartPoint, etEndPoint;
    private Switch switchActive;
    private Bus currentBus;

    public static EditBusDialogFragment newInstance(Bus bus) {
        EditBusDialogFragment frag = new EditBusDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("bus", bus);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_bus, container, false);

        etStartPoint = view.findViewById(R.id.etStartPoint);
        etEndPoint = view.findViewById(R.id.etEndPoint);
        switchActive = view.findViewById(R.id.switchActive);
        Button btnSave = view.findViewById(R.id.btnSave);

        if (getArguments() != null) {
            currentBus = (Bus) getArguments().getSerializable("bus");
            if (currentBus != null) {
                etStartPoint.setText(currentBus.startPoint);
                etEndPoint.setText(currentBus.endPoint);
                switchActive.setChecked(currentBus.active);
            }
        }

        btnSave.setOnClickListener(v -> {
            currentBus.startPoint = etStartPoint.getText().toString().trim();
            currentBus.endPoint = etEndPoint.getText().toString().trim();
            currentBus.active = switchActive.isChecked();

            FirebaseDatabase.getInstance().getReference("bus")
                    .child(currentBus.id)
                    .setValue(currentBus)
                    .addOnSuccessListener(unused -> dismiss());
        });

        return view;
    }
}
