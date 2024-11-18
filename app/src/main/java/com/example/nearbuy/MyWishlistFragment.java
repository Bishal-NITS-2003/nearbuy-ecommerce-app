package com.example.nearbuy;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

public class MyWishlistFragment extends Fragment {
    public MyWishlistFragment() {
    }

    private RecyclerView wishlistRecyclerView;
    private Dialog loadingDialog;
    public static WishlistAdapter wishlistAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_wishlist, container, false);
        wishlistRecyclerView=root.findViewById(R.id.my_wishlist_recycler_view);

        //////////loading dialog

        loadingDialog=new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.show();

        //////////loading dialog

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        wishlistRecyclerView.setLayoutManager(linearLayoutManager);


        if(DBqueries.wishlistModelList.size() == 0){
            DBqueries.wishList.clear();
            DBqueries.loadWishlist(getContext(),loadingDialog,true);
        }else {
            loadingDialog.dismiss();
        }

        wishlistAdapter=new WishlistAdapter(DBqueries.wishlistModelList,true);
        wishlistAdapter.notifyDataSetChanged();
        wishlistRecyclerView.setAdapter(wishlistAdapter);
        return root;
    }
}