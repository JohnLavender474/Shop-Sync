package edu.uga.cs.shopsync.frontend.utils;

/**
 * A simple adapter class for the TextWatcher interface. This class provides empty implementations
 * for all methods of the TextWatcher interface. This class is useful for when you only want to
 * override a single method of the TextWatcher interface.
 */
public class TextWatcherAdapter implements android.text.TextWatcher {

    public void beforeTextChanged(java.lang.CharSequence charSequence, int i, int i2, int i3) {
    }

    public void onTextChanged(java.lang.CharSequence charSequence, int i, int i2, int i3) {
    }

    public void afterTextChanged(android.text.Editable editable) {
    }

}
