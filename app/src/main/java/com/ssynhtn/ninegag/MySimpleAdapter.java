package com.ssynhtn.ninegag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ssynhtn.ninegag.data.GagItem;
import com.ssynhtn.ninegag.view.SquareImageView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ssynhtn on 11/15/2014.
 */
public class MySimpleAdapter extends BaseAdapter {

    private Context context;
    private List<GagItem> data;
    private Bitmap placeholderBitmap;

    public MySimpleAdapter(Context context) {
        super();
        this.context = context;
        data = new ArrayList<GagItem>();
        placeholderBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
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

        ViewHolder holder;
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String caption = getItem(position).getCaption();
        holder.textView.setText(caption);
        holder.imageView.setImageBitmap(placeholderBitmap);
        return convertView;
    }

    public static class ViewHolder {
        SquareImageView imageView;
        TextView textView;

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
