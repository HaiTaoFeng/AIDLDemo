package com.fenght.aidldemo.aidl;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.Nullable;

/**
 * 数据管理服务
 * @author fht
 * @time 2020年8月1日14:02:38
 */
public class BookManagerService extends Service {

    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();
    private AtomicBoolean isDestory = new AtomicBoolean(false);
    //使用RemoteCallbackList可以对监听进行反注册，否则反注册会失败
    private RemoteCallbackList<NewBookArriveListener> listeners = new RemoteCallbackList<>();
    private Binder mBinder = new IBookManager.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }

        @Override
        public void registerListener(NewBookArriveListener listener) throws RemoteException {
            //注册监听
            listeners.register(listener);
        }

        @Override
        public void unregisterListener(NewBookArriveListener listener) throws RemoteException {
            //反注册
            listeners.unregister(listener);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1,"android"));
        mBookList.add(new Book(2,"java"));
        //启动线程
        new Thread(new ServiceWorker()).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        int check = checkCallingOrSelfPermission("com.fenght.aidldemo.aidl.BOOK_SERVICE");
        if (check == PackageManager.PERMISSION_DENIED) {
            return null;
        }
        return mBinder;
    }

    @Override
    public void onDestroy() {
        isDestory.set(true);
        super.onDestroy();
    }

    private class ServiceWorker implements Runnable{
        @Override
        public void run() {
            while (!isDestory.get()){
                try {
                    Thread.sleep(100);
                    int bookId = mBookList.size() + 1;
                    Book newBook = new Book(bookId,"新书" + bookId);
                    mBookList.add(newBook);
                    Log.e("fht","服务中添加新书：" + newBook.toString());
                    final int size = listeners.beginBroadcast();
                    for (int i=0;i<size;i++) {
                        //获取监听
                        NewBookArriveListener newBookArriveListener = listeners.getBroadcastItem(i);
                        if (newBookArriveListener != null) {
                            //发送通知
                            newBookArriveListener.newBookArrived(newBook);
                        }
                    }
                    //beginBroadcast和finishBroadcast必须配对使用
                    listeners.finishBroadcast();
                    //中断重连测试
//                    if (bookId == 9) {
//                        //结束当前进程，测试Binder死亡回调
//                        android.os.Process.killProcess(android.os.Process.myPid());
//                        return;
//                    }
                } catch (InterruptedException | RemoteException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
