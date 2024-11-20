package com.example.nearbuy;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DBqueries {

    public static String email,fullname,profile;


    public static FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    public static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public static String points;

    public static List<CategoryModel> categoryModelList = new ArrayList<>();

    public static List<List<HomePageModel>> lists = new ArrayList<>();
    public static List<String> loadedCategoriesNames = new ArrayList<>();

    public static List<String> wishList = new ArrayList<>();
    public static List<WishlistModel> wishlistModelList=new ArrayList<>();
    public static List<String> addWith = new ArrayList<>();

    public static void loadCategories(final RecyclerView categoryRecyclerView, final Context context ){
        categoryModelList.clear();
        firebaseFirestore.collection("CATEGORIES").orderBy("index").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot documentSnapshot :task.getResult()){
                                categoryModelList.add(new CategoryModel(documentSnapshot.get("icon").toString(),documentSnapshot.get("categoryName").toString()));
                            }
                            CategoryAdapter categoryAdapter = new CategoryAdapter(categoryModelList);
                            categoryRecyclerView.setAdapter(categoryAdapter);
                            categoryAdapter.notifyDataSetChanged();
                        }else{
                            String error = task.getException().getMessage();
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }


    public static void loadFragmentData(final RecyclerView homePageRecyclerView, final Context context, final int index, String categoryName) {
        List<HomePageModel> tempList = new ArrayList<>(); // Temporary list to store data before adding to main list

        // Step 1: Fetch CATEGORIES -> TOP_DEALS
        firebaseFirestore.collection("CATEGORIES")
                .document(categoryName.toUpperCase())
                .collection("TOP_DEALS").orderBy("index").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Process view_type 0 and 1
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                if ((long) documentSnapshot.get("view_type") == 0) {
                                    List<SliderModel> sliderModelList = new ArrayList<>();
                                    long no_of_banners = (long) documentSnapshot.get("no_of_banners");
                                    for (long x = 1; x <= no_of_banners; x++) {
                                        sliderModelList.add(new SliderModel(documentSnapshot.get("banner_" + x).toString(),
                                                documentSnapshot.get("banner_" + x + "_background").toString()));
                                    }
                                    tempList.add(new HomePageModel(0, sliderModelList));
                                } else if ((long) documentSnapshot.get("view_type") == 1) {
                                    tempList.add(new HomePageModel(1, documentSnapshot.get("strip_ad_banner").toString(),
                                            documentSnapshot.get("background").toString()));
                                }
                            }

                            // Step 2: Fetch PRODUCTS -> categoryName
                            firebaseFirestore.collection("PRODUCTS")
                                    .document("aqGT4ZvNfP7CIrME1kPu")
                                    .collection(categoryName.toUpperCase())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> productTask) {
                                            if (productTask.isSuccessful()) {
                                                List<WishlistModel> viewAllProductList = new ArrayList<>();
                                                List<HorizontalProductScrollModel> horizontalProductList = new ArrayList<>();
                                                List<HorizontalProductScrollModel> gridProductList = new ArrayList<>();

                                                for (QueryDocumentSnapshot productSnapshot : productTask.getResult()) {

                                                    Object viewTypeObj = productSnapshot.get("view_type");
                                                    if (viewTypeObj != null) {
                                                        long viewType = (long) viewTypeObj;

                                                        if (viewType == 2) {
                                                            horizontalProductList.add(new HorizontalProductScrollModel(
                                                                    productSnapshot.getId(),
                                                                    productSnapshot.get("product_image_1").toString(),
                                                                    productSnapshot.get("product_title").toString(),
                                                                    productSnapshot.get("product_subtitle").toString(),
                                                                    productSnapshot.get("product_price").toString(),
                                                                    ""
                                                            ));
                                                            viewAllProductList.add(new WishlistModel(
                                                                    productSnapshot.getId(),
                                                                    productSnapshot.get("product_image_1").toString(),
                                                                    productSnapshot.get("product_title").toString(),
                                                                    productSnapshot.get("product_subtitle").toString(),
                                                                    productSnapshot.get("product_price").toString(),
                                                                    ""
                                                            ));
                                                        } else if (viewType == 3) {
                                                            gridProductList.add(new HorizontalProductScrollModel(
                                                                    productSnapshot.getId(),
                                                                    productSnapshot.get("product_image_1").toString(),
                                                                    productSnapshot.get("product_title").toString(),
                                                                    productSnapshot.get("product_subtitle").toString(),
                                                                    productSnapshot.get("product_price").toString(),
                                                                    ""
                                                            ));
                                                        }
                                                    }
                                                }

                                                if (!horizontalProductList.isEmpty()) {
                                                    tempList.add(new HomePageModel(2,
                                                            "Best Deals", // Replace with actual title
                                                            "#ffffff",    // Replace with actual background
                                                            horizontalProductList,
                                                            viewAllProductList));
                                                }

                                                if (!gridProductList.isEmpty()) {
                                                    tempList.add(new HomePageModel(3,
                                                            "Best Sellers", // Replace with actual title
                                                            "#ffffff",      // Replace with actual background
                                                            gridProductList,
                                                            null));
                                                }

                                                // Update the main list and UI once both queries are done
                                                lists.set(index, tempList);
                                                HomePageAdapter homePageAdapter = new HomePageAdapter(lists.get(index));
                                                homePageRecyclerView.setAdapter(homePageAdapter);
                                                homePageAdapter.notifyDataSetChanged();
                                                HomeFragment.swipeRefreshLayout.setRefreshing(false);
                                            } else {
                                                Toast.makeText(context, productTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    public static void loadWishlist(final Context context, final Dialog dialog,final boolean loadProductData){
        wishList.clear();
        firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .collection("USER_DATA").document("MY_WISHLIST").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            for(long x=0;x<(long)task.getResult().get("list_size");x++){
                                wishList.add(task.getResult().get("product_ID_"+x).toString());

                                if(DBqueries.wishList.contains(ProductDetailsActivity.productID)){
                                    ProductDetailsActivity.ALREADY_ADDED_TO_WISHLIST=true;
                                    if(ProductDetailsActivity.addToWishListBtn != null) {
                                        ProductDetailsActivity.addToWishListBtn.setSupportImageTintList(context.getResources().getColorStateList(R.color.colorPrimary));
                                    }
                                }else {
                                    if(ProductDetailsActivity.addToWishListBtn != null) {
                                        ProductDetailsActivity.addToWishListBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#9e9e9e")));
                                    }
                                    ProductDetailsActivity.ALREADY_ADDED_TO_WISHLIST=false;
                                }

                                if(loadProductData) {
                                    wishlistModelList.clear();
                                    final String productId=task.getResult().get("product_ID_" + x).toString();
                                    firebaseFirestore.collection("PRODUCTS").document(productId).get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        final DocumentSnapshot documentSnapshot=task.getResult();
                                                        wishlistModelList.add(new WishlistModel(
                                                                productId
                                                                ,documentSnapshot.get("product_image_1").toString()
                                                                , documentSnapshot.get("product_title").toString()
                                                                , documentSnapshot.get("product_description").toString()
                                                                , documentSnapshot.get("product_price").toString()
                                                                , ""

                                                        ));
                                                        MyWishlistFragment.wishlistAdapter.notifyDataSetChanged();

                                                    } else {
                                                        String error = task.getException().getMessage();
                                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            });
                                }
                            }

                        }
                        else {
                            String error=task.getException().getMessage();
                            Toast.makeText(context,error,Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
    }


    public static void removeFromWishlist(final int index, final Context context) {
        final String removedProductId=wishList.get(index);
        wishList.remove(index);

        Map<String, Object> updateWishlist = new HashMap<>();

        for (int x = 0; x < wishList.size(); x++) {
            updateWishlist.put("product_ID_" + x, wishList.get(x));
        }
        updateWishlist.put("list_size", (long) wishList.size());

        firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .collection("USER_DATA").document("MY_WISHLIST").set(updateWishlist)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if(wishlistModelList.size() != 0){
                                wishlistModelList.remove(index);
                                MyWishlistFragment.wishlistAdapter.notifyDataSetChanged();
                            }
                            ProductDetailsActivity.ALREADY_ADDED_TO_WISHLIST=false;
                            Toast.makeText(context,"Removed Successfully!",Toast.LENGTH_SHORT).show();
                        } else {
                            if(ProductDetailsActivity.addToWishListBtn != null) {
                                ProductDetailsActivity.addToWishListBtn.setSupportImageTintList(context.getResources().getColorStateList(R.color.colorPrimary));
                            }
                            wishList.add(index,removedProductId);
                            String error=task.getException().getMessage();
                            Toast.makeText(context,error,Toast.LENGTH_SHORT).show();
                        }
                        ProductDetailsActivity.running_wishlist_querry=false;
                    }
                });

    }

    public static void clearData(){

        categoryModelList.clear();
        lists.clear();
        loadedCategoriesNames.clear();
        wishList.clear();
        wishlistModelList.clear();

    }







}
