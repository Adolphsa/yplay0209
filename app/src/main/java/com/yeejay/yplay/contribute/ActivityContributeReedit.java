package com.yeejay.yplay.contribute;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.yeejay.yplay.R;
import com.yeejay.yplay.base.BaseActivity;
import com.yeejay.yplay.customview.MyLinearLayout;
import com.yeejay.yplay.model.BaseRespond;
import com.yeejay.yplay.utils.GsonUtil;
import com.yeejay.yplay.utils.LogUtils;
import com.yeejay.yplay.utils.NetWorkUtil;
import com.yeejay.yplay.utils.SharePreferenceUtil;
import com.yeejay.yplay.utils.StatuBarUtil;
import com.yeejay.yplay.utils.YPlayConstant;
import com.yeejay.yplay.wns.WnsAsyncHttp;
import com.yeejay.yplay.wns.WnsRequestListener;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivityContributeReedit extends BaseActivity implements MyLinearLayout.OnSizeChangedListener{
    @BindView(R.id.layout_title2)
    TextView titleView;
    @BindView(R.id.layout_title_back2)
    ImageButton layoutTitleBack2;
    @BindView(R.id.contribute_history)
    ImageButton contributeHistory;
    @BindView(R.id.con_img)
    LinearLayout conImg;
    @BindView(R.id.con_selected_img)
    ImageView selectedImg;
    @BindView(R.id.con_edit)
    EditText conEdit;
    @BindView(R.id.con_text_count)
    TextView conTextCount;
    @BindView(R.id.con_apply_button)
    Button conApplyButton;
    @BindView(R.id.con_apply_ll)
    LinearLayout conApplyLl;
    @BindView(R.id.rl_edittext)
    FrameLayout rlEditText;
    @BindView(R.id.scroll_view)
    ScrollView scrollView;
    @BindView(R.id.scroll_inner_view)
    LinearLayout scrollInnerView;
    @BindView(R.id.my_ll)
    MyLinearLayout myLl;

    private static final String TAG = "ContributeReedit";
    private static final String EMOJI_URL = "http://yplay-1253229355.image.myqcloud.com/qicon/";

    private String mQiconUrl;
    private int mPosition;

    @OnClick(R.id.layout_title_back2)
    public void back() {
        finish();
    }

    @OnClick(R.id.con_img)
    public void conEmojiImg() {
        Intent intent = new Intent(ActivityContributeReedit.this, ActivityContribute2.class);
        startActivityForResult(intent, 2);
    }

    @OnClick(R.id.con_selected_img)
    public void conSelectedEmojiImg() {
        Intent intent = new Intent(ActivityContributeReedit.this, ActivityContribute2.class);
        startActivityForResult(intent, 2);
    }

    @OnClick(R.id.con_apply_button)
    public void submit() {
        LogUtils.getInstance().debug("提交");
        if(NetWorkUtil.isNetWorkAvailable(ActivityContributeReedit.this)){

            String questionText = conEdit.getText().toString();
            if (!TextUtils.isEmpty(questionText)){
                LogUtils.getInstance().error("问题不为空, v, emojiIndex = {}, mQiconUrl = {}, getQiconId(mQiconUrl) = {}",
                        emojiIndex, mQiconUrl, getQiconId(mQiconUrl));
                submitQuestion(questionText, emojiIndex != -1 ? emojiIndex : getQiconId(mQiconUrl));
            }

        }else {
            Toast.makeText(ActivityContributeReedit.this,"网络异常",Toast.LENGTH_SHORT).show();
        }

    }

    int emojiIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contribute1);
        ButterKnife.bind(this);

        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        StatuBarUtil.setMiuiStatusBarDarkMode(ActivityContributeReedit.this, true);

        myLl.setOnSizeChangedListener(this);

        conApplyButton.setEnabled(false);

        initOthers();
        initEdit();
    }

    private void initOthers() {
        layoutTitleBack2.setImageResource(R.drawable.back_black);
        titleView.setText(R.string.re_edit);
        contributeHistory.setVisibility(View.GONE);

        //设置之前选择的未通过审核投稿的文本和图片；
        conEdit.setText(getIntent().getStringExtra("selected_con_qtext"));
        mQiconUrl = getIntent().getStringExtra("selected_con_qiconurl");
        mPosition = getIntent().getIntExtra("selected_con_position", 0);
        conImg.setVisibility(View.GONE);
        selectedImg.setVisibility(View.VISIBLE);
        Picasso.with(ActivityContributeReedit.this).load(mQiconUrl).resizeDimen(
                R.dimen.non_ques_head_img_width, R.dimen.non_ques_head_img_height)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(selectedImg);
    }

    private void initEdit() {

        conEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                conTextCount.setText(s.toString().length() + "/30");
                enableButton(s.toString());
            }
        });

        conEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode()
                        && KeyEvent.ACTION_DOWN == event.getAction())){
                    LogUtils.getInstance().debug("回车键被点击");
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private boolean hideInputMethod(Context context, View v) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            return imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }

        return false;
    }

    /*
    * 用于监听输入法软键盘是否弹出;
    */
    public void onSizeChange(int width, int height, int oldWidth, int oldHeight){
        if (height < oldHeight) {//软键盘弹出;
            rlEditText.setBackgroundResource(R.drawable.shape_con1_edit_selected_background);
            conEdit.setCursorVisible(true);

            //scrollView滚动到底部;
            scrollToBottom(scrollView, scrollInnerView);
        } else if (height > oldHeight) {//软键盘收起：
            rlEditText.setBackgroundResource(R.drawable.shape_con1_edit_background);
            conEdit.setCursorVisible(false);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                if(hideInputMethod(this, v)) {
                    //return true; //隐藏键盘时，其他控件不响应点击事件
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /*
    * 通过计算点击区域来判断是否点击了edittext以外的区域；
    */
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            v.getLocationInWindow(leftTop);
            int left = leftTop[0], top = leftTop[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {// 点击了edittext
                return false;
            } else {//点击了editext以外的区域;
                //判断是否点击了“提交”按钮区域，若是，则返回false使得按键事件继续传递
                conApplyButton.getLocationInWindow(leftTop);
                int left1 = leftTop[0], top1 = leftTop[1], bottom1 = top + v.getHeight(), right1 = left
                        + v.getWidth();
                if (event.getX() > left1 && event.getX() < right1
                        && event.getY() > top1 && event.getY() < bottom1) {// 点击了"提交"按钮
                    return false;
                }

                return true;
            }
        }
        return false;
    }

    /*
    * scrollview滚动到底部;
    */
    private void scrollToBottom(final View scroll, final View inner) {
        scrollView.post(new Runnable() {
            public void run() {
                if (scroll == null || inner == null) {
                    return;
                }
                int offset = inner.getMeasuredHeight() - scroll.getHeight();
                if (offset < 0) {
                    offset = 0;
                }

                scroll.scrollTo(0, offset);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == 1) {
            if (data != null) {
                conImg.setVisibility(View.GONE);
                selectedImg.setVisibility(View.VISIBLE);

                int currentSelectEmoji = data.getIntExtra("current_select_emoji", 0);
                emojiIndex = data.getIntExtra("current_emoji_index", 0);
                LogUtils.getInstance().error("1---currentSelectEmoji = {}, current_emoji_index = {}",
                        currentSelectEmoji, emojiIndex);
                String demojiUrl = EMOJI_URL + emojiIndex + ".png";
                Picasso.with(ActivityContributeReedit.this).load(demojiUrl).into(selectedImg);

                enableButton(conEdit.getText().toString().trim());
            }
        }
    }

    private void enableButton(String s){
        if (s.length() > 0 && (!TextUtils.isEmpty(mQiconUrl) || emojiIndex != -1)) {
            conApplyButton.setBackgroundResource(R.drawable.shape_purple_btn_backround);
            conApplyButton.setTextColor(getResources().getColor(R.color.white));
            conApplyButton.setEnabled(true);
        } else {
            conApplyButton.setBackgroundResource(R.drawable.shape_gray_btn_backround);
            conApplyButton.setTextColor(getResources().getColor(R.color.text_color_gray2));
            conApplyButton.setEnabled(false);
        }
    }

    private void submitQuestion(String qtext, int qiconId) {
        Map<String, Object> conMap = new HashMap<>();
        conMap.put("qtext", qtext);
        conMap.put("qiconId", qiconId);
        conMap.put("uin", SharePreferenceUtil.get(ActivityContributeReedit.this, YPlayConstant.YPLAY_UIN, 0));
        conMap.put("token", SharePreferenceUtil.get(ActivityContributeReedit.this, YPlayConstant.YPLAY_TOKEN, "yplay"));
        conMap.put("ver", SharePreferenceUtil.get(ActivityContributeReedit.this, YPlayConstant.YPLAY_VER, 0));

        WnsAsyncHttp.wnsRequest(YPlayConstant.BASE_URL + YPlayConstant.API_SUBMITQUESTION, conMap,
                new WnsRequestListener() {

                    @Override
                    public void onNoInternet() {

                    }

                    @Override
                    public void onStartLoad(int value) {

                    }

                    @Override
                    public void onComplete(String result) {
                        handleSubmitQuestionResponse(result);
                    }

                    @Override
                    public void onTimeOut() {
                    }

                    @Override
                    public void onError() {
                        LogUtils.getInstance().debug("投稿异常");
                    }
                });
    }

    private void handleSubmitQuestionResponse(String result) {
        BaseRespond baseRespond = GsonUtil.GsonToBean(result, BaseRespond.class);
        LogUtils.getInstance().debug("投稿, {}", baseRespond.toString());
        if (baseRespond.getCode() == 0){
            //conEdit.setEnabled(false);

            Toast.makeText(ActivityContributeReedit.this,R.string.contribute_success,
                    Toast.LENGTH_SHORT).show();
            //通知查询投稿页面中的未上线子页面，更新未上线列表信息;
            Intent dataIntent = new Intent();
            dataIntent.putExtra("position", mPosition);
            setResult(6, dataIntent);

            finish();
        }else {
            Toast.makeText(ActivityContributeReedit.this,"提交失败",Toast.LENGTH_SHORT).show();
        }
    }

    private int getQiconId(String qIconUrl) {
        int ret = -1;
        if (!TextUtils.isEmpty(qIconUrl)) {
            int start = qIconUrl.lastIndexOf('/');
            int end = qIconUrl.lastIndexOf('.');
            try {
                LogUtils.getInstance().debug(qIconUrl.substring(start + 1, end));
                ret = Integer.parseInt(qIconUrl.substring(start + 1, end));
            } catch (NumberFormatException e) {
                LogUtils.getInstance().error("exception happend; + " + e.getMessage());
            }
        }

        return ret;
    }
}