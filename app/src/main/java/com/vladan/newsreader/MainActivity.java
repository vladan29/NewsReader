package com.vladan.newsreader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements
        SourceListFragment.OnFragmentInteractionListener,
        BlogListFragment.OnBlogListFragmentInteractionListener,
        BlogDetailFragment.OnFragmentInteractionListener {

    private DrawerLayout drawer;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String language;
    private int position;
    private String url;
    private String endpoints = "top-headlines";
    FragmentManager fragmentManager;
    SourceListFragment sourceListFragment;
    int selectedEndpoint;
    String topic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources res = getResources();


        sharedPreferences = MainActivity.this.getSharedPreferences("com.vladan.newsreader", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        language = sharedPreferences.getString("language", "all");
        position = sharedPreferences.getInt("position", 0);
        if (sharedPreferences.getInt("checkedItem", 0) == 0) {
            endpoints = "top-headlines";
        } else {
            endpoints = "everything";
        }

        url = res.getString(R.string.host) + endpoints + "?";
        fragmentManager = getSupportFragmentManager();
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String[] category = getResources().getStringArray(R.array.category);
        drawer = findViewById(R.id.drawer_layout);
        ListView drawerList = findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<>(MainActivity.this, R.layout.drawer_list_item, category));
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                topic = adapterView.getAdapter().getItem(position).toString();

                Toast.makeText(MainActivity.this, position + " " + topic, Toast.LENGTH_LONG).show();

                drawer.closeDrawer(GravityCompat.START);


            }
        });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Spinner language_spinner = findViewById(R.id.language_spinner);


        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(MainActivity.this,
                R.array.language, R.layout.my_spinner);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);

        language_spinner.setAdapter(languageAdapter);
        language_spinner.setSelection(position);
        languageAdapter.notifyDataSetChanged();
        language_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int spinnerPosition, long l) {

                language = adapterView.getAdapter().getItem(spinnerPosition).toString();
                position = spinnerPosition;
                editor.putString("language", language);
                editor.putInt("position", position);
                editor.apply();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_sources:
                        View startSource = findViewById(R.id.action_sources);

                        sourceListFragment = new SourceListFragment();
                        FragmentTransaction transaction1 = fragmentManager.beginTransaction();
                        transaction1.replace(R.id.frame_container, sourceListFragment);
                        transaction1.addToBackStack(null);
                        transaction1.commit();


                        startSource.setClickable(false);

                        break;
                    case R.id.action_endpoint:
                        Toast.makeText(MainActivity.this, "select 2", Toast.LENGTH_LONG).show();

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Choose an endpoint");

// add a radio button list

                        String[] giveEndpoints = {"top-headlines", "everything"};
                        int checkedItem = sharedPreferences.getInt("checkedItem", 0);
                        endpoints = giveEndpoints[checkedItem];
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
                                // user clicked OK
                                editor.putInt("checkedItem", selectedEndpoint);
                                editor.apply();

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
        createBlogList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Toast.makeText(MainActivity.this, "settings", Toast.LENGTH_LONG).show();
            return true;
        }
        if (id == R.id.action_language) {
            Toast.makeText(MainActivity.this, "language", Toast.LENGTH_LONG).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String source) {
        String id = source;

        Toast.makeText(MainActivity.this, "listener" + source, Toast.LENGTH_LONG).show();

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
        params.setMargins(params.leftMargin = 0, params.topMargin = 0, params.rightMargin = 0, params.bottomMargin = 0);
        content.setLayoutParams(params);

    }

    public void createBlogList() {
        String url = "https://newsapi.org/v2/top-headlines?apiKey=c540ba5d76254cadb8261a1a2fac4342&page=0&language=en";
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        BlogListFragment allAdvertiserAdFragment = BlogListFragment.newInstance(url);
        fragmentTransaction.replace(R.id.frame_container, allAdvertiserAdFragment);
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
                params.setMargins(params.leftMargin = 0, params.topMargin = 0, params.rightMargin = 0, params.bottomMargin = (int) pixels);
                content.setLayoutParams(params);
                super.onBackPressed();
            }
            if (currentFragment instanceof SourceListFragment) {

                View startSource = findViewById(R.id.action_sources);
                startSource.setClickable(true);
                super.onBackPressed();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.remove(currentFragment);
                transaction.commit();


            }
        } else finish();

    }
}
