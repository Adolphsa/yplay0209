package com.yeejay.yplay.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.yeejay.yplay.R;
import com.yeejay.yplay.friend.AddFriends;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddFriendGuide extends AppCompatActivity {

    @BindView(R.id.aafg_enter)
    Button aafgEnter;

    @OnClick(R.id.aafg_enter)
    public void aafgEnter(){
        //添加好友
        Intent intent = new Intent(AddFriendGuide.this, AddFriends.class);
        intent.putExtra("from_add_friend_guide",true);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_guide);
        ButterKnife.bind(this);

        getWindow().setStatusBarColor(getResources().getColor(R.color.edit_text_color2));
    }

}