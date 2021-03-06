package com.ypcxpt.fish.library.view.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.callback.NavCallback;
import com.alibaba.android.arouter.launcher.ARouter;

public class SchemeFilterActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doFilter();
    }

    private void doFilter() {
        Uri uri = null;
        try {
            uri = getIntent().getData();
        } finally {
            if (uri == null) {
                finish();
                return;
            }
        }
        ARouter.getInstance().build(uri).navigation(this, new NavCallback() {
            @Override
            public void onArrival(Postcard postcard) {
                finish();
            }
        });
    }
}
