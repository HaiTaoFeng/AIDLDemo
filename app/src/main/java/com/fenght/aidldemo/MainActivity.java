package com.fenght.aidldemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import com.fenght.aidldemo.aidl.Book;
import com.fenght.aidldemo.aidl.BookManagerService;
import com.fenght.aidldemo.aidl.IBookManager;
import com.fenght.aidldemo.aidl.NewBookArriveListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView tv_book;
    private IBookManager iBookManager;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 1:
                    Log.e("fht","新书：" + msg.obj.toString());
                    tv_book.setText(msg.obj.toString());
                    break;
            }
            return false;
        }
    });

    //服务连接
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iBookManager = IBookManager.Stub.asInterface(service);
            try {
                //设置binder死亡代理，binder死亡时有回调
                service.linkToDeath(deathRecipient,0);
                //获取数据
                List<Book> list = iBookManager.getBookList();
                Log.e("fht","书本：" + list.toString());
                iBookManager.addBook(new Book(3,"这是客户端发送的书"));
                List<Book> list1 = iBookManager.getBookList();
                Log.e("fht","书本：" + list1.toString());
                iBookManager.registerListener(newBookArriveListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    //回调方法：当binder死亡时，系统会回调binderDied方法
    private IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if (iBookManager == null) {
                return;
            }
            //先解除binder旧的死亡监听，在ServiceConnection中会重新新的设置监听
            iBookManager.asBinder().unlinkToDeath(deathRecipient,0);
            iBookManager = null;
            //死亡时，重新启动连接
            Intent intent = new Intent(MainActivity.this, BookManagerService.class);
            bindService(intent,serviceConnection,BIND_AUTO_CREATE);
        }
    };

    //新书监听
    private NewBookArriveListener newBookArriveListener = new NewBookArriveListener.Stub() {
        @Override
        public void newBookArrived(Book newBook) throws RemoteException {
            //发送消息，由UI线程处理数据
            handler.obtainMessage(1,newBook).sendToTarget();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_book = findViewById(R.id.tv_book);
        Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent,serviceConnection,BIND_AUTO_CREATE);
    }



    @Override
    protected void onDestroy() {
        if (iBookManager != null && iBookManager.asBinder().isBinderAlive()) {
            try {
                //反注册监听
                iBookManager.unregisterListener(newBookArriveListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(serviceConnection);
        super.onDestroy();
    }
}
