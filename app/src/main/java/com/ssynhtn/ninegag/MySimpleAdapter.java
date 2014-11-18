package com.ssynhtn.ninegag;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.ssynhtn.ninegag.data.GagItem;
import com.ssynhtn.ninegag.view.SquareImageView;
import com.ssynhtn.ninegag.volley.VolleySingleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ssynhtn on 11/15/2014.
 */
public class MySimpleAdapter extends BaseAdapter {

    private Context context;
    private Resources mResources;
    private List<GagItem> data;
    private Bitmap placeholderBitmap;
    private Bitmap errorBitmap;

    public MySimpleAdapter(Context context) {
        super();
        this.context = context;
        mResources = context.getResources();
        data = new ArrayList<GagItem>();
        data.add(new GagItem("-1", "test caption", "http://testbadurl/", "http://testbadurl/"));
        placeholderBitmap = BitmapFactory.decodeResource(mResources, R.drawable.ic_launcher);
        errorBitmap = BitmapFactory.decodeResource(mResources, R.drawable.empty_photo);
    }

    public MySimpleAdapter(Context context, List<GagItem> data){
        this(context);
        this.data.addAll(data);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public GagItem getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String caption = getItem(position).getCaption();
        String url = getItem(position).getImageUrlNormal();

        final ViewHolder holder;
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            if(holder.mImageContainer != null){
                if(holder.mImageContainer.getRequestUrl().equals(url)){ // if this view is already requesting same url, just return it
                    return convertView;
                }

                // else cancel this request
                holder.mImageContainer.cancelRequest();
                holder.mImageContainer = null;
            }
        }



        holder.textView.setText(caption);
//        holder.imageView.setImageBitmap(placeholderBitmap);
        holder.mImageContainer = VolleySingleton.getInstance(context).getImageLoader().get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                Bitmap bitmap = imageContainer.getBitmap();
                if(bitmap == null){
                    holder.imageView.setImageBitmap(placeholderBitmap);
                } else {
                    holder.imageView.setImageBitmap(bitmap);
                }

            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                    holder.imageView.setImageBitmap(errorBitmap);
            }
        });

        return convertView;
    }

    public static class ViewHolder {
        SquareImageView imageView;
        TextView textView;
        ImageLoader.ImageContainer mImageContainer;

        public ViewHolder(View view){
            imageView = (SquareImageView) view.findViewById(R.id.square_image_view);
            textView = (TextView) view.findViewById(R.id.caption_text_view);
        }

    }

    public void addAll(Collection<GagItem> items){
        data.addAll(items);
        notifyDataSetChanged();
    }


}
