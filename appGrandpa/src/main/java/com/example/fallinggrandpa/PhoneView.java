package com.example.fallinggrandpa;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;


public class PhoneView extends View {
    public PhoneView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public static void add(final Activity activity, Button buttonAdd)
    {
        buttonAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PhoneView.create(activity);
            }
        });
    }

    public static void create(Activity activity)
    {
        create(activity, "");
    }

    public static void create(Activity activity, String s)
    {
        final LinearLayout scrollViewLayout = activity.findViewById(R.id.LayoutNumbers);
        final ConstraintLayout newView = (ConstraintLayout) activity.getLayoutInflater().inflate(R.layout.phone_layout, scrollViewLayout,false);
        newView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        EditText editText = newView.findViewById(R.id.editTextPhone);
        editText.setText(s);
        Button btnRemove = newView.findViewById(R.id.buttonRemove);
        btnRemove.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                scrollViewLayout.removeView(newView);
            }
        });
        scrollViewLayout.addView(newView);
    }
}
