package com.vladan.newsreader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by vladan on 12/25/2017
 */

public class BlogListAdapter extends RecyclerView.Adapter<BlogListAdapter.ViewHolder> {

    private List<BlogDetails> blogDetailses;
    private ImageLoader imageLoader;
    private OnItemClickListener itemClickListener;


    public BlogListAdapter(List<BlogDetails> blogDetailses) {
        this.blogDetailses = blogDetailses;

        imageLoader = AppController.getInstance().getImageLoader();
    }

    @NonNull
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
        holder.publishedAt.setText(convertDateStringFormat(blogDetailses.get(position).getPublishedAt()));
        String imageUrl = blogDetailses.get(position).getBlogUrlToImage();
        if (!imageUrl.equals(null)) {
            holder.thumbnail.setImageUrl(imageUrl, imageLoader);
        }
    }

    @Override
    public int getItemCount() {
        return blogDetailses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, description, publishedAt;
        NetworkImageView thumbnail;
        private int MIN_HIGH = 500;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title_blog);
            description = itemView.findViewById(R.id.description_blog);
            publishedAt = itemView.findViewById(R.id.published_at_blog);
            thumbnail = itemView.findViewById(R.id.image_blog);
            thumbnail.setMinimumHeight(MIN_HIGH);

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

    public String convertDateStringFormat(String strDate) {
        Calendar calendar = Calendar.getInstance();

        TimeZone timeZone = calendar.getTimeZone();
        String fromFormat = "yyyy-MM-dd'T'hh:mm:ss";
        String toFormat = "yyy-MM-dd" + "\t\t" + "hh:mm:ss aaa ";

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fromFormat);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC + 0"));

            SimpleDateFormat dateFormatLocale = new SimpleDateFormat(toFormat.trim());
            dateFormatLocale.setTimeZone(timeZone);

            return dateFormatLocale.format(simpleDateFormat.parse(strDate));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
