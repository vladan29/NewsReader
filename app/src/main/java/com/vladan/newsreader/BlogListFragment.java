package com.vladan.newsreader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class BlogListFragment extends Fragment {

    FragmentActivity activity;
    private OnBlogListFragmentInteractionListener mListener;
    RecyclerView recyclerView;
    BlogListAdapter adapter;
    List<BlogDetails> blogDetailsList = new ArrayList<>();
    String url;
    String newUrl;
    public int mTotalItemCount;
    public static int scrollFlag = 1;
    public int pageDown = 1;
    public int pageUp = 1;
    boolean scrollingDown = true;
    boolean scrollingUp;
    int fVItem = 0;
    public static int lastFirstVisibleItem = 0;
    int mStatusCode;
    Map<String, String> responseHeaders;
    String myEndpoint;
    int newItems;
    RequestQueue queue;
    public static String TAG = "GET";
    AlertDialog dialog;


    public BlogListFragment() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (FragmentActivity) context;
        if (context instanceof OnBlogListFragmentInteractionListener) {
            mListener = (OnBlogListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public static BlogListFragment newInstance(String url, String endpoint) {

        BlogListFragment blogListFragment = new BlogListFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        args.putString("endpoint", endpoint);
        blogListFragment.setArguments(args);
        return blogListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getArguments().getString("url", " ");
        myEndpoint = getArguments().getString("endpoint", " ");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_blog_list, container, false);
        recyclerView = rootView.findViewById(R.id.blog_list_recycler);
        adapter = new BlogListAdapter(blogDetailsList);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SeparatorDecoration itemDecoration = new SeparatorDecoration(recyclerView.getContext(), Color.parseColor("#607D8B"), 1.8f);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter.SetOnItemClickListener(new BlogListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String blogUrl = blogDetailsList.get(position).getBlogUrl();
                onItemClicked(blogUrl);
            }
        });
    }

    //@SuppressWarnings("deprecation")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final LinearLayoutManager myManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    int visibleItemCount = myManager.getChildCount();
                    mTotalItemCount = myManager.getItemCount();
                    fVItem = myManager.findFirstVisibleItemPosition();
                    int dy = fVItem - lastFirstVisibleItem;
                    if (dy > 0) {
                        scrollingDown = true;
                        scrollingUp = false;
                    }

                    if (dy < 0) {
                        scrollingUp = true;
                        scrollingDown = false;
                    }
                    lastFirstVisibleItem = fVItem;
                    int startItem = visibleItemCount + 3;

                    if (scrollFlag == 0 && scrollingDown && mTotalItemCount != 0 && (mTotalItemCount - fVItem) <= startItem && mTotalItemCount % 20 == 0 && newItems == 20) {
                        pageDown++;

                        newUrl = url + "&page=" + String.valueOf(pageDown);
                        fetchData(newUrl);

                        scrollFlag = 1;
                    }

                    if (scrollFlag == 0 && scrollingUp && mTotalItemCount != 0 && fVItem <= 3 && pageUp > 1 && newItems == 20) {

                        pageUp--;
                        newUrl = url + "&page=" + String.valueOf(pageUp);
                        fetchData(newUrl);
                        scrollFlag = 1;
                    }
                }

            });

        } else {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int visibleItemCount = myManager.getChildCount();
                    mTotalItemCount = myManager.getItemCount();
                    fVItem = myManager.findFirstVisibleItemPosition();
                    Log.d("DY", String.valueOf(dy));
                    if (dy > 0) {
                        scrollingDown = true;
                        scrollingUp = false;
                    }
                    if (dy < 0) {
                        scrollingUp = true;
                        scrollingDown = false;
                    }

                    int startItem = visibleItemCount + 3;

                    if (scrollFlag == 0 && scrollingDown && mTotalItemCount != 0 && (mTotalItemCount - fVItem) <= startItem && mTotalItemCount % 20 == 0 && newItems == 20) {
                        pageDown++;

                        newUrl = url + "&page=" + String.valueOf(pageDown);
                        fetchData(newUrl);

                        scrollFlag = 1;
                    }

                    if (scrollFlag == 0 && scrollingUp && mTotalItemCount != 0 && fVItem <= 3 && pageUp > 1 && newItems == 20) {

                        pageUp--;
                        newUrl = url + "&page=" + String.valueOf(pageUp);
                        fetchData(newUrl);
                        scrollFlag = 1;
                    }
                }
            });
        }
        if (blogDetailsList.size() == 0) {
            fetchData(url);
        }

    }

    public void onItemClicked(String blogUrl) {
        if (mListener != null) {
            mListener.onBlogListFragmentInteraction(blogUrl);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnBlogListFragmentInteractionListener {
        void onBlogListFragmentInteraction(String blogUrl);
    }

    public int fetchData(String url) {
        showProgressDialog(getActivity());
        queue = AppController.getInstance().getRequestQueue();
        StringRequest getRequest = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideProgressDialog();
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray articles = (JSONArray) jsonObject.get("articles");
                            newItems = articles.length();
                            for (int i = 0; i < articles.length(); i++) {
                                JSONObject blog = articles.getJSONObject(i);
                                BlogDetails blogDetails = new BlogDetails();
                                blogDetails.setBlogTitle(blog.optString("title"));
                                blogDetails.setBlogDescription(blog.optString("description"));
                                blogDetails.setBlogUrl(blog.optString("url"));
                                blogDetails.setBlogUrlToImage(blog.optString("urlToImage"));
                                blogDetails.setPublishedAt(blog.optString("publishedAt"));

                                if (scrollingDown && newItems > 0) {
                                    blogDetailsList.add(blogDetails);
                                }
                                if (scrollingUp && newItems > 0) {
                                    blogDetailsList.add(i, blogDetails);
                                    fVItem++;
                                }
                            }
                            if (scrollingDown && blogDetailsList.size() > 60 && articles.length() % 20 == 0 && newItems == 20) {
                                while (blogDetailsList.size() > 60) {
                                    AppController.getInstance().getRequestQueue().getCache()
                                            .remove(blogDetailsList.get(0).getBlogUrlToImage());
                                    blogDetailsList.remove(0);
                                    fVItem--;

                                }
                                pageUp++;
                            }
                            if (scrollingUp && blogDetailsList.size() > 60 && newItems == 20) {
                                while (blogDetailsList.size() > 60) {
                                    AppController.getInstance().getRequestQueue().getCache()
                                            .remove(blogDetailsList.get(blogDetailsList.size() - 1).getBlogUrlToImage());
                                    blogDetailsList.remove(blogDetailsList.size() - 1);

                                }
                                pageDown--;
                            }
                            adapter.notifyDataSetChanged();
                            ((LinearLayoutManager) (recyclerView.getLayoutManager())).scrollToPositionWithOffset(fVItem, 0);
                            scrollFlag = 0;
                        } catch (JSONException e) {

                            Log.d("JSON", "empty string");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR", error.getMessage());
            }
        }) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                mStatusCode = response.statusCode;
                responseHeaders = response.headers;
                return super.parseNetworkResponse(response);
            }

        };
        int retries = 1;
        getRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                retries, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequest.setTag(TAG);
        queue.add(getRequest);

        return scrollFlag;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }

    public void showProgressDialog(Activity activity) {

        AlertDialog.Builder dialogBuilder;
        dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.progress_dialog_layout, null);
        TextView message = dialogView.findViewById(R.id.textView9);
        message.setText(getResources().getString(R.string.alert_dialog_download));
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    public void hideProgressDialog() {

        dialog.dismiss();
    }
}
