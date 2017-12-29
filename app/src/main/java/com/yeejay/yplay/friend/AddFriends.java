package com.yeejay.yplay.friend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jwenfeng.library.pulltorefresh.BaseRefreshListener;
import com.jwenfeng.library.pulltorefresh.PullToRefreshLayout;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;
import com.yeejay.yplay.MainActivity;
import com.yeejay.yplay.R;
import com.yeejay.yplay.YplayApplication;
import com.yeejay.yplay.adapter.ContactsAdapter;
import com.yeejay.yplay.adapter.SchoolmateAdapter;
import com.yeejay.yplay.answer.ActivityInviteFriend;
import com.yeejay.yplay.api.YPlayApiManger;
import com.yeejay.yplay.base.BaseActivity;
import com.yeejay.yplay.customview.CardBigDialog;
import com.yeejay.yplay.customview.MesureListView;
import com.yeejay.yplay.customview.SpinerPopWindow;
import com.yeejay.yplay.greendao.ContactsInfo;
import com.yeejay.yplay.greendao.ContactsInfoDao;
import com.yeejay.yplay.model.AddFriendRespond;
import com.yeejay.yplay.model.GetRecommendsRespond;
import com.yeejay.yplay.model.UserInfoResponde;
import com.yeejay.yplay.utils.NetWorkUtil;
import com.yeejay.yplay.utils.SharePreferenceUtil;
import com.yeejay.yplay.utils.StatuBarUtil;
import com.yeejay.yplay.utils.YPlayConstant;

import java.util.ArrayList;
import java.util.Arrays;
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

