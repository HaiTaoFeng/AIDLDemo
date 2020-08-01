// NewBookArriveListener.aidl
package com.fenght.aidldemo.aidl;
import com.fenght.aidldemo.aidl.Book;
// Declare any non-default types here with import statements

interface NewBookArriveListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     //通知方法
    void newBookArrived(in Book newBook);
}
