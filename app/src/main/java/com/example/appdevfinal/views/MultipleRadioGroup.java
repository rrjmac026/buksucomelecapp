package com.example.appdevfinal.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.util.ArrayList;
import java.util.List;

public class MultipleRadioGroup extends RadioGroup {
    private List<RadioButton> checkedButtons;
    private int maxSelections = 12;

    public MultipleRadioGroup(Context context) {
        super(context);
        init();
    }

    public MultipleRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        checkedButtons = new ArrayList<>();
        setOnCheckedChangeListener(null); // Remove default single selection behavior
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        if (child instanceof RadioButton) {
            ((RadioButton) child).setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (checkedButtons.size() >= maxSelections) {
                        buttonView.setChecked(false);
                        return;
                    }
                    checkedButtons.add((RadioButton) buttonView);
                } else {
                    checkedButtons.remove(buttonView);
                }
            });
        }
    }

    public List<RadioButton> getCheckedButtons() {
        return checkedButtons;
    }

    public void clearCheck() {
        for (RadioButton rb : checkedButtons) {
            rb.setChecked(false);
        }
        checkedButtons.clear();
    }
}
