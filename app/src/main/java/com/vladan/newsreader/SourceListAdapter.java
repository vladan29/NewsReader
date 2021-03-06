package com.vladan.newsreader;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by vladan on 12/24/2017
 */

public class SourceListAdapter extends RecyclerView.Adapter<SourceListAdapter.ViewHolder> {

    private final FragmentActivity activity;
    private List<SourceDetails> sourcesDetailses;
    private OnItemClickListener itemClickListener;

    public SourceListAdapter(FragmentActivity activity, List<SourceDetails> sourcesDetailses) {
        this.activity = activity;
        this.sourcesDetailses = sourcesDetailses;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        final View view = mInflater.inflate(R.layout.source_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String tvNameText = "Name:" + sourcesDetailses.get(position).getName();
        String tvDescriptionText = "Description:" + sourcesDetailses.get(position).getDescription();
        String tvLanguageText = "Language:" + sourcesDetailses.get(position).getLanguage();
        holder.tvName.setText(tvNameText);
        holder.tvDescription.setText(tvDescriptionText);
        holder.tvLanguage.setText(tvLanguageText);
    }

    @Override
    public int getItemCount() {
        return sourcesDetailses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvName, tvDescription, tvLanguage;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.name_source);
            tvDescription = itemView.findViewById(R.id.description_source);
            tvLanguage = itemView.findViewById(R.id.language_source);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
