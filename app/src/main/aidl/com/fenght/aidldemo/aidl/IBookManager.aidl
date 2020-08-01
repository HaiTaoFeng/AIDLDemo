// IBookManager.aidl
package com.fenght.aidldemo.aidl;
import com.fenght.aidldemo.aidl.Book;
import com.fenght.aidldemo.aidl.NewBookArriveListener;
// Declare any non-default types here with import statements

interface IBookManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
    List<Book> getBookList();
    void addBook(in Book book);
    void registerListener(NewBookArriveListener listener);
    void unregisterListener(NewBookArriveListener listener);
}
