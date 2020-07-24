package com.vladan.newsreader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    public void onAttach(@NonNull Context context) {
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

        SeparatorDecoration itemDecoration=new SeparatorDecoration(recyclerView.getContext(), Color.parseColor("#008641"),1.5f);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter.SetOnItemClickListener(new SourceListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                String sourceId = sourceDetailses.get(position).getId();
                String language=sourceDetailses.get(position).getLanguage();
                String name=sourceDetailses.get(position).getName();
                onItemClicked(sourceId,language,name);
               // Toast.makeText(activity, sourceId, Toast.LENGTH_LONG).show();
                onDestroy();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshData(url);

    }

    public void onItemClicked(String source,String language,String name) {
        if (listener != null) {
            listener.onFragmentInteraction(source,language,name);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(String source,String language,String name);
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
