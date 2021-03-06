package com.yeejay.yplay.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yeejay.yplay.R;
import com.yeejay.yplay.model.GetAddFriendMsgs;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 添加朋友列表适配器
 * Created by Administrator on 2017/10/27.
 */

public class AddFriendsAdapter extends BaseAdapter implements View.OnClickListener {

    private Context context;
    private hideCallback hideCallback;
    private acceptCallback acceptCallback;
    List<GetAddFriendMsgs.PayloadBean.MsgsBean> contentList;

    View.OnClickListener hideListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideCallback.hideClick(v);
        }
    };
    View.OnClickListener acceptListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            acceptCallback.acceptClick(v);
        }
    };

    public interface hideCallback {
        void hideClick(View v);
    }

    public interface acceptCallback {
        void acceptClick(View v);
    }

    public AddFriendsAdapter(Context context,
                             hideCallback hideCallback,
                             acceptCallback acceptCallback,
                             List<GetAddFriendMsgs.PayloadBean.MsgsBean> list) {
        this.hideCallback = hideCallback;
        this.acceptCallback = acceptCallback;
        this.context = context;
        this.contentList = list;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public int getCount() {
        return contentList.size();
    }

    @Override
    public Object getItem(int position) {
        return contentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_add_friends, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        String url = contentList.get(position).getFromHeadImgUrl();
        if (!TextUtils.isEmpty(url)){
            Picasso.with(context).load(url).resize(40,40).into(holder.afItemHeaderImg);
        }
        holder.afItemName.setText(contentList.get(position).getFromNickName());
        holder.afBtnHide.setOnClickListener(hideListener);
        holder.afBtnHide.setTag(position);
        holder.afBtnHide.setVisibility(View.VISIBLE);

        System.out.println("姓名---" + contentList.get(position).getFromNickName() +
        ",状态---" + contentList.get(position).getStatus());


        if (contentList.get(position).getStatus() == 0){
            holder.afBtnAccept.setText("接受");
            holder.afBtnAccept.setEnabled(true);
            holder.afBtnAccept.setOnClickListener(acceptListener);
        }else if (contentList.get(position).getStatus() == 1){
            holder.afBtnAccept.setText("已添加");
            holder.afBtnAccept.setEnabled(false);
        }
        holder.afBtnAccept.setTag(position);
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.af_item_header_img)
        ImageView afItemHeaderImg;
        @BindView(R.id.af_item_name)
        TextView afItemName;
        @BindView(R.id.af_item_tv_shares_friends)
        TextView afItemTvSharesFriends;
        @BindView(R.id.af_btn_accept)
        Button afBtnAccept;
        @BindView(R.id.af_btn_hide)
        Button afBtnHide;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
