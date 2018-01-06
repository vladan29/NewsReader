package com.vladan.newsreader;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by vladan on 12/30/2017
 */

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<MainActivity.CategoryObject> category;
    private OnItemClickListener itemClickListener;
    public static int clickPosition = 0;
    public static int previousClickPosition = 0;


    public CategoryAdapter(List<MainActivity.CategoryObject> category) {
        this.category = category;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        final View view = mInflater.inflate(R.layout.category_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.label.setText(category.get(position).getCategory());
        if (position == clickPosition) {
           // holder.categoryDivider.setBackgroundColor(Color.RED);
            holder.label.setTextColor(Color.parseColor("#de000000"));
        } else {
            //holder.categoryDivider.setBackgroundColor(Color.BLUE);
            holder.label.setTextColor(Color.parseColor("#607d8b"));
        }

    }

    @Override
    public int getItemCount() {
        return category.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView label, categoryDivider;

        public ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.category_label);
            categoryDivider = itemView.findViewById(R.id.category_divider);
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

    public void SetOnItemClickListener(final CategoryAdapter.OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

}
