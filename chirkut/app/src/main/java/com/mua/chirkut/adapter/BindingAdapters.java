package com.mua.chirkut.adapter;

import android.view.View;

import androidx.databinding.BindingAdapter;

public class BindingAdapters {


    @BindingAdapter("visibility")
    public static void setVisibility(View view, Boolean visibile) {
        if (visibile)
            view.setVisibility(View.VISIBLE);
        else
            view.setVisibility(View.GONE);
    }

}
