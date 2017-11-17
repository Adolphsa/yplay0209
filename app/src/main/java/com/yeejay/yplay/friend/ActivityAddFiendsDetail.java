package com.yeejay.yplay.friend;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.jwenfeng.library.pulltorefresh.BaseRefreshListener;
import com.jwenfeng.library.pulltorefresh.PullToRefreshLayout;
import com.yeejay.yplay.R;
import com.yeejay.yplay.adapter.FriendsDetailAdapter;
import com.yeejay.yplay.api.YPlayApiManger;
import com.yeejay.yplay.model.BaseRespond;
import com.yeejay.yplay.model.GetAddFriendMsgs;
import com.yeejay.yplay.utils.SharePreferenceUtil;
import com.yeejay.yplay.utils.StatuBarUtil;
import com.yeejay.yplay.utils.YPlayConstant;

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

public class ActivityAddFiendsDetail extends AppCompatActivity {

    @BindView(R.id.layout_title_back2)
    ImageButton layoutTitleBack;
    @BindView(R.id.layout_title2)
    TextView layoutTitle;
    @BindView(R.id.aafd_list_view)
    ListView aafdListView;
    @BindView(R.id.aafd_ptf_refresh)
    PullToRefreshLayout aafdPtfRefresh;

    @OnClick(R.id.layout_title_back2)
    public void back(View view) {
        finish();
    }

    FriendsDetailAdapter friendsDetailAdapter;
    int mPageNum = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activty_add_fiends_detail);
        ButterKnife.bind(this);

        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        StatuBarUtil.setMiuiStatusBarDarkMode(ActivityAddFiendsDetail.this, true);

        layoutTitle.setText("好友请求");

        getAddFriendmsgs(mPageNum);
        loadMore();
    }

    private void initFriendsDetailListView(final List<GetAddFriendMsgs.PayloadBean.MsgsBean> tempList) {
        friendsDetailAdapter = new FriendsDetailAdapter(ActivityAddFiendsDetail.this,
                new FriendsDetailAdapter.hideCallback() {
                    @Override
                    public void hideClick(View v) {
                        System.out.println("隐藏按钮被点击");
                        Button button = (Button) v;
                        accepeAddFreind(tempList.get((int) button.getTag()).getMsgId(),1);
                        button.setVisibility(View.INVISIBLE);
                        if (tempList.size() > 0) {
                            System.out.println("tempList---" + tempList.size() + "----" + (int) v.getTag());
                            tempList.remove((int) v.getTag());
                            friendsDetailAdapter.notifyDataSetChanged();
                        }

                    }
                }, new FriendsDetailAdapter.acceptCallback() {
            @Override
            public void acceptClick(View v) {
                System.out.println("接受按钮被点击");
                Button button = (Button) v;
                button.setBackgroundResource(R.drawable.green_accept);
                button.setEnabled(false);
                //接受加好友的请求
                accepeAddFreind(tempList.get((int) button.getTag()).getMsgId(),0);
            }
        }, tempList);
        aafdListView.setAdapter(friendsDetailAdapter);
    }

    private void loadMore(){
        aafdPtfRefresh.setCanRefresh(false);
        aafdPtfRefresh.setRefreshListener(new BaseRefreshListener() {
            @Override
            public void refresh() {}

            @Override
            public void loadMore() {
                mPageNum++;
                getAddFriendmsgs(mPageNum);
            }
        });
    }

    //拉取添加好友消息
    private void getAddFriendmsgs(int pageNum) {

        Map<String, Object> getAddFriendmsgsMap = new HashMap<>();
        getAddFriendmsgsMap.put("updateLastReadMsgId", 0);
        getAddFriendmsgsMap.put("pageNum",pageNum);
        getAddFriendmsgsMap.put("uin", SharePreferenceUtil.get(ActivityAddFiendsDetail.this, YPlayConstant.YPLAY_UIN, 0));
        getAddFriendmsgsMap.put("token", SharePreferenceUtil.get(ActivityAddFiendsDetail.this, YPlayConstant.YPLAY_TOKEN, "yplay"));
        getAddFriendmsgsMap.put("ver", SharePreferenceUtil.get(ActivityAddFiendsDetail.this, YPlayConstant.YPLAY_VER, 0));
        YPlayApiManger.getInstance().getZivApiService()
                .getAddFriendMsg(getAddFriendmsgsMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GetAddFriendMsgs>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull GetAddFriendMsgs getAddFriendMsgs) {
                        System.out.println("拉取添加好友消息---" + getAddFriendMsgs.toString());
                        if (getAddFriendMsgs.getCode() == 0) {
                            List<GetAddFriendMsgs.PayloadBean.MsgsBean> tempList
                                    = getAddFriendMsgs.getPayload().getMsgs();
                            initFriendsDetailListView(tempList);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        System.out.println("拉取添加好友消息异常---" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    //接受好友请求
    private void accepeAddFreind(int msgId, int act) {
        Map<String, Object> accepeAddFreindMap = new HashMap<>();
        accepeAddFreindMap.put("msgId", msgId);
        accepeAddFreindMap.put("act",act);
        accepeAddFreindMap.put("uin", SharePreferenceUtil.get(ActivityAddFiendsDetail.this, YPlayConstant.YPLAY_UIN, 0));
        accepeAddFreindMap.put("token", SharePreferenceUtil.get(ActivityAddFiendsDetail.this, YPlayConstant.YPLAY_TOKEN, "yplay"));
        accepeAddFreindMap.put("ver", SharePreferenceUtil.get(ActivityAddFiendsDetail.this, YPlayConstant.YPLAY_VER, 0));
        YPlayApiManger.getInstance().getZivApiService()
                .acceptAddFriend(accepeAddFreindMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseRespond>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull BaseRespond baseRespond) {
                        System.out.println("接受好友请求---" + baseRespond.toString());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        System.out.println("接受好友请求异常---" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    //删除好友
    private void removeFriend(int toUin) {

        Map<String, Object> removeFreindMap = new HashMap<>();
        removeFreindMap.put("toUin", toUin);
        removeFreindMap.put("uin", SharePreferenceUtil.get(ActivityAddFiendsDetail.this, YPlayConstant.YPLAY_UIN, 0));
        removeFreindMap.put("token", SharePreferenceUtil.get(ActivityAddFiendsDetail.this, YPlayConstant.YPLAY_TOKEN, "yplay"));
        removeFreindMap.put("ver", SharePreferenceUtil.get(ActivityAddFiendsDetail.this, YPlayConstant.YPLAY_VER, 0));
        YPlayApiManger.getInstance().getZivApiService()
                .removeFriend(removeFreindMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseRespond>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull BaseRespond baseRespond) {
                        System.out.println("删除好友---" + baseRespond.toString());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        System.out.println("删除好友异常---" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}
