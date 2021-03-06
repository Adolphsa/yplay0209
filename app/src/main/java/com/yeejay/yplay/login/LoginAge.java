package com.yeejay.yplay.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.yeejay.yplay.R;
import com.yeejay.yplay.base.BaseActivity;
import com.yeejay.yplay.customview.WheelView;
import com.yeejay.yplay.model.BaseRespond;
import com.yeejay.yplay.utils.GsonUtil;
import com.yeejay.yplay.utils.LogUtils;
import com.yeejay.yplay.utils.SharePreferenceUtil;
import com.yeejay.yplay.utils.YPlayConstant;
import com.yeejay.yplay.wns.WnsAsyncHttp;
import com.yeejay.yplay.wns.WnsRequestListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginAge extends BaseActivity {

    private static final String TAG = "LoginAge";

    @BindView(R.id.la_back)
    ImageButton laBack;
    @BindView(R.id.la_tv_age)
    TextView mAgeView;
    @BindView(R.id.la_btn_quick_start)
    Button mQuickStartBtn;
    @BindView(R.id.la_wheel_view)
    WheelView mWheelView;

    @OnClick(R.id.la_back)
    public void back(View view) {
        finish();
    }

    Handler mHandler = new Handler();
    ArrayList<String> ageList;
    String mAge;
    boolean isFromInviteCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_age);
        ButterKnife.bind(this);

        getWindow().setStatusBarColor(getResources().getColor(R.color.message_title_color));
        laBack.setVisibility(View.GONE);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null){
            isFromInviteCode = bundle.getBoolean("is_from_invite_code");
        }

        ageList = new ArrayList<String>();
        //添加年龄数据
        for (int i = 0; i < 112; i++) {
            ageList.add(i + " ");
        }

        mWheelView.setOffset(1);
        mWheelView.setItems(ageList);
        mWheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, final String item) {
                Log.d("LoginAge", "selectedIndex: " + selectedIndex + ", item: " + item);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAgeView.setText(item);
                        mAgeView.setTextColor(getResources().getColor(R.color.black));
                        mAge = item;

                        mQuickStartBtn.setEnabled(true);
                        mQuickStartBtn.setTextColor(getResources().getColor(R.color.white));
                    }
                });
            }
        });

        mQuickStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Integer.parseInt(mAge.trim()) < 12) {
                    //年龄未达到要求
                    showNormalDialog();
                } else if (Integer.parseInt(mAge.trim()) >= 12 && Integer.parseInt(mAge.trim()) < 25) {
                    //允许进入
                    LogUtils.getInstance().error("age {}",mAge.trim());
                    settingAge(Integer.parseInt(mAge.trim()));

                } else {
                    //年龄未达到要求
                    showNormalDialog();
                }
            }
        });
    }

    /**
     * 显示对话框
     */
    private void showNormalDialog() {

        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(LoginAge.this);
        normalDialog.setTitle(R.string.la_age_invail);
        normalDialog.setPositiveButton(R.string.la_i_know,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        // 显示
        normalDialog.show();
    }

    //设置年龄
    private void settingAge(int age) {

        String url = YPlayConstant.YPLAY_API_BASE + YPlayConstant.API_SET_AGE_URL;
        Map<String, Object> nameMap = new HashMap<>();
        nameMap.put("age", age);
        nameMap.put("uin", SharePreferenceUtil.get(LoginAge.this, YPlayConstant.YPLAY_UIN, 0));
        nameMap.put("token", SharePreferenceUtil.get(LoginAge.this, YPlayConstant.YPLAY_TOKEN, "yplay"));
        nameMap.put("ver", SharePreferenceUtil.get(LoginAge.this, YPlayConstant.YPLAY_VER, 0));

        WnsAsyncHttp.wnsRequest(url, nameMap, new WnsRequestListener() {
            @Override
            public void onNoInternet() {

            }

            @Override
            public void onStartLoad(int value) {

            }

            @Override
            public void onComplete(String result) {
                Log.i(TAG, "onComplete: 设置年龄---" + result);
                BaseRespond baseRespond = GsonUtil.GsonToBean(result,BaseRespond.class);
                if (baseRespond.getCode() == 0) {
                    startActivity(new Intent(LoginAge.this, LoginAuthorization.class));
                } else {
                    Toast.makeText(LoginAge.this, R.string.set_age_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTimeOut() {

            }

            @Override
            public void onError() {

            }
        });
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isFromInviteCode) {
            return true;
        }
        return false;
    }

}
