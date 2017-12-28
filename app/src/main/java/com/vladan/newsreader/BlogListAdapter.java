package com.vladan.newsreader;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

/**
 * Created by vladan on 12/25/2017
 */

public class BlogListAdapter extends RecyclerView.Adapter<BlogListAdapter.ViewHolder> {

    private final FragmentActivity activity;
    private List<BlogDetails> blogDetailses;
    private ImageLoader imageLoader;
    private OnItemClickListener itemClickListener;


    public BlogListAdapter(FragmentActivity activity,List<BlogDetails> blogDetailses){
        this.activity=activity;
        this.blogDetailses=blogDetailses;

        imageLoader = AppController.getInstance().getImageLoader();
    }

    @Override
    public BlogListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        final View view = mInflater.inflate(R.layout.blog_list_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.title.setText(blogDetailses.get(position).getBlogTitle());
        holder.description.setText(blogDetailses.get(position).getBlogDescription());
        holder.publishedAt.setText(blogDetailses.get(position).getPublishedAt());
        holder.thumbnail.setImageUrl(blogDetailses.get(position).getBlogUrlToImage(), imageLoader);
    }

    @Override
    public int getItemCount() {
        return blogDetailses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, description, publishedAt;
        NetworkImageView thumbnail;
        public ViewHolder(View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.title_blog);
            description=itemView.findViewById(R.id.description_blog);
            publishedAt=itemView.findViewById(R.id.published_at_blog);
            thumbnail=itemView.findViewById(R.id.image_blog);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
           if (itemClickListener!=null){
               itemClickListener.onItemClick(view,getAdapterPosition());
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
