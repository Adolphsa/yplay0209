package com.yeejay.yplay.model;

/**
 *确认收到好友动态返回
 * Created by Administrator on 2017/10/31.
 */

public class FriendFeedsMakesureRespond {


    /**
     * code : 0
     * msg : succ
     * payload : {"cnt":1}
     */

    private int code;
    private String msg;
    private PayloadBean payload;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public PayloadBean getPayload() {
        return payload;
    }

    public void setPayload(PayloadBean payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "FriendFeedsMakesureRespond{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", payload=" + payload +
                '}';
    }

    public static class PayloadBean {
        /**
         * cnt : 1
         */

        private int cnt;

        public int getCnt() {
            return cnt;
        }

        public void setCnt(int cnt) {
            this.cnt = cnt;
        }

        @Override
        public String toString() {
            return "PayloadBean{" +
                    "cnt=" + cnt +
                    '}';
        }
    }
}
