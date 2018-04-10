package com.zhketech.alarmclietn.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Root on 2018/4/10.
 */

public class ThreadReceiver extends BroadcastReceiver {

    OnGetDateListern onGetDateListern = null;

    public ThreadReceiver(OnGetDateListern onGetDateListern) {
        this.onGetDateListern = onGetDateListern;
    }
    @Override
    public void onReceive(Context context, Intent intent) {


        if(onGetDateListern!=null){
            onGetDateListern.getData("fgg");
        }
    }
    public interface OnGetDateListern {
        void getData(String str);
    }
}