public class AddFriends extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "AddFriends";
    private static final int REQUEST_CODE_PERMISSION_SINGLE_CONTACTS = 101;

    private static final int CLASSMATE_TYPE_ALL = 0;
    private static final int CLASSMATE_MALE = 1;
    private static final int CLASSMATE_FEMALE = 2;
    private static final int CLASSMATE_SAME_GRADE = 3;
    private static final String KEY_SHARED_PREFERENCE = "preferences_class_type";

    @BindView(R.id.af_btn_add_contacts)
    ImageButton btnAddContacts;
    @BindView(R.id.af_btn_add_schoolmate)
    ImageButton btnAddSchollMate;
    @BindView(R.id.af_btn_wait_invite)
    ImageButton btnAddWaitInvite;
    @BindView(R.id.layout_title_back2)
    ImageButton layoutTitleBack;
    @BindView(R.id.layout_title2)
    TextView layoutTitle;
    @BindView(R.id.searchView)
    TextView searchView;

    @BindView(R.id.friend_pll_refresh)
    PullToRefreshLayout pullToRefreshLayout;
    @BindView(R.id.filter_text)
    TextView filterText;

    @OnClick(R.id.filter_text)
    public void filterClick() {
        Drawable drawable = getResources().getDrawable(R.drawable.spinner_down);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(),drawable.getMinimumHeight());// 必须设置图片大小，否则不显示
        filterText.setCompoundDrawables(null, null, drawable, null);

        mSpinerPopWindow.setWidth(filterText.getWidth());
        mSpinerPopWindow.showAsDropDown(filterText);
    }

    @OnClick(R.id.layout_title_back2)
    public void back(View view) {

        if (isFromAddFriend){
            startActivity(new Intent(AddFriends.this, MainActivity.class));
        }

        finish();
    }

    @OnClick(R.id.searchView)
    public void clickSearch(View view) {
        startActivity(new Intent(AddFriends.this, ActivitySearchFriends.class));
    }

    @OnClick(R.id.af_btn_add_contacts)
    public void btnAddContacts() {
        btnAddContacts.setImageResource(R.drawable.add_friends_contacts_icon_selected);
        btnAddSchollMate.setImageResource(R.drawable.add_friends_classmates_icon_unselected);
        btnAddWaitInvite.setImageResource(R.drawable.add_friends__wait_invite_icon_unselected);

        //通讯录好友
        System.out.println("通讯录好友");

        if (contactRoot.isShown()) {
            contactRoot.setEnabled(false);
            schoolRoot.setEnabled(true);
            maybeRoot.setEnabled(true);
            return;
        }

        mPageNum = 1;
        mType = 1;

        contactRoot.setVisibility(View.VISIBLE);
        schoolRoot.setVisibility(View.GONE);
        maybeRoot.setVisibility(View.GONE);

        positionList.clear();
        contactDredgeList.clear();

        if (contactRoot != null && contactRoot.isShown()) {
            getRecommends(1, mPageNum);
        }

    }

    @OnClick(R.id.af_btn_add_schoolmate)
    public void btnAddSchool() {
        btnAddContacts.setImageResource(R.drawable.add_friends_contacts_icon_unselected);
        btnAddSchollMate.setImageResource(R.drawable.add_friends_classmates_icon_selected);
        btnAddWaitInvite.setImageResource(R.drawable.add_friends__wait_invite_icon_unselected);

        //同校好友
        System.out.println("同校好友");

        if (schoolRoot.isShown()) {
            contactRoot.setEnabled(true);
            schoolRoot.setEnabled(false);
            maybeRoot.setEnabled(false);
            return;
        }

        mPageNum = 1;
        mType = 3;

        contactRoot.setVisibility(View.GONE);
        schoolRoot.setVisibility(View.VISIBLE);
        maybeRoot.setVisibility(View.GONE);

        positionList.clear();
        allSchoolMateList.clear();

        if (schoolRoot != null && schoolRoot.isShown()) {
            getRecommends(3, mPageNum);
        }

    }

    @OnClick(R.id.af_btn_wait_invite)
    public void btnWaitInvite() {
        btnAddContacts.setImageResource(R.drawable.add_friends_contacts_icon_unselected);
        btnAddSchollMate.setImageResource(R.drawable.add_friends_classmates_icon_unselected);
        btnAddWaitInvite.setImageResource(R.drawable.add_friends__wait_invite_icon_selected);

        //可能认识的人
        System.out.println("可能认识的人");

        if (maybeRoot.isShown()) {
            contactRoot.setEnabled(true);
            schoolRoot.setEnabled(true);
            maybeRoot.setEnabled(false);
            return;
        }

        mPageNum = 1;
        mType = 7;

        contactRoot.setVisibility(View.GONE);
        schoolRoot.setVisibility(View.GONE);
        maybeRoot.setVisibility(View.VISIBLE);

        positionList.clear();
        maybeKnowList.clear();

        if (maybeRoot != null && maybeRoot.isShown()) {
            getRecommends(7, mPageNum);
        }
    }

    LinearLayout contactRoot; //通讯录好友
    LinearLayout schoolRoot;    //同校同学
    LinearLayout maybeRoot;     //可能认识的人

    List<GetRecommendsRespond.PayloadBean.FriendsBean> contactDredgeList;
    List<GetRecommendsRespond.PayloadBean.FriendsBean> allSchoolMateList;   //全部
    List<GetRecommendsRespond.PayloadBean.FriendsBean> sameGradeList;       //同年级
    List<GetRecommendsRespond.PayloadBean.FriendsBean> boyList;             //男
    List<GetRecommendsRespond.PayloadBean.FriendsBean> girlList;            //女
    List<GetRecommendsRespond.PayloadBean.FriendsBean> maybeKnowList;       //可能认识的人

    int mPageNum = 1;

    int mType = 1; //好友类型
    int buttonDirt = 1; //学校按钮朝向
    boolean isFromAddFriend;

    boolean numberBookAuthoritySuccess = false;
    ContactsInfoDao contactsInfoDao;
    ContactsAdapter contactsAdapter;
    SchoolmateAdapter schoolmateAdapter;
    List<Integer> positionList;

    private SpinerPopWindow<String> mSpinerPopWindow;
    private List<String> typeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);
        ButterKnife.bind(this);

        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        StatuBarUtil.setMiuiStatusBarDarkMode(AddFriends.this, true);
        layoutTitle.setText("添加好友");

        contactsInfoDao = YplayApplication.getInstance().getDaoSession().getContactsInfoDao();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            isFromAddFriend = bundle.getBoolean("from_add_friend_guide");
        }

        contactRoot = (LinearLayout) findViewById(R.id.layout_contact);
        schoolRoot = (LinearLayout) findViewById(R.id.layout_school_mate);
        maybeRoot = (LinearLayout) findViewById(R.id.layout_maybe_know);

        contactDredgeList = new ArrayList<>();
        allSchoolMateList = new ArrayList<>();
        sameGradeList = new ArrayList<>();
        boyList = new ArrayList<>();
        girlList = new ArrayList<>();
        maybeKnowList = new ArrayList<>();
        positionList = new ArrayList();

        initPullRefresh();
        initClassmatesTypePop();

        getRecommends(1, 1);
    }

    private void initClassmatesTypePop() {
        final List<String> typeList = Arrays.asList(getResources().getStringArray(R.array.classmates_type));
        SharedPreferences sharedFilter = getSharedPreferences("preferences_class_filter",
                Context.MODE_PRIVATE);
        int selectOpn = sharedFilter.getInt("position", 0);
        filterText.setText(typeList.get(selectOpn));
        mSpinerPopWindow = new SpinerPopWindow<String>(this, this, typeList,
                new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sharedFilterItem = getSharedPreferences("preferences_class_filter",
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editorsettings = sharedFilterItem.edit();
                editorsettings.putInt("position", position);
                editorsettings.commit();

                filterText.setText(typeList.get(position));
                switch (position) {
                    case CLASSMATE_TYPE_ALL ://全部
                        mType = 3;
                        mPageNum = 1;
                        positionList.clear();
                        allSchoolMateList.clear();
                        getRecommends(3, mPageNum);
                        break;
                    case CLASSMATE_MALE ://男同学
                        mType = 5;
                        mPageNum = 1;
                        positionList.clear();
                        boyList.clear();
                        getRecommends(5, mPageNum);
                        break;
                    case CLASSMATE_FEMALE ://女同学
                        mType = 6;
                        mPageNum = 1;
                        positionList.clear();
                        girlList.clear();
                        getRecommends(6, mPageNum);
                        break;
                    case CLASSMATE_SAME_GRADE ://同年级
                        mType = 4;
                        mPageNum = 1;
                        positionList.clear();
                        sameGradeList.clear();
                        getRecommends(4, mPageNum);
                        break;
                    default :
                }

                mSpinerPopWindow.dismiss();
            }
        });
        mSpinerPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Drawable drawable = getResources().getDrawable(R.drawable.spinner_normal);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());// 必须设置图片大小，否则不显示
                filterText.setCompoundDrawables(null, null, drawable, null);
            }
        });
    }

    private void initPullRefresh() {

        pullToRefreshLayout.setCanRefresh(false);
        pullToRefreshLayout.setRefreshListener(new BaseRefreshListener() {
            @Override
            public void refresh() {
                //刷新
            }

            @Override
            public void loadMore() {
                //加载更多
                mPageNum++;
                Log.i(TAG, "loadMore: pageNum---" + mPageNum);

                if (mType == 1) {    //通讯录已开通
                    getRecommends(1, mPageNum);
                } else if (mType == 3) {    //同校所有
                    getRecommends(3, mPageNum);
                } else if (mType == 4) {    //同校同年级
                    getRecommends(4, mPageNum);
                } else if (mType == 5) {    //同校男生
                    getRecommends(5, mPageNum);
                } else if (mType == 6) {    //同校女生
                    getRecommends(6, mPageNum);
                } else if (mType == 7) {    //可能认识的人
                    getRecommends(7, mPageNum);
                }
            }
        });

    }

