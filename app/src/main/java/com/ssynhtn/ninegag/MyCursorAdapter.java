package com.ssynhtn.ninegag;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.ssynhtn.ninegag.data.GagItem;
import com.ssynhtn.ninegag.view.SquareImageView;
import com.ssynhtn.ninegag.volley.VolleySingleton;

/**
 * Created by ssynhtn on 11/20/2014.
 */
public class MyCursorAdapter extends CursorAdapter {

    private Bitmap mPlaceholderBitmap;
    private Bitmap mErrorBitmap;

    public MyCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mPlaceholderBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
        mErrorBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.empty_photo);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.grid_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(final View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        GagItem item = GagItem.extractFromCursor(cursor);

        if(holder.mImageContainer != null){
            if(holder.mImageContainer.getRequestUrl().equals(item.getImageUrlNormal())){
                return; // if the image request in image container is just the one we are going to request, then just return
            } else {
                holder.mImageContainer.cancelRequest();
                holder.mImageContainer = null;
            }
        }

        holder.textView.setText(item.getCaption());
        holder.mImageContainer = VolleySingleton.getInstance(context).getImageLoader()
                .get(item.getImageUrlNormal(), new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                        Bitmap bitmap = null;
                        if((bitmap = imageContainer.getBitmap()) == null){
                            bitmap = mPlaceholderBitmap;
                        }
                        holder.imageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        holder.imageView.setImageBitmap(mErrorBitmap);
                    }
                });
        holder.mGagItem = GagItem.extractFromCursor(cursor);
    }


    public static class ViewHolder {
        SquareImageView imageView;
        TextView textView;
        ImageLoader.ImageContainer mImageContainer;
        GagItem mGagItem;

        public ViewHolder(View view){
            imageView = (SquareImageView) view.findViewById(R.id.square_image_view);
            textView = (TextView) view.findViewById(R.id.caption_text_view);
        }

    }
}
