package com.vladan.newsreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements
        SourceListFragment.OnFragmentInteractionListener,
        BlogListFragment.OnBlogListFragmentInteractionListener,
        BlogDetailFragment.OnFragmentInteractionListener {


    public static String endpoints = "top-headlines";
    public static String category = " ";
    public static String mySource = "cnn";
    public static String language = "en";
    public static String myCountry = "us";
    FragmentManager fragmentManager;
    SourceListFragment sourceListFragment;
    int selectedEndpoint;
    RecyclerView topicList;
    List<CategoryObject> categoryObjects = new ArrayList<>();
    MenuItem startSource;
    MenuItem startSourceDisable;
    public static int checkedItem = 0;
    Spinner country_spinner;
    TextView tvSourceLanguage;
    TextView tvSourceName;
    TextView tvIsConnected;
    public static String sourceName = "CNN";
    BroadcastReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources res = getResources();

        fragmentManager = getSupportFragmentManager();
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        tvSourceLanguage = findViewById(R.id.source_language);
        tvSourceName = findViewById(R.id.source_name);
        tvIsConnected = findViewById(R.id.is_connected);
        country_spinner = findViewById(R.id.language_spinner);


        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(MainActivity.this,
                R.array.country, R.layout.my_spinner);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        country_spinner.setAdapter(languageAdapter);
        country_spinner.setSelected(false);
        country_spinner.setSelection(0, true);
        country_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int spinnerPosition, long l) {

                myCountry = (adapterView.getAdapter().getItem(spinnerPosition).toString()).replaceAll("\\s+", "");

                if (myCountry.equals("us") || myCountry.equals("au") || myCountry.equals("gb")) {
                    topicList.setVisibility(View.VISIBLE);
                } else {
                    topicList.setVisibility(View.GONE);
                }
                createBlogList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });

        topicList = findViewById(R.id.topic);
        final String[] topic = res.getStringArray(R.array.category);
        for (String item : topic) {
            CategoryObject categoryObject = new CategoryObject();
            categoryObject.setCategory(item);
            View categoryDividerRoot = getLayoutInflater().inflate(R.layout.category_list_row, null);
            TextView tvCategoryDivider = categoryDividerRoot.findViewById(R.id.category_divider);
            categoryObject.setCategoryDivider(tvCategoryDivider);
            categoryObjects.add(categoryObject);
        }

        final CategoryAdapter categoryAdapter = new CategoryAdapter(categoryObjects);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        topicList.setLayoutManager(layoutManager);
        categoryAdapter.SetOnItemClickListener(new CategoryAdapter.OnItemClickListener() {
            int previousClickPosition = 0;

            @Override
            public void onItemClick(View view, int position) {
                String label = categoryObjects.get(position).getCategory();
                if (label.equals("all")) {
                    category = " ";
                } else {
                    category = label;
                }
                createBlogList();
                CategoryAdapter.previousClickPosition = previousClickPosition;
                CategoryAdapter.clickPosition = position;
                previousClickPosition = position;
                categoryAdapter.notifyDataSetChanged();
            }
        });
        topicList.setAdapter(categoryAdapter);

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        startSource = bottomNavigationView.getMenu().findItem(R.id.action_sources).setVisible(false);
        startSourceDisable = bottomNavigationView.getMenu().findItem(R.id.action_sources_disable).setVisible(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_sources:

                        sourceListFragment = new SourceListFragment();
                        FragmentTransaction transaction1 = fragmentManager.beginTransaction();
                        transaction1.replace(R.id.frame_container, sourceListFragment);
                        transaction1.addToBackStack(null);
                        transaction1.commit();
                        startSource.setVisible(false);
                        startSourceDisable.setVisible(true);

                        break;
                    case R.id.action_endpoint:

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Choose filter");

                        final String[] giveEndpoints = {"top-headlines", "everything"};
                        final String[] titleEndpoints = {"Top headlines", "Everything"};
                        endpoints = giveEndpoints[0];
                        builder.setSingleChoiceItems(titleEndpoints, checkedItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectedEndpoint = which;
                            }
                        });

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                checkedItem = selectedEndpoint;
                                endpoints = giveEndpoints[selectedEndpoint];
                                if (endpoints.equals("top-headlines")) {
                                    startSource.setVisible(false);
                                    startSourceDisable.setVisible(true);
                                    country_spinner.setVisibility(View.VISIBLE);
                                } else {
                                    startSource.setVisible(true);
                                    startSourceDisable.setVisible(false);
                                    country_spinner.setVisibility(View.GONE);
                                }
                                createBlogList();
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton("Cancel", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        break;
                }
                return true;
            }
        });

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    if (!isOnline(context)) {
                        tvIsConnected.setVisibility(View.VISIBLE);
                    } else {
                        tvIsConnected.setVisibility(View.GONE);
                    }
                }

            }
        };
        registerReceiver(receiver, intentFilter);

        createBlogList();
    }


    @Override
    public void onFragmentInteraction(String source, String sourceLanguage, String name) {
        mySource = source;
        language = sourceLanguage;
        sourceName = name;
        startSource.setVisible(true);
        startSourceDisable.setVisible(false);
        createBlogList();


    }

    @Override
    public void onBlogListFragmentInteraction(String blogUrl) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        BlogDetailFragment blogDetailFragment = BlogDetailFragment.newInstance(blogUrl);
        fragmentTransaction.replace(R.id.frame_container, blogDetailFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        View botomBarr = findViewById(R.id.bottom_navigation);
        botomBarr.setVisibility(View.INVISIBLE);
        LinearLayout content = findViewById(R.id.content_main);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) content.getLayoutParams();
        params.setMargins(params.leftMargin = 0, params.topMargin = this.getSupportActionBar().getHeight(), params.rightMargin = 0, params.bottomMargin = 0);
        content.setLayoutParams(params);
        topicList.setVisibility(View.GONE);
        if (endpoints.equals("top-headlines")) {
            country_spinner.setEnabled(false);
        }


    }

    public void createBlogList() {

        String url_base = "https://newsapi.org/v2/" + endpoints + "?apiKey=c540ba5d76254cadb8261a1a2fac4342";
        StringBuilder builder = new StringBuilder(url_base);

        if (endpoints.equals("everything")) {

            tvSourceLanguage.setText(language);
            tvSourceLanguage.setVisibility(View.VISIBLE);
            tvSourceName.setText(sourceName);
            tvSourceName.setVisibility(View.VISIBLE);
            topicList.setVisibility(View.GONE);
            String mySourceFinal = "&sources=" + mySource;
            if (!mySource.equals(" ")) {
                builder.append(mySourceFinal);
            }
            String myLanguageFinal = "&language=" + language;
            builder.append(myLanguageFinal);
        } else {
            tvSourceName.setVisibility(View.GONE);
            tvSourceLanguage.setVisibility(View.GONE);
            if (myCountry.equals("us") || myCountry.equals("au") || myCountry.equals("gb")) {
                topicList.setVisibility(View.VISIBLE);
            }

            String myCategory = "&category=" + category;
            if (!category.equals(" ") && (myCountry.equals("us") || myCountry.equals("au") || myCountry.equals("gb"))) {
                builder.append(myCategory);
            }
            String myCountryFinal = "&country=" + myCountry;
            builder.append(myCountryFinal);
        }

        loadBlogList(builder.toString(), endpoints);


    }

    public void loadBlogList(String url, String endpoint) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        BlogListFragment blogListFragment = BlogListFragment.newInstance(url, endpoint);
        fragmentTransaction.replace(R.id.frame_container, blogListFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onBackPressed() {
        int count = MainActivity.this.getSupportFragmentManager().getBackStackEntryCount();
        if (count > 1) {
            Fragment currentFragment = fragmentManager
                    .findFragmentById(R.id.frame_container);
            if (currentFragment instanceof BlogDetailFragment) {

                View botomBarr = findViewById(R.id.bottom_navigation);
                botomBarr.setVisibility(View.VISIBLE);
                LinearLayout content = findViewById(R.id.content_main);
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) content.getLayoutParams();
                float pixels = 60 * MainActivity.this.getResources().getDisplayMetrics().density;
                params.setMargins(params.leftMargin = 0, params.topMargin = this.getSupportActionBar().getHeight(), params.rightMargin = 0, params.bottomMargin = (int) pixels);
                content.setLayoutParams(params);
                if ((myCountry.equals("us") || myCountry.equals("au") || myCountry.equals("gb")) && endpoints.equals("top-headlines")) {
                    topicList.setVisibility(View.VISIBLE);
                }
                if (endpoints.equals("everything")) {
                    startSourceDisable.setVisible(false);
                    startSource.setVisible(true);
                }
                if (endpoints.equals("top-headlines")) {
                    country_spinner.setEnabled(true);
                }
                super.onBackPressed();
            }
            if (currentFragment instanceof SourceListFragment) {
                startSourceDisable.setVisible(false);
                startSource.setVisible(true);
                super.onBackPressed();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.remove(currentFragment);
                transaction.commit();


            }
            if (currentFragment instanceof BlogListFragment) {
                finish();

            }

        } else finish();

    }

    static class CategoryObject {
        String category;
        TextView categoryDivider;

        CategoryObject() {
        }

        public CategoryObject(String category, TextView categoryDivider) {
            this.category = category;
            this.categoryDivider = categoryDivider;
        }

        String getCategory() {
            return category;
        }

        void setCategory(String category) {
            this.category = category;
        }

        public TextView getCategoryDivider() {
            return categoryDivider;
        }

        void setCategoryDivider(TextView categoryDivider) {
            this.categoryDivider = categoryDivider;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("category", category);
        savedInstanceState.putString("endpoints", endpoints);
        savedInstanceState.putString("myCountry", myCountry);
        savedInstanceState.putString("mySource", mySource);
        savedInstanceState.putInt("checkedItem", checkedItem);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        category = savedInstanceState.getString("category", " ");
        endpoints = savedInstanceState.getString("endpoints", "top-headlines");
        myCountry = savedInstanceState.getString("myCountry", "us");
        mySource = savedInstanceState.getString("mySource", "cnn");
        checkedItem = savedInstanceState.getInt("checkedItem", 0);
    }

    public static boolean isOnline(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni != null && ni.isConnected();
    }

    @Override
    protected void onDestroy() {

        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }
}
