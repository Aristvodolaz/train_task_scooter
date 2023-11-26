package com.example.train_task_scooter.dialog;

import android.os.Bundle;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ScooterInfoBottomDialog extends BottomSheetDialogFragment {
    private int id;

    public ScooterInfoBottomDialog(int id) {
        this.id = id;
    }

    public static ScooterInfoBottomDialog newInstance(int id) {
        return new ScooterInfoBottomDialog(id);
    }
}
