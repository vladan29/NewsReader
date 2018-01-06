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
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements
        SourceListFragment.OnFragmentInteractionListener,
        BlogListFragment.OnBlogListFragmentInteractionListener,
        BlogDetailFragment.OnFragmentInteractionListener {

    private int position;
    public static String endpoints = "top-headlines";
    public static String category = " ";
    public static String mySource = "cnn";
    public static String language = "en";
    public static String myLanguage = "en";
    FragmentManager fragmentManager;
    SourceListFragment sourceListFragment;
    int selectedEndpoint;
    RecyclerView topicList;
    List<CategoryObject> categoryObjects = new ArrayList<>();
    View startSource;
    View startSourceDisable;
    public static int checkedItem = 0;
    Spinner language_spinner;
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
        language_spinner = findViewById(R.id.language_spinner);


        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(MainActivity.this,
                R.array.language, R.layout.my_spinner);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);


        language_spinner.setAdapter(languageAdapter);
        language_spinner.setSelection(position);
        languageAdapter.notifyDataSetChanged();
        language_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int spinnerPosition, long l) {

                String setLanguage = adapterView.getAdapter().getItem(spinnerPosition).toString();

                if (setLanguage.equals("all")) {
                    myLanguage = " ";
                } else {
                    myLanguage = setLanguage;
                }
                position = spinnerPosition;
                if (myLanguage.equals("en")) {
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
        startSource = findViewById(R.id.action_sources);
        startSourceDisable = findViewById(R.id.action_sources_disable);
        startSource.setVisibility(View.GONE);
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


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
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
                        startSource.setVisibility(View.GONE);
                        startSourceDisable.setVisibility(View.VISIBLE);

                        break;
                    case R.id.action_endpoint:
                       // Toast.makeText(MainActivity.this, "select 2", Toast.LENGTH_LONG).show();

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Choose an endpoint");

// add a radio button list

                        final String[] giveEndpoints = {"top-headlines", "everything"};
                        endpoints = giveEndpoints[0];
                        builder.setSingleChoiceItems(giveEndpoints, checkedItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectedEndpoint = which;

                            }
                        });

// add OK and Cancel buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                checkedItem = selectedEndpoint;
                                endpoints = giveEndpoints[selectedEndpoint];
                                if (endpoints.equals("top-headlines")) {
                                    //startSource.setClickable(false);
                                    startSource.setVisibility(View.GONE);
                                    startSourceDisable.setVisibility(View.VISIBLE);
                                } else {
                                    //startSource.setClickable(true);
                                    startSource.setVisibility(View.VISIBLE);
                                    startSourceDisable.setVisibility(View.GONE);

                                }
                                createBlogList();
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton("Cancel", null);

// create and show the alert dialog
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        break;


                }

                return true;
            }
        });
        //startSource.setClickable(false);
        createBlogList();

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
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.app_bar, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        int id = item.getItemId();
//
//        if (id == R.id.action_settings) {
//            Toast.makeText(MainActivity.this, "settings", Toast.LENGTH_LONG).show();
//            return true;
//        }
//        if (id == R.id.action_language) {
//            Toast.makeText(MainActivity.this, "language", Toast.LENGTH_LONG).show();
//
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onFragmentInteraction(String source, String sourceLanguage, String name) {
        mySource = source;
        language = sourceLanguage;
        sourceName = name;
        startSource.setVisibility(View.VISIBLE);
        startSourceDisable.setVisibility(View.GONE);
        createBlogList();


//        Toast.makeText(MainActivity.this, "listener" + source, Toast.LENGTH_LONG).show();

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
            language_spinner.setEnabled(false);
        }


    }

    public void createBlogList() {

        String url_base = "https://newsapi.org/v2/" + endpoints + "?apiKey=c540ba5d76254cadb8261a1a2fac4342";
        StringBuilder builder = new StringBuilder(url_base);
        if (endpoints.equals("everything")) {
            language_spinner.setVisibility(View.GONE);
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
            if (myLanguage.equals("en")) {
                topicList.setVisibility(View.VISIBLE);
            }
            language_spinner.setVisibility(View.VISIBLE);
            String myCategory = "&category=" + category;
            if (!category.equals(" ") && myLanguage.equals("en")) {
                builder.append(myCategory);
            }
            String myLanguageFinal = "&language=" + myLanguage;
            builder.append(myLanguageFinal);

        }

        loadBlogList(builder.toString(), endpoints);


    }

    public void loadBlogList(String url, String endpoint) {
        Log.d("URL", url);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        BlogListFragment blogListFragment = BlogListFragment.newInstance(url, endpoint);
        fragmentTransaction.replace(R.id.frame_container, blogListFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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
                if (myLanguage.equals("en") && endpoints.equals("top-headlines")) {
                    topicList.setVisibility(View.VISIBLE);
                }
                if (endpoints.equals("everything")) {
                    //startSource.setClickable(true);
                    startSourceDisable.setVisibility(View.GONE);
                    startSource.setVisibility(View.VISIBLE);
                }
                if (endpoints.equals("top-headlines")) {
                    language_spinner.setEnabled(true);
                }
                super.onBackPressed();
            }
            if (currentFragment instanceof SourceListFragment) {
                startSourceDisable.setVisibility(View.GONE);
                startSource.setVisibility(View.VISIBLE);
                //startSource.setClickable(true);
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

    class CategoryObject {
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
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("category", category);
        savedInstanceState.putString("endpoints", endpoints);
        savedInstanceState.putString("myLanguage", myLanguage);
        savedInstanceState.putString("mySource", mySource);
        savedInstanceState.putInt("checkedItem", checkedItem);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        category = savedInstanceState.getString("category", " ");
        endpoints = savedInstanceState.getString("endpoints", "top-headlines");
        myLanguage = savedInstanceState.getString("myLanguage", "en");
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
