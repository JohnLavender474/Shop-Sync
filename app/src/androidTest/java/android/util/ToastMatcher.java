package android.util;

import android.os.IBinder;
import android.view.WindowManager;

import androidx.test.espresso.Root;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class ToastMatcher extends TypeSafeMatcher<Root> {

    @Override
    public void describeTo(Description description) {
        description.appendText("is toast");
    }

    @Override
    public boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;
        IBinder windowToken = root.getDecorView().getWindowToken();
        IBinder appToken = root.getDecorView().getApplicationWindowToken();

        return type == WindowManager.LayoutParams.TYPE_TOAST && windowToken == appToken;
    }

    public static ToastMatcher isToast() {
        return new ToastMatcher();
    }
}
