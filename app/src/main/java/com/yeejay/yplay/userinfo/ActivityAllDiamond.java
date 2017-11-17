package com.yeejay.yplay.userinfo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jwenfeng.library.pulltorefresh.BaseRefreshListener;
import com.jwenfeng.library.pulltorefresh.PullToRefreshLayout;
import com.squareup.picasso.Picasso;
import com.yeejay.yplay.R;
import com.yeejay.yplay.api.YPlayApiManger;
import com.yeejay.yplay.model.UsersDiamondInfoRespond;
import com.yeejay.yplay.utils.SharePreferenceUtil;
import com.yeejay.yplay.utils.StatuBarUtil;
import com.yeejay.yplay.utils.YPlayConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ActivityAllDiamond extends AppCompatActivity {

    @BindView(R.id.layout_title_back2)
    ImageButton layoutTitleBack;
    @BindView(R.id.layout_title2)
    TextView layoutTitle;
    @BindView(R.id.aad_list_view)
    ListView aadListView;
    @BindView(R.id.aad_ptf_refresh)
    PullToRefreshLayout aadPtfRefresh;

    @OnClick(R.id.layout_title_back2)
    public void back(View view) {
        finish();
    }

    List<UsersDiamondInfoRespond.PayloadBean.StatsBean> mDataList;

    int mPageNum = 1;
    int uin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_diamond);
        ButterKnife.bind(this);

        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        StatuBarUtil.setMiuiStatusBarDarkMode(ActivityAllDiamond.this, true);

        layoutTitle.setText("所有钻石");
        mDataList = new ArrayList<>();
        uin = (int)SharePreferenceUtil.get(ActivityAllDiamond.this, YPlayConstant.YPLAY_UIN, 0);
        getUserDiamondInfo(uin,mPageNum,20);

    }

    private void initDiamondList(final List<UsersDiamondInfoRespond.PayloadBean.StatsBean> tempList){

        aadListView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return tempList.size();
            }

            @Override
            public Object getItem(int position) {
                return tempList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    convertView = View.inflate(ActivityAllDiamond.this, R.layout.item_afi, null);
                    holder = new ViewHolder();
                    holder.itemAmiIndex = (TextView) convertView.findViewById(R.id.afi_item_index);
                    holder.itemAmiImg = (ImageView) convertView.findViewById(R.id.afi_item_img);
                    holder.itemAmiText = (TextView) convertView.findViewById(R.id.afi_item_text);
                    holder.itemAmiCount = (TextView) convertView.findViewById(R.id.afi_item_count);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                UsersDiamondInfoRespond.PayloadBean.StatsBean statsBean = tempList.get(position);

                holder.itemAmiIndex.setText(String.valueOf(position+1));

                String url = statsBean.getQiconUrl();
                if (!TextUtils.isEmpty(url)){
                    Picasso.with(ActivityAllDiamond.this).load(url).into(holder.itemAmiImg);
                }else {
                    holder.itemAmiImg.setImageDrawable(getDrawable(R.drawable.diamond));
                }
                holder.itemAmiText.setText(statsBean.getQtext());

                holder.itemAmiCount.setText(String.valueOf(statsBean.getGemCnt()));
                return convertView;
            }
        });
        loadMore();
    }

    private void loadMore(){
        aadPtfRefresh.setCanRefresh(false);
        aadPtfRefresh.setRefreshListener(new BaseRefreshListener() {
            @Override
            public void refresh() {

            }

            @Override
            public void loadMore() {
                mPageNum++;
                System.out.println("mPageNum---" + mPageNum);
                getUserDiamondInfo(uin,mPageNum,20);
            }
        });
    }

    //获取钻石信息
    private void getUserDiamondInfo(int userUin,int pageNum, int pageSize){
        Map<String, Object> diamondInfoMap = new HashMap<>();
        diamondInfoMap.put("userUin",userUin);
        diamondInfoMap.put("pageNum",pageNum);
        diamondInfoMap.put("pageSize",pageSize);
        diamondInfoMap.put("uin", SharePreferenceUtil.get(ActivityAllDiamond.this, YPlayConstant.YPLAY_UIN, 0));
        diamondInfoMap.put("token", SharePreferenceUtil.get(ActivityAllDiamond.this, YPlayConstant.YPLAY_TOKEN, "yplay"));
        diamondInfoMap.put("ver", SharePreferenceUtil.get(ActivityAllDiamond.this, YPlayConstant.YPLAY_VER, 0));
        YPlayApiManger.getInstance().getZivApiService()
                .getUsersDamonInfo(diamondInfoMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UsersDiamondInfoRespond>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull UsersDiamondInfoRespond usersDiamondInfoRespond) {
                        System.out.println("获取用户钻石信息---" + usersDiamondInfoRespond.toString());
                        if (usersDiamondInfoRespond.getCode() == 0){
                            mDataList.addAll(usersDiamondInfoRespond.getPayload().getStats());
                            if (mDataList.size() > 0){
                                int total = usersDiamondInfoRespond.getPayload().getTotal();
                                layoutTitle.setText("所有钻石" + total);
                                initDiamondList(mDataList);
                            }

                        }
                        aadPtfRefresh.finishLoadMore();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        System.out.println("获取用户钻石信息异常---" + e.getMessage());
                        aadPtfRefresh.finishLoadMore();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private static class ViewHolder {
        TextView itemAmiIndex;
        ImageView itemAmiImg;
        TextView itemAmiText;
        TextView itemAmiCount;
    }
}
