package com.yeejay.yplay.model;

/**
 * 通知相关返回
 * Created by Administrator on 2017/12/1.
 */

public class PushNotifyRespond {


    /**
     * code : 0
     * msg : succ
     * payload : {"newFeedCnt":0,"newAddFriendMsgCnt":0,"newOnlineSubmitQustionCnt":0}
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
        return "PushNotifyRespond{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", payload=" + payload +
                '}';
    }

    public static class PayloadBean {
        /**
         * newFeedCnt : 0
         * newAddFriendMsgCnt : 0
         * newOnlineSubmitQustionCnt : 0
         */

        private int newFeedCnt;
        private int newAddFriendMsgCnt;
        private int newOnlineSubmitQustionCnt;

        public int getNewFeedCnt() {
            return newFeedCnt;
        }

        public void setNewFeedCnt(int newFeedCnt) {
            this.newFeedCnt = newFeedCnt;
        }

        public int getNewAddFriendMsgCnt() {
            return newAddFriendMsgCnt;
        }

        public void setNewAddFriendMsgCnt(int newAddFriendMsgCnt) {
            this.newAddFriendMsgCnt = newAddFriendMsgCnt;
        }

        public int getNewOnlineSubmitQustionCnt() {
            return newOnlineSubmitQustionCnt;
        }

        public void setNewOnlineSubmitQustionCnt(int newOnlineSubmitQustionCnt) {
            this.newOnlineSubmitQustionCnt = newOnlineSubmitQustionCnt;
        }

        @Override
        public String toString() {
            return "PayloadBean{" +
                    "newFeedCnt=" + newFeedCnt +
                    ", newAddFriendMsgCnt=" + newAddFriendMsgCnt +
                    ", newOnlineSubmitQustionCnt=" + newOnlineSubmitQustionCnt +
                    '}';
        }
    }
}
