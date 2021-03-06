package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.INoteViewModule;
import com.thinkernote.ThinkerNote._interface.v.OnNoteViewListener;
import com.thinkernote.ThinkerNote.bean.CommonBean3;
import com.thinkernote.ThinkerNote.bean.main.GetNoteByNoteIdBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 笔记详情 m层 具体实现
 */
public class NoteViewModuleImpl implements INoteViewModule {

    private Context context;
    private static final String TAG = "SJY";

    public NoteViewModuleImpl(Context context) {
        this.context = context;
    }


    @Override
    public void mGetNote(final OnNoteViewListener listener, long noteId) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .GetNoteByNoteId(noteId, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean3<GetNoteByNoteIdBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mGetNoteByNoteId--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetNoteByNoteId 异常onError:" + e.toString());
                        listener.onGetNoteFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean3<GetNoteByNoteIdBean> bean) {
                        MLog.d(TAG, "mGetNoteByNoteId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onGetNoteSuccess(bean.getNote());
                        } else {
                            listener.onGetNoteFailed(bean.getMsg(), null);
                        }
                    }


                });
    }
}
