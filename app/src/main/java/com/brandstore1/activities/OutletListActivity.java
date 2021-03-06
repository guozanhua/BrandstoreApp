package com.brandstore1.activities;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.brandstore1.BrandstoreApplication;
import com.brandstore1.R;
import com.brandstore1.adapters.OutletListAdapter;
import com.brandstore1.asynctasks.OutletListAsyncTask;
import com.brandstore1.entities.Outlet;
import com.brandstore1.entities.OutletListFilterConstraint;
import com.brandstore1.fragments.OutletListFilters;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;


public class OutletListActivity extends ActionBarActivity {

    private static final String TAG = OutletListActivity.class.getSimpleName();

    ListView lvOutletListView;
    Toolbar toolbar;

    MenuItem favoriteMenuItem;
    MenuItem saleMenuItem;
    MenuItem filterItem;
    OutletListAdapter mOutletListAdapter;
    OutletListType outletListType;
    OutletListFilters outletListFilters;

    ArrayList<Outlet> outletArrayList = new ArrayList<Outlet>();

    OutletListFilterConstraint outletListFilterConstraint;

    public enum OutletListType {
        CLICKED_ON_TAG, CLICKED_ON_COLLECTION, SEARCHED_QUERY, ALL_FAVORITE, ALL_ON_SALE,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outlet_list);

        // Get tracker.
        Tracker t = ((BrandstoreApplication) getApplication()).getTracker(BrandstoreApplication.TrackerName.APP_TRACKER);
        // Send a screen view.
        t.setScreenName(TAG);
        t.send(new HitBuilders.ScreenViewBuilder().build());


        /*
            Bundle setup >
            Get title for toolbar
            Get parameter to decide call in AsyncTask
         */
        Bundle bundle = getIntent().getExtras();
        String query = bundle.getString("name");
        String id = bundle.getString("id");
        outletListType = (OutletListType) bundle.get("type");

        /*
            Toolbar setup
         */
        toolbar = (Toolbar) findViewById(R.id.outletlisttoolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setSubtitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(query);

        /*
            UniversalImageLoader setup
         */
        File cacheDir = StorageUtils.getCacheDirectory(this);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.blank_screen) // resource or drawable
                .showImageForEmptyUri(R.drawable.blank_screen) // resource or drawable
                .showImageOnFail(R.drawable.blank_screen) // resource or drawable
                .resetViewBeforeLoading(true)  // default
                .cacheInMemory(true) // default
                .cacheOnDisk(true) // default
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCache(new UnlimitedDiscCache(cacheDir))
                .defaultDisplayImageOptions(options)
                .build();
        ImageLoader.getInstance().init(config);

        /*
            ListView setup
         */
        TextView emptyView = (TextView) findViewById(R.id.outlet_list_empty_textView);
        lvOutletListView = (ListView) findViewById(R.id.outlet_list_list_view);
        lvOutletListView.setEmptyView(emptyView);

        /*
            Adapter setup
         */
        mOutletListAdapter = new OutletListAdapter(outletArrayList, this, toolbar, emptyView);
        lvOutletListView.setAdapter(mOutletListAdapter);

        /*
            AsyncTask setup
         */
        // Basic (ArrayList , Adapter , this )
        // UI Elements : ( toolbar , emptyView )
        // Parameters : query , id , outletListType

