package com.thinkernote.ThinkerNote._interface.v;

/**
 *  v层 只有一个接口 通用回调
 */
public interface OnCommonListener {
    void onSuccess(Object obj);

    void onFailed(String msg, Exception e);

}
