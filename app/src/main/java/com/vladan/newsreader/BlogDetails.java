package com.vladan.newsreader;

/**
 * Created by vladan on 12/25/2017
 */

public class BlogDetails {

  private  String blogTitle, blogDescription, blogUrl, blogUrlToImage, publishedAt;

    public BlogDetails(){

    }

    public BlogDetails(String blogTitle,String blogDescription,String blogUrl,
                       String blogUrlToImage,String publishedAt){

        this.blogTitle=blogTitle;
        this.blogDescription=blogDescription;
        this.blogUrl=blogUrl;
        this.blogUrlToImage=blogUrlToImage;
        this.publishedAt=publishedAt;

    }

    public String getBlogTitle() {
        return blogTitle;
    }

    public void setBlogTitle(String blogTitle) {
        this.blogTitle = blogTitle;
    }

    public String getBlogDescription() {
        return blogDescription;
    }

    public void setBlogDescription(String blogDescription) {
        this.blogDescription = blogDescription;
    }

    public String getBlogUrl() {
        return blogUrl;
    }

    public void setBlogUrl(String blogUrl) {
        this.blogUrl = blogUrl;
    }

    public String getBlogUrlToImage() {
        return blogUrlToImage;
    }

    public void setBlogUrlToImage(String blogUrlToImage) {
        this.blogUrlToImage = blogUrlToImage;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }
}
