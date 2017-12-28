package com.vladan.newsreader;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    public int mTotalItemCount;
    public static int scrollFlag = 0;
    public int pageDown = 0;
    public int pageUp = 0;
    boolean scrollingDown = true;
    boolean scrollingUp;
    int fVItem = 0;
    int mStatusCode;
    Map<String, String> responseHeaders;


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

    public static BlogListFragment newInstance(String url) {

        BlogListFragment blogListFragment = new BlogListFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        blogListFragment.setArguments(args);
        return blogListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getArguments().getString("url"," ");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_blog_list, container, false);
        recyclerView=rootView.findViewById(R.id.blog_list_recycler);
        adapter=new BlogListAdapter(activity,blogDetailsList);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter.SetOnItemClickListener(new BlogListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String blogUrl=blogDetailsList.get(position).getBlogUrl();
                onItemClicked(blogUrl);
            }
        });
    }
 @SuppressWarnings("deprecation")
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
                    int dy = scrollY - oldScrollY;
                    if (dy > 0) {
                        scrollingDown = true;
                        scrollingUp = false;
                        Log.i("SCROLLING DOWN", "TRUE");
                    }
                    if (dy < 0) {
                        scrollingUp = true;
                        scrollingDown = false;
                        Log.i("SCROLLING UP", "TRUE");
                    }

                    int startItem = visibleItemCount + 3;

                    if (scrollFlag == 0 && scrollingDown && mTotalItemCount != 0 && (mTotalItemCount - fVItem) <= startItem && mTotalItemCount % 20 == 0) {
                        pageDown++;

                        url = url + "&page=" + String.valueOf(pageDown);
                        fetchData(url);

                        scrollFlag = 1;
                        Log.d("URL", url);
                    }

                    Log.d("TotalItemCount", String.valueOf(mTotalItemCount));
                    Log.d("FirstVisibleItem", String.valueOf(fVItem));
                    Log.d("VisibleItemCount", String.valueOf(visibleItemCount));

                    if (scrollFlag == 0 && scrollingUp && mTotalItemCount != 0 && fVItem <= 3 && pageUp > 0) {

                        pageUp--;
                        url = url + "&page=" + String.valueOf(pageUp);
                        fetchData(url);
                        scrollFlag = 1;
                        Log.d("URL", url);
                        Log.d("TotalItemCount", String.valueOf(mTotalItemCount));
                        Log.d("FirstVisibleItem", String.valueOf(fVItem));
                        Log.d("VisibleItemCount", String.valueOf(visibleItemCount));
                    }
                }
            });
        } else {
            recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
        }

        fetchData(url);
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

        RequestQueue queue = AppController.getInstance().getRequestQueue();
        StringRequest getRequest = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        // hideProgressDialog();
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray articles = (JSONArray) jsonObject.get("articles");
                            for (int i = 0; i < articles.length(); i++) {
                                JSONObject blog = articles.getJSONObject(i);
                                BlogDetails blogDetails = new BlogDetails();
                                blogDetails.setBlogTitle(blog.optString("title"));
                                blogDetails.setBlogDescription(blog.optString("description"));
                                blogDetails.setBlogUrl(blog.optString("url"));
                                blogDetails.setBlogUrlToImage(blog.optString("urlToImage"));
                                blogDetails.setPublishedAt(blog.optString("publishedAt"));

                                if (scrollingDown){
                                    blogDetailsList.add(blogDetails);
                                }
                                if (scrollingUp) {
                                    blogDetailsList.add(i, blogDetails);
                                    fVItem++;
                                }
                            }
                            if (scrollingDown && blogDetailsList.size() > 60 && articles.length() % 20 == 0) {
                                while (blogDetailsList.size() > 60) {
                                    AppController.getInstance().getRequestQueue().getCache()
                                            .remove(blogDetailsList.get(0).getBlogUrlToImage());
                                    blogDetailsList.remove(0);
                                    fVItem--;

                                }
                                pageUp++;
                            }
                            if (scrollingUp && blogDetailsList.size() > 60) {
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

                            Log.d("JSON", "prazan string");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Log.d("JSON", error.getMessage());
            }
        }) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                mStatusCode = response.statusCode;
                responseHeaders = response.headers;
                Log.d("STATUS CODE", String.valueOf(mStatusCode));
                return super.parseNetworkResponse(response);
            }

        };
        int retries = 2;
        getRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                retries, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(getRequest);

        return scrollFlag;
    }
}
