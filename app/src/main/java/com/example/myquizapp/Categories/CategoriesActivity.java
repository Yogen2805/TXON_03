package com.example.myquizapp.Categories;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.myquizapp.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.myquizapp.Categories.CategoryAdapter.*;

import static com.example.myquizapp.Categories.CategoryAdapter.SPAN_COUNT_ONE;

public class CategoriesActivity extends AppCompatActivity {

    private RecyclerView recyclerview;
    public final String TAG = getClass().getSimpleName();
    private  List<CategoryModel> list ;
    private CategoryAdapter adapter;

    private GridLayoutManager gridLayoutManager;

    private Dialog loadingDialog;

    private SearchView searchview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // For Mobile Ads
        loadAds();

        // Create Loading Dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.setCancelable(false);
        // set dialog Width & Height
       loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
      //  loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        // set Rounded Loading Dialog
        loadingDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_corners));

        // Firebase Database Reference
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        recyclerview = findViewById(R.id.recView);
        gridLayoutManager = new GridLayoutManager(this,SPAN_COUNT_ONE);

        list = new ArrayList<>();
        final CategoryAdapter adapter = new CategoryAdapter(list,gridLayoutManager);
        recyclerview.setAdapter(adapter);
        recyclerview.setLayoutManager(gridLayoutManager);

        loadingDialog.show();

        // Read Data from Firebase
        myRef.child("Category").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    list.add(dataSnapshot1.getValue(CategoryModel.class));
                }
                adapter.notifyDataSetChanged();
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());
                Toast.makeText(CategoriesActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                finish();
            }
        });
    }

    // Finish Activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id  = item.getItemId();

        switch (id){
            case android.R.id.home :
                            finish();
                            break;
            case R.id.gridView :
                switchLayout();
                switchIcon(item);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Switch Layout
    private void switchLayout() {
        if(gridLayoutManager.getSpanCount()==SPAN_COUNT_ONE){
            gridLayoutManager.setSpanCount(SPAN_COUNT_THREE);
        }else{
            gridLayoutManager.setSpanCount(SPAN_COUNT_ONE);
        }
    }

    // Switch Icon
    private void switchIcon(MenuItem item) {
        if(gridLayoutManager.getSpanCount()==SPAN_COUNT_ONE){
            item.setIcon(getResources().getDrawable(R.drawable.icon_grid));
        }else{
            item.setIcon(getResources().getDrawable(R.drawable.icon_list));
        }
    }

    // LoadAds
    private void loadAds() {
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    // SearchView Logic
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        searchview = (androidx.appcompat.widget.SearchView) menu.findItem(R.id.search).getActionView();
        searchview.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchview.setMaxWidth(Integer.MAX_VALUE);
        searchview.setQueryHint("Enter Categories");
        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                performSearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return false;
            }
        });
        return true;
    }

    private void performSearch(String s) {

        ArrayList<CategoryModel> mylist = new ArrayList<>();
        for(CategoryModel object : list) {
            if(object.getTitle().toLowerCase().contains(s.toLowerCase())){
                mylist.add(object);
            }
        }
        CategoryAdapter adapter = new CategoryAdapter(mylist, gridLayoutManager);
        recyclerview.setAdapter(adapter);
    }

}
