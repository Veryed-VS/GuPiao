package com.xjm.gupiao;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import java.util.ArrayList;

public class SharesAdapter extends BaseAdapter {
    private ArrayList<AllSharesBean> resultBeans;
    private RequestOptions options = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true);
    private Context context;

    public SharesAdapter(ArrayList<AllSharesBean> resultBeans, Context context) {
        this.resultBeans = resultBeans;
        this.context = context;
    }

    @Override
    public int getCount() {
        return resultBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return resultBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.shares_list_item, null);
            holder = new Holder();
            holder.dayImageView = convertView.findViewById(R.id.day_imageView);
            holder.minImageView = convertView.findViewById(R.id.min_imageView);
            holder.nameTextView = convertView.findViewById(R.id.name_textView);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        AllSharesBean resultBean = resultBeans.get(position);
        String dayUrl = DataTools.min_image_url
                + (resultBean.getCode().startsWith("0") ? "sz" : "sh")
                + resultBean.getCode() + ".gif";
        String minUrl = DataTools.image_url
                + (resultBean.getCode().startsWith("0") ? "sz" : "sh")
                + resultBean.getCode() + ".gif";
        //日K线
        Glide.with(context)
                .asGif()
                .load(Uri.parse(dayUrl))
                .apply(options)
                .into(holder.dayImageView);
        //分时线
        Glide.with(context)
                .asGif()
                .load(Uri.parse(minUrl))
                .apply(options)
                .into(holder.minImageView);
        holder.nameTextView.setText((position+1)+"."+resultBean.getName() + "(" + resultBean.getCode() + ")");
        return convertView;
    }

    private class Holder {
        private ImageView dayImageView;
        private ImageView minImageView;
        private TextView nameTextView;
    }
}
