package com.vladan.newsreader;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SourceListFragment extends Fragment {

    private OnFragmentInteractionListener listener;
    FragmentActivity activity;
    RecyclerView recyclerView;
    SourceListAdapter adapter;
    List<SourceDetails> sourceDetailses = new ArrayList<>();
    String url;

    public SourceListFragment() {

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (FragmentActivity) context;
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    public static SourceListFragment newInstance() {
        SourceListFragment sourceListFragment = new SourceListFragment();

        return sourceListFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getResources();
        url = res.getString(R.string.host) + "sources?apiKey=c540ba5d76254cadb8261a1a2fac4342";
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_source_list, container, false);
        recyclerView = rootView.findViewById(R.id.source_list_recycler);
        adapter = new SourceListAdapter(activity, sourceDetailses);
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
        adapter.SetOnItemClickListener(new SourceListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                String sourceId = sourceDetailses.get(position).getId();
                onItemClicked(sourceId);
                Toast.makeText(activity, sourceId, Toast.LENGTH_LONG).show();
                onDestroy();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshData(url);

    }

    public void onItemClicked(String source) {
        if (listener != null) {
            listener.onFragmentInteraction(source);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }


    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(String source);
    }

    public void refreshData(String url) {
        RequestQueue queue = Volley.newRequestQueue(activity);

        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.optString("status");
                            if (status.equals("ok")) {
                                JSONArray sources = (JSONArray) jsonObject.opt("sources");
                                for (int i = 1; i < sources.length(); i++) {
                                    SourceDetails sourceDetails = new SourceDetails();
                                    JSONObject source = sources.getJSONObject(i);
                                    sourceDetails.setId(source.optString("id"));
                                    sourceDetails.setName(source.optString("name"));
                                    sourceDetails.setDescription(source.optString("description"));
                                    sourceDetails.setLanguage(source.optString("language"));
                                    sourceDetailses.add(sourceDetails);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {

        };
        queue.add(getRequest);
    }
}