        OutletListAsyncTask mOutletListAsyncTask = new OutletListAsyncTask(outletArrayList, query, mOutletListAdapter, id, emptyView, toolbar, this, outletListType);
        mOutletListAsyncTask.execute();
        lvOutletListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), OutletDetailsActivity.class);
                intent.putExtra("id", outletArrayList.get(position).getId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_outlet_list, menu);

        // Initialize the menuItem variables
        /*favoriteMenuItem = menu.findItem(R.id.display_favorites);
        saleMenuItem = menu.findItem(R.id.on_sale);

        if (outletListType == OutletListType.ALL_ON_SALE) {
            saleMenuItem.setChecked(true);
            saleMenuItem.setIcon(R.drawable.sale_on);
            saleMenuItem.setEnabled(false);
        }
        else if(outletListType == OutletListType.ALL_FAVORITE){
            favoriteMenuItem.setChecked(true);
            favoriteMenuItem.setIcon(R.drawable.favselect);
            favoriteMenuItem.setEnabled(false);
        }*/
        filterItem = menu.findItem(R.id.filter_button);
        setFilterIconSelected(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                //noinspection SimplifiableIfStatement
                onBackPressed();
                return true;
            /*case R.id.display_favorites:
                onFavoriteMenuItemClicked(!item.isChecked());
                break;
            case R.id.on_sale:
                onSaleMenuItemClicked(!item.isChecked());
                break;*/
            case R.id.filter_button:
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                outletListFilters = new OutletListFilters();
                fragmentTransaction.add(R.id.fl_filters, outletListFilters);
                fragmentTransaction.commit();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onFavoriteMenuItemClicked(boolean toBeSelected) {
        // if((!saleMenuItem.isChecked() && !favoriteMenuItem.isChecked()) || (saleMenuItem.isChecked() && favoriteMenuItem.isChecked()) ||(saleMenuItem.isChecked() && !favoriteMenuItem.isChecked()) ){
        mOutletListAdapter.setOutletListFromOriginal();
        //   }
        if (toBeSelected) {
            //filter favorites
            favoriteMenuItem.setChecked(true);
            favoriteMenuItem.setIcon(R.drawable.favselect);
            if (!saleMenuItem.isChecked())
                mOutletListAdapter.getFilter().filter("fav");
            else {
                //mOutletListAdapter.resetData();
                mOutletListAdapter.getFilter().filter("favAndSale");
            }
        } else {
            favoriteMenuItem.setChecked(false);
            favoriteMenuItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
            if (saleMenuItem.isChecked()) {
                //mOutletListAdapter.resetData();
                mOutletListAdapter.getFilter().filter("Sale");
            } else
                mOutletListAdapter.getFilter().filter("Y");
        }
    }

    public void onSaleMenuItemClicked(boolean toBeSelected) {
        // if((!saleMenuItem.isChecked() && !favoriteMenuItem.isChecked()) || (saleMenuItem.isChecked() && favoriteMenuItem.isChecked()) ||(!saleMenuItem.isChecked() && favoriteMenuItem.isChecked()) ){
        mOutletListAdapter.setOutletListFromOriginal();
        //}
        if (toBeSelected) {
            //filter OnSale Outlets
            saleMenuItem.setChecked(true);
            saleMenuItem.setIcon(R.drawable.sale_on);
            if (!favoriteMenuItem.isChecked())
                mOutletListAdapter.getFilter().filter("Sale");
            else mOutletListAdapter.getFilter().filter("favAndSale");
        } else {
            saleMenuItem.setChecked(false);
            saleMenuItem.setIcon(R.drawable.saleicon);
            if (favoriteMenuItem.isChecked()) {
                mOutletListAdapter.setOutletListFromOriginal();
                mOutletListAdapter.getFilter().filter("fav");
            } else
                mOutletListAdapter.getFilter().filter("Y");
        }
    }

    @Override
    public void onBackPressed() {
        if (OutletListFilters.isOpen() && outletListFilters != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(outletListFilters);
            fragmentTransaction.commit();
        } else super.onBackPressed();
    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    public void setOutletListFilterConstraint(OutletListFilterConstraint outletListFilterConstraint){
        this.outletListFilterConstraint = outletListFilterConstraint;
    }
    public OutletListFilterConstraint getOutletListFilterConstraint(){
        return outletListFilterConstraint;
    }

    public void resetMenuItems(){
        if(toolbar!=null){
            this.setSupportActionBar(toolbar);
        }
    }

    public void resetArrayList(){
        if(this.outletListFilterConstraint!=null){
            mOutletListAdapter.setOutletListFilterConstraint(outletListFilterConstraint);
            mOutletListAdapter.setOutletListFromOriginal();
            mOutletListAdapter.getFilter().filter("");
        }
    }
    public void setFilterIconSelected(boolean isSet){
        Drawable drawable = toolbar.getMenu().findItem(R.id.filter_button).getIcon();
        if (drawable != null) {
            // If we don't mutate the drawable, then all drawable's with this id will have a color
            // filter applied to it.
            drawable.mutate();
            if(isSet){
                drawable.setColorFilter(getResources().getColor(android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);
            }
            else{
                drawable.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
            }

            //drawable.setAlpha(alpha);
        }
    }

}
