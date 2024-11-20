package com.example.nearbuy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ViewTypeBottomSheetFragment extends BottomSheetDialogFragment {
    private OnViewTypeSelectedListener mListener;

    public interface OnViewTypeSelectedListener {
        void onViewTypeSelected(String viewType);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.dialog_view_type, container, false);

        String[] viewTypes = {"Horizontal Product View", "Grid Product View"};
        int[] images = {R.drawable.horizontal_view_image, R.drawable.grid_view_image};

        LinearLayout containerLayout = rootView.findViewById(R.id.container_layout);

        // Add the items dynamically
        for (int i = 0; i < viewTypes.length; i++) {
            // Make the index i effectively final by copying it into a final variable
            final int position = i;

            View itemView = inflater.inflate(R.layout.dialog_item, containerLayout, false);

            ImageView itemImage = itemView.findViewById(R.id.item_image);
            TextView itemText = itemView.findViewById(R.id.item_text);

            itemImage.setImageResource(images[position]);
            itemText.setText(viewTypes[position]);

            itemView.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onViewTypeSelected(viewTypes[position]);
                    dismiss(); // Close the BottomSheet
                }
            });

            containerLayout.addView(itemView);
        }

        return rootView;
    }


    public void setOnViewTypeSelectedListener(OnViewTypeSelectedListener listener) {
        mListener = listener;
    }

}