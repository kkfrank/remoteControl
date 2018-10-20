package com.frank.remotecontrol;

public class MessageEvent {
    private String tag;
    private String msg;
    public MessageEvent(){

    }
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
