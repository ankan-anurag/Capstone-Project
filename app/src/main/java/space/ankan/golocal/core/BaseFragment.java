package space.ankan.golocal.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.auth.BuildConfig;
import com.google.firebase.auth.FirebaseUser;

import space.ankan.golocal.helpers.FirebaseHelper;
import space.ankan.golocal.model.users.User;

/**
 * Created by ankan.
 */

public abstract class BaseFragment extends Fragment {

    private static final String SHARED_PREF_FILE_NAME = "GoLocal";
    private static SharedPreferences sharedPreferences;
    private FirebaseHelper firebaseHelper;
    private Boolean isUserKitchenOwner;
    private User user;
    protected TwoPaneListener mTwoPaneListener;

    protected User getUser() {
        if (user == null)
            user = ((LoggedInActivity) getActivity()).getUser();

        return user;
    }

    protected FirebaseHelper getFirebaseHelper() {

        if (firebaseHelper == null)
            firebaseHelper = new FirebaseHelper();
        return firebaseHelper;
    }

    @SuppressWarnings("unused")
    protected boolean isUserKitchenOwner() {
        if (isUserKitchenOwner == null)
            isUserKitchenOwner = getSharedPref().getString(AppConstants.KITCHEN_ID, null) != null;
        return isUserKitchenOwner;
    }

    protected String getUserKitchenId() {
        return getSharedPref().getString(AppConstants.KITCHEN_ID, null);

    }

    protected SharedPreferences getSharedPref() {
        if (sharedPreferences == null)
            sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    @Nullable
    public View inflate(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, @LayoutRes int layout) {
        View view = inflater.inflate(layout, container, false);
        firebaseHelper = new FirebaseHelper();
        if (getActivity() instanceof TwoPaneListener) {
            mTwoPaneListener = (TwoPaneListener) getActivity();
        }
        return view;
    }

    protected FirebaseUser getCurrentUser() {
        return getFirebaseHelper().getFirebaseAuth().getCurrentUser();
    }

    // logging for debugs
    public static void dLog(String log) {
        if (BuildConfig.DEBUG) return;
        Log.wtf(AppConstants.TAG, log);
    }

    protected void saveUserType(String kitchenId) {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).saveUserType(kitchenId);
    }

    protected boolean isTwoPane() {
        return mTwoPaneListener != null && mTwoPaneListener.isTwoPane();
    }

}