//    //接受好友请求
//    private void accepeAddFreind(int msgId) {
//        Map<String, Object> accepeAddFreindMap = new HashMap<>();
//        accepeAddFreindMap.put("msgId", msgId);
//        accepeAddFreindMap.put("uin", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_UIN, 0));
//        accepeAddFreindMap.put("token", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_TOKEN, "yplay"));
//        accepeAddFreindMap.put("ver", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_VER, 0));
//        YPlayApiManger.getInstance().getZivApiService()
//                .acceptAddFriend(accepeAddFreindMap)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<BaseRespond>() {
//                    @Override
//                    public void onSubscribe(@NonNull Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(@NonNull BaseRespond baseRespond) {
//                        System.out.println("接受好友请求---" + baseRespond.toString());
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//                        System.out.println("接受好友请求异常---" + e.getMessage());
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//    }

    //发送加好友的请求
    private void addFriend(int toUin,int srcType) {
        Map<String, Object> addFreindMap = new HashMap<>();
        addFreindMap.put("toUin", toUin);
        addFreindMap.put("srcType",srcType);
        addFreindMap.put("uin", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_UIN, 0));
        addFreindMap.put("token", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_TOKEN, "yplay"));
        addFreindMap.put("ver", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_VER, 0));
        YPlayApiManger.getInstance().getZivApiService()
                .addFriend(addFreindMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AddFriendRespond>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull AddFriendRespond addFriendRespond) {
                        System.out.println("发送加好友请求---" + addFriendRespond.toString());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        System.out.println("发送加好友请求异常---" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

//    //删除好友
//    private void removeFriend(int toUin) {
//
//        Map<String, Object> removeFreindMap = new HashMap<>();
//        removeFreindMap.put("toUin", toUin);
//        removeFreindMap.put("uin", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_UIN, 0));
//        removeFreindMap.put("token", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_TOKEN, "yplay"));
//        removeFreindMap.put("ver", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_VER, 0));
//        YPlayApiManger.getInstance().getZivApiService()
//                .removeFriend(removeFreindMap)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<BaseRespond>() {
//                    @Override
//                    public void onSubscribe(@NonNull Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(@NonNull BaseRespond baseRespond) {
//                        System.out.println("删除好友---" + baseRespond.toString());
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//                        System.out.println("删除好友异常---" + e.getMessage());
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//    }

//    //通过短信邀请好友
//    private void invitefriendsbysms(String friends) {
//        Map<String, Object> removeFreindMap = new HashMap<>();
//        removeFreindMap.put("friends", friends);
//        removeFreindMap.put("uin", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_UIN, 0));
//        removeFreindMap.put("token", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_TOKEN, "yplay"));
//        removeFreindMap.put("ver", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_VER, 0));
//        YPlayApiManger.getInstance().getZivApiService()
//                .removeFriend(removeFreindMap)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<BaseRespond>() {
//                    @Override
//                    public void onSubscribe(@NonNull Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(@NonNull BaseRespond baseRespond) {
//                        System.out.println("短信邀请好友---" + baseRespond.toString());
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//                        System.out.println("短信邀请好友异常---" + e.getMessage());
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//    }

    //拉取同校/通讯录好友
    private void getRecommends(int type, int pageNum) {

        System.out.println("type---" + type);
        Map<String, Object> recommendsMap = new HashMap<>();
        recommendsMap.put("type", type);
        recommendsMap.put("pageNum", pageNum);
        recommendsMap.put("pageSize", 5);
        recommendsMap.put("uin", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_UIN, 0));
        recommendsMap.put("token", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_TOKEN, "yplay"));
        recommendsMap.put("ver", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_VER, 0));
        YPlayApiManger.getInstance().getZivApiService()
                .getSchoolmates(recommendsMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GetRecommendsRespond>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull GetRecommendsRespond getRecommendsRespond) {
                        if (getRecommendsRespond.getCode() == 0) {
                            System.out.println("好友列表---" + getRecommendsRespond.toString());
                            List<GetRecommendsRespond.PayloadBean.FriendsBean> friendsBeanList =
                                    getRecommendsRespond.getPayload().getFriends();
                            if (friendsBeanList != null) {

                                if (mType == 1) { //通讯录已开通
                                    contactDredgeList.addAll(friendsBeanList);
                                    handleContactDredge(contactDredgeList);
                                } else if (mType == 3) {  //全部
                                    allSchoolMateList.addAll(friendsBeanList);
                                    handleSchoolMate(allSchoolMateList);
                                } else if (mType == 4) {  //同年级
                                    sameGradeList.addAll(friendsBeanList);
                                    handleSchoolMate(sameGradeList);
                                } else if (mType == 5) {      //男
                                    boyList.addAll(friendsBeanList);
                                    handleSchoolMate(boyList);
                                } else if (mType == 6) {      //女
                                    girlList.addAll(friendsBeanList);
                                    handleSchoolMate(girlList);
                                } else if (mType == 7) {      //可能认识的人
                                    maybeKnowList.addAll(friendsBeanList);
                                    handleMaybeKnowFriend(maybeKnowList);
                                }

                            }

                        }
                        pullToRefreshLayout.finishLoadMore();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        System.out.println("拉取好友异常---" + e.getMessage());
                        Toast.makeText(AddFriends.this, "网络异常", Toast.LENGTH_SHORT).show();
                        pullToRefreshLayout.finishLoadMore();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        System.out.println("position---" + position);
        if (position == 4) {
            System.out.println("啦啦啦");
            startActivity(new Intent(AddFriends.this, ActivityAddFiendsDetail.class));
        }
    }

    //处理通讯录已开通
    private void handleContactDredge(final List<GetRecommendsRespond.PayloadBean.FriendsBean> friendsBeanList) {

        LinearLayout nullLl = (LinearLayout) contactRoot.findViewById(R.id.lcn_ll_null);
        MesureListView dredgeListView = (MesureListView) contactRoot.findViewById(R.id.lcn_dredge_list);
        RelativeLayout dredgeNoRl = (RelativeLayout) contactRoot.findViewById(R.id.lcn_rl);
        dredgeNoRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               getContacts();
               if (!numberBookAuthoritySuccess){    //如果没有权限  就申请权限
                   getNumberBookAuthority();
               }

            }
        });

        if (friendsBeanList.size() > 0) {

            nullLl.setVisibility(View.GONE);
            contactsAdapter = new ContactsAdapter(AddFriends.this,
                    new ContactsAdapter.hideCallback() {
                        @Override
                        public void hideClick(View v) {

                        }
                    },
                    new ContactsAdapter.acceptCallback() {
                        @Override
                        public void acceptClick(View v) {
                            if (NetWorkUtil.isNetWorkAvailable(AddFriends.this)) {
                                Button button = (Button) v;
                                button.setBackgroundResource(R.drawable.add_friend_apply);
                                button.setEnabled(false);

                                int position = (int) button.getTag();
                                positionList.add(position);
                                addFriend(friendsBeanList.get(position).getUin(),mType);
                            } else {
                                Toast.makeText(AddFriends.this, "网络异常", Toast.LENGTH_SHORT).show();
                            }


                        }
                    },
                    friendsBeanList,positionList);

            dredgeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int uin = friendsBeanList.get(position).getUin();
                    if (NetWorkUtil.isNetWorkAvailable(AddFriends.this)) {
                        getFriendInfo(uin, view);
                    } else {
                        Toast.makeText(AddFriends.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }

                }

            });

            dredgeListView.setAdapter(contactsAdapter);
        }
    }

    //处理同校同学
    private void handleSchoolMate(final List<GetRecommendsRespond.PayloadBean.FriendsBean> friendsBeanList) {
        final LinearLayout llButton = (LinearLayout) schoolRoot.findViewById(R.id.lsm_ll_button);
        LinearLayout llNullView = (LinearLayout) schoolRoot.findViewById(R.id.lsm_ll_null);
        final ImageButton allImgButton = (ImageButton) schoolRoot.findViewById(R.id.lsm_all);
        final ImageButton sameGradeButton = (ImageButton) schoolRoot.findViewById(R.id.lsm_class);
        final ImageButton boyButton = (ImageButton) schoolRoot.findViewById(R.id.lsm_boy);
        final ImageButton girlButton = (ImageButton) schoolRoot.findViewById(R.id.lsm_girl);
        MesureListView allSchoolmateListView = (MesureListView) schoolRoot.findViewById(R.id.lsm_list);

        if (friendsBeanList.size() > 0) {
            filterText.setVisibility(View.VISIBLE);
            llNullView.setVisibility(View.GONE);
        }

/*        arrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("箭头被点击");
                if (buttonDirt == 1) {
                    arrowButton.setImageResource(R.drawable.up_arrow);
                    buttonDirt = 2;
                    llButton.setVisibility(View.VISIBLE);
                } else if (buttonDirt == 2) {
                    arrowButton.setImageResource(R.drawable.down_arrow);
                    buttonDirt = 1;
                    llButton.setVisibility(View.GONE);
                }
            }
        });


        //全部
        allImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allImgButton.setImageResource(R.drawable.school_all_select);
                sameGradeButton.setImageResource(R.drawable.school_same_grade_unselect);
                boyButton.setImageResource(R.drawable.school_boy_unselect);
                girlButton.setImageResource(R.drawable.school_girl_unselect);

                mType = 3;
                mPageNum = 1;

                positionList.clear();
                allSchoolMateList.clear();
                getRecommends(3, mPageNum);
            }
        });

        //同年级
        sameGradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                allImgButton.setImageResource(R.drawable.school_all_unselect);
                sameGradeButton.setImageResource(R.drawable.school_same_grade_select);
                boyButton.setImageResource(R.drawable.school_boy_unselect);
                girlButton.setImageResource(R.drawable.school_girl_unselect);

                mType = 4;
                mPageNum = 1;

                positionList.clear();
                sameGradeList.clear();
                getRecommends(4, mPageNum);
            }
        });

        //男
        boyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allImgButton.setImageResource(R.drawable.school_all_unselect);
                sameGradeButton.setImageResource(R.drawable.school_same_grade_unselect);
                boyButton.setImageResource(R.drawable.school_boy_select);
                girlButton.setImageResource(R.drawable.school_girl_unselect);

                mType = 5;
                mPageNum = 1;
                positionList.clear();
                boyList.clear();
                getRecommends(5, mPageNum);
            }
        });

        //女
        girlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allImgButton.setImageResource(R.drawable.school_all_unselect);
                sameGradeButton.setImageResource(R.drawable.school_same_grade_unselect);
                boyButton.setImageResource(R.drawable.school_boy_unselect);
                girlButton.setImageResource(R.drawable.school_gril_select);

                mType = 6;
                mPageNum = 1;
                positionList.clear();
                girlList.clear();
                getRecommends(6, mPageNum);
            }
        });*/

        schoolmateAdapter = new SchoolmateAdapter(AddFriends.this,
                new SchoolmateAdapter.hideCallback() {
                    @Override
                    public void hideClick(View v) {

                    }
                },
                new SchoolmateAdapter.acceptCallback() {
                    @Override
                    public void acceptClick(View v) {
                        if (NetWorkUtil.isNetWorkAvailable(AddFriends.this)) {
                            Button button = (Button) v;
                            button.setBackgroundResource(R.drawable.add_friend_apply);
                            button.setEnabled(false);
                            int position = (int) button.getTag();
                            Log.i(TAG, "acceptClick: mType---" + mType);
                            positionList.add(position);
                            addFriend(friendsBeanList.get(position).getUin(),mType);
                        } else {
                            Toast.makeText(AddFriends.this, "网络异常", Toast.LENGTH_SHORT).show();
                        }

                    }
                },
                friendsBeanList,positionList);

        allSchoolmateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int uin = friendsBeanList.get(position).getUin();
                if (NetWorkUtil.isNetWorkAvailable(AddFriends.this)) {
                    getFriendInfo(uin, view);
                } else {
                    Toast.makeText(AddFriends.this, "网络异常", Toast.LENGTH_SHORT).show();
                }

            }

        });

        allSchoolmateListView.setAdapter(schoolmateAdapter);
    }


    //处理可能认识的人
    private void handleMaybeKnowFriend(final List<GetRecommendsRespond.PayloadBean.FriendsBean> friendsBeanList) {
        maybeRoot = (LinearLayout) findViewById(R.id.layout_maybe_know);
        LinearLayout lmklLlNull = (LinearLayout) maybeRoot.findViewById(R.id.lmk_ll_null);
        MesureListView lmkListView = (MesureListView) maybeRoot.findViewById(R.id.lmk_list);

        if (friendsBeanList.size() > 0) {
            lmklLlNull.setVisibility(View.GONE);
        }



        lmkListView.setAdapter(new SchoolmateAdapter(AddFriends.this,
                new SchoolmateAdapter.hideCallback() {
                    @Override
                    public void hideClick(View v) {

                    }
                },
                new SchoolmateAdapter.acceptCallback() {
                    @Override
                    public void acceptClick(View v) {
                        if (NetWorkUtil.isNetWorkAvailable(AddFriends.this)) {
                            Button button = (Button) v;
                            button.setBackgroundResource(R.drawable.add_friend_apply);
                            button.setEnabled(false);

                            int position = (int) button.getTag();
                            positionList.add(position);
                            addFriend(friendsBeanList.get(position).getUin(),mType);
                        } else {
                            Toast.makeText(AddFriends.this, "网络异常", Toast.LENGTH_SHORT).show();
                        }

                    }
                },
                friendsBeanList,positionList));

        lmkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("点击事件");
                int uin = friendsBeanList.get(position).getUin();
                if (NetWorkUtil.isNetWorkAvailable(AddFriends.this)) {
                    getFriendInfo(uin, view);
                } else {
                    Toast.makeText(AddFriends.this, "网络异常", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    //查询朋友的信息
    private void getFriendInfo(int friendUin, View view) {
        final View friendItemView = view;
        Map<String, Object> friendMap = new HashMap<>();
        friendMap.put("userUin", friendUin);
        friendMap.put("uin", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_UIN, 0));
        friendMap.put("token", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_TOKEN, "yplay"));
        friendMap.put("ver", SharePreferenceUtil.get(AddFriends.this, YPlayConstant.YPLAY_VER, 0));
        YPlayApiManger.getInstance().getZivApiService()
                .getUserInfo(friendMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserInfoResponde>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(UserInfoResponde userInfoResponde) {
                        System.out.println("获取朋友资料---" + userInfoResponde.toString());
                        if (userInfoResponde.getCode() == 0) {
                            UserInfoResponde.PayloadBean.InfoBean infoBean =
                                    userInfoResponde.getPayload().getInfo();
                            int status = userInfoResponde.getPayload().getStatus();
                            if (status == 1) {
                                Intent intent = new Intent(AddFriends.this, ActivityFriendsInfo.class);
                                intent.putExtra("yplay_friend_name", infoBean.getNickName());
                                intent.putExtra("yplay_friend_uin", infoBean.getUin());
                                System.out.println("朋友的uin---" + infoBean.getUin());
                                startActivity(intent);
                            } else {
                                showCardDialog(userInfoResponde.getPayload(), friendItemView);
                            }

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("获取朋友资料异常---" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    //显示名片
    private void showCardDialog(UserInfoResponde.PayloadBean payloadBean, View view) {

        final View friendItemView = view;
        final UserInfoResponde.PayloadBean.InfoBean infoBean = payloadBean.getInfo();

        //状态
        int status = payloadBean.getStatus();

        final CardBigDialog cardDialog = new CardBigDialog(AddFriends.this, R.style.CustomDialog,
                payloadBean);

        cardDialog.setAddFriendListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView button = (ImageView) v;
                if (NetWorkUtil.isNetWorkAvailable(AddFriends.this)) {
                    button.setImageResource(R.drawable.peer_friend_requested);
                    //除了更新朋友选项卡信息中的按钮状态外，还要更新外部对应的朋友列表item的按钮状态；
                    if (friendItemView != null) {
                        Button freiendIcon = (Button) friendItemView.findViewById(R.id.af_btn_accept2);
                        if (freiendIcon != null) {
                            freiendIcon.setBackgroundResource(R.drawable.add_friend_apply);
                        }
                    }
                    addFriend(infoBean.getUin(),mType);
                } else {
                    Toast.makeText(AddFriends.this, "网络异常", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cardDialog.show();
    }

    //获取通讯录权限
    private void getNumberBookAuthority() {

        AndPermission.with(AddFriends.this)
                .requestCode(REQUEST_CODE_PERMISSION_SINGLE_CONTACTS)
                .permission(Permission.CONTACTS)
                .callback(mPermissionListener)
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                        AndPermission.rationaleDialog(AddFriends.this, rationale).show();
                    }
                })
                .start();
    }

    PermissionListener mPermissionListener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
            switch (requestCode) {
                case REQUEST_CODE_PERMISSION_SINGLE_CONTACTS:
                    Log.i(TAG, "onSucceed: 通讯录权限成功");
                    getContacts();
                    if (numberBookAuthoritySuccess){
                        Log.i(TAG, "onSucceed: 通讯录有权限");
                    }else {
                        Log.i(TAG, "onSucceed: 通讯录无权限");
                        AndPermission.defaultSettingDialog(AddFriends.this, 401).show();
                    }
                    break;
            }

        }

        @Override
        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
            switch (requestCode) {
                case REQUEST_CODE_PERMISSION_SINGLE_CONTACTS:
                    Log.i(TAG, "onFailed: 通讯录权限失败");
                    getContacts();
                    if (numberBookAuthoritySuccess){
                        Log.i(TAG, "onFailed: 读到通讯录权限了numberBookAuthoritySuccess---" + numberBookAuthoritySuccess);
                    }else {
                        if (AndPermission.hasAlwaysDeniedPermission(AddFriends.this, deniedPermissions)) {
                            if (requestCode == REQUEST_CODE_PERMISSION_SINGLE_CONTACTS) {
                                AndPermission.defaultSettingDialog(AddFriends.this, 400).show();
                            }

                        }
                    }
                    break;
            }
        }
    };

    private void getContacts() {

        if (Build.VERSION.SDK_INT >= 23
                && AddFriends.this.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                && AddFriends.this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("无读取联系人权限");
            return;
        }

        try {
            Uri contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            System.out.println("contactUri---" + contactUri);
            if (contactUri != null) {

                Log.i(TAG, "getContacts: 通讯录权限申请成功");

            }
            Cursor cursor = getContentResolver().query(contactUri,
                    new String[]{"display_name", "sort_key", "contact_id", "data1"},
                    null, null, "sort_key");
            String contactName = "";
            String contactNumber = "";
            //String contactSortKey;
            //int contactId;
            while (cursor != null && cursor.moveToNext()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                com.yeejay.yplay.greendao.ContactsInfo contactsInfo = new com.yeejay.yplay.greendao.ContactsInfo(null,contactName,contactNumber);
                ContactsInfo tempContactsInfo = contactsInfoDao.queryBuilder()
                        .where(ContactsInfoDao.Properties.Phone.eq(contactName))
                        .build().unique();
                if (tempContactsInfo == null){
                    contactsInfoDao.insert(contactsInfo);
                }

            }
            cursor.close();//使用完后一定要将cursor关闭，不然会造成内存泄露等问题

            if (!TextUtils.isEmpty(contactNumber)){
                numberBookAuthoritySuccess = true;
                //跳转到邀请好友界面
                Intent intent = new Intent(AddFriends.this, ActivityInviteFriend.class);
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 400:
                if (!numberBookAuthoritySuccess){
                    getContacts();
                }
                Log.i(TAG, "onActivityResult: numberBookAuthoritySuccess---" + numberBookAuthoritySuccess);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isFromAddFriend){

            startActivity(new Intent(AddFriends.this, MainActivity.class));
            return true;//不执行父类点击事件
        }
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }
}
