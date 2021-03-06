package space.ankan.golocal.screens;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import butterknife.BindView;
import butterknife.ButterKnife;
import space.ankan.golocal.R;
import space.ankan.golocal.core.AppConstants;
import space.ankan.golocal.core.LoggedInActivity;
import space.ankan.golocal.core.MyViewPager;
import space.ankan.golocal.core.TwoPaneListener;
import space.ankan.golocal.model.kitchens.Dish;
import space.ankan.golocal.model.kitchens.Kitchen;
import space.ankan.golocal.model.notifications.Notification;
import space.ankan.golocal.screens.chat.ChannelsFragment;
import space.ankan.golocal.screens.chat.ChatActivityFragment;
import space.ankan.golocal.screens.mykitchen.ManageKitchenFragment;
import space.ankan.golocal.screens.mykitchen.addDish.AddDishActivity;
import space.ankan.golocal.screens.mykitchen.addDish.AddDishFragment;
import space.ankan.golocal.screens.nearbykitchens.KitchenDetailFragment;
import space.ankan.golocal.screens.nearbykitchens.KitchenListFragment;
import space.ankan.golocal.screens.setupkitchen.SetupKitchenFragment;
import space.ankan.golocal.services.FetchAddressIntentService;
import space.ankan.golocal.utils.CommonUtils;
import space.ankan.golocal.utils.NotificationUtils;

/**
 * Created by Ankan.
 * The landing activity for user which contains all the important fragments.
 */

public class MainActivity extends LoggedInActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, TwoPaneListener {

    public static final int TAB_COUNT = 3;
    private static final int SETUP_KITCHEN_TAB = 0;
    private static final int OWNER_KITCHENS_NEARBY_TAB = 0;
    private static final int DEFAULT_KITCHENS_NEARBY_TAB = 1;
    private static final int MANAGE_KITCHEN_TAB = 1;
    private static final int CHAT_TAB = 2;

    private static final int GPS_PERMISSION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 11;
    private static final String DETAIL_FRAG_TAG = "DetailFragment";

    private ManageKitchenFragment manageKitchenFragment;
    private KitchenListFragment kitchenListFragment;
    private Fragment lastKitchenDetailFragment, lastChannelDetailFragment;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    @BindView(R.id.fab)
    FloatingActionButton addDishFab;


    private static final String PAGER_STATE = "state";

    @BindView(R.id.viewpager)
    MyViewPager mViewPager;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.tab0)
    ImageView tab0;

    @BindView(R.id.tab1)
    ImageView tab1;

    @BindView(R.id.tab2)
    ImageView tab2;

    @BindView(R.id.tabLayout)
    View tabLayout;

    @BindView(R.id.detail_container)
    @Nullable
    FrameLayout detailContainer;

    private ImageView[] tabs;
    private int[] defaultSelectIcons;
    private int[] defaultDeselectIcons;
    private int[] ownerSelectIcons;
    private int[] ownerDeselectIcons;
    private int[] defaultTitle;
    private int[] ownerTitle;
    private boolean twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initImageResources();
        setupViewMode();
        setupToolbar();
        setupViewPager(savedInstanceState);
        setupFab();
        createLocationRequest();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        syncNotifications();

    }


    private void setupViewMode() {
        if (detailContainer != null)
            twoPane = true;
        CommonUtils.removeViews(detailContainer);

    }

    private void initImageResources() {
        int setupKitchenSelect = R.drawable.setup_kitchen_filled;
        int setupKitchenDeselect = R.drawable.setup_kitchen_black;
        int kitchenNearbySelect = R.drawable.kitchens_nearby_white;
        int kitchenNearbyDeselect = R.drawable.kitchens_nearby_black;
        int chatDeselect = R.drawable.chat_black;
        int chatSelect = R.drawable.chat_white;
        int manageKitchenDeselect = R.drawable.manage_black;
        int manageKitchenSelect = R.drawable.manage_white;
        tabs = new ImageView[]{tab0, tab1, tab2};
        defaultSelectIcons = new int[]{setupKitchenSelect, kitchenNearbySelect, chatSelect};
        defaultDeselectIcons = new int[]{setupKitchenDeselect, kitchenNearbyDeselect, chatDeselect};
        ownerSelectIcons = new int[]{kitchenNearbySelect, manageKitchenSelect, chatSelect};
        ownerDeselectIcons = new int[]{kitchenNearbyDeselect, manageKitchenDeselect, chatDeselect};
        defaultTitle = new int[]{R.string.setup_kitchen_title, R.string.kitchen_nearby_title, R.string.chat_title};
        ownerTitle = new int[]{R.string.kitchen_nearby_title, R.string.manage_title, R.string.chat_title};
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() == null) return;
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        toolbar.setTitle(getResources().getString(R.string.app_name));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(Bundle savedInstanceState) {

        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), TAB_COUNT);
        initializeFragments(adapter);

        mViewPager.setAdapter(adapter);

        mViewPager.setSaveEnabled(true);
        mViewPager.setOffscreenPageLimit(2);
        if (savedInstanceState != null) {
            mViewPager.setCurrentItem(savedInstanceState.getInt(PAGER_STATE));
        }

        mViewPager.setPagingEnabled(!twoPane);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                formatTabs(position);
                updateDetailFragment(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mViewPager.setCurrentItem(1); //middle page
        formatTabs(1);
        for (int i = 0; i < tabs.length; i++) {
            final int position = i;
            tabs[position].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mViewPager.setCurrentItem(position);
                }
            });
        }

    }

    private void initializeFragments(FragmentAdapter adapter) {
        adapter.clearFragments();

        ChannelsFragment channelsFragment = new ChannelsFragment();
        kitchenListFragment = new KitchenListFragment();
        int i = 0;
        if (isUserKitchenOwner()) {
            manageKitchenFragment = ManageKitchenFragment.newInstance();
            adapter.addFragment(kitchenListFragment);
            tabs[i++].setContentDescription(getString(R.string.cd_tab_nearby_kitchens));
            adapter.addFragment(manageKitchenFragment);
            tabs[i++].setContentDescription(getString(R.string.cd_tab_manage_kitchen));
            adapter.addFragment(channelsFragment);
            tabs[i].setContentDescription(getString(R.string.cd_tab_chats));

        } else {
            SetupKitchenFragment setupKitchenFragment = SetupKitchenFragment.newInstance();
            adapter.addFragment(setupKitchenFragment);
            tabs[i++].setContentDescription(getString(R.string.cd_tab_setup_kitchen));
            adapter.addFragment(kitchenListFragment);
            tabs[i++].setContentDescription(getString(R.string.cd_tab_nearby_kitchens));
            adapter.addFragment(channelsFragment);
            tabs[i].setContentDescription(getString(R.string.cd_tab_chats));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void formatTabs(int position) {
        //log("formatting tabs position: " + position + " | add dish fab shown: " + addDishFab.isShown());
        for (int i = 0; i < TAB_COUNT; i++) {
            if (i == position)
                selectTab(i);
            else
                deselectTab(i);
        }
        if (isUserKitchenOwner()) {
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(ownerTitle[position]);

            if (position == MANAGE_KITCHEN_TAB) {
                CommonUtils.showViews(addDishFab);
            } else {
                CommonUtils.hideViews(addDishFab);
            }
        } else {
            CommonUtils.closeKeyBoard(this);
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(defaultTitle[position]);
            addDishFab.hide();
        }
    }

    private void selectTab(int position) {

        ImageView tab = tabs[position];
        tab.setBackgroundColor(ContextCompat.getColor(this, R.color.tab_select));

        //kitchen owner layout
        if (isUserKitchenOwner())
            tab.setImageDrawable(ContextCompat.getDrawable(this, ownerSelectIcons[position]));

            // default layout
        else
            tab.setImageDrawable(ContextCompat.getDrawable(this, defaultSelectIcons[position]));


    }

    private void deselectTab(int position) {
        ImageView tab = tabs[position];
        tab.setBackgroundColor(ContextCompat.getColor(this, R.color.tab_deselect));

        //kitchen owner layout
        if (isUserKitchenOwner())
            tab.setImageDrawable(ContextCompat.getDrawable(this, ownerDeselectIcons[position]));

            // default layout
        else
            tab.setImageDrawable(ContextCompat.getDrawable(this, defaultDeselectIcons[position]));
    }

    private void setupFab() {
        addDishFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!twoPane)
                    AddDishActivity.createIntent(MainActivity.this);
                else setupManageDishView(null);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        int state = mViewPager.getCurrentItem();
        outState.putInt(PAGER_STATE, state);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mViewPager.setCurrentItem(savedInstanceState.getInt(PAGER_STATE));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //log("google api connected");
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED))
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, GPS_PERMISSION);

        checkLocationSettings();
    }


    public void checkLocationSettings() {
        if (mLastLocation != null) return;
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            @Override
            public void onResult(@NonNull LocationSettingsResult result) {

                final Status status = result.getStatus();
                //final LocationSettingsStates states = result.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        //log("got result for location settings: success");

                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            //log("got result for location settings: resolution required");

                            status.startResolutionForResult(
                                    MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.

                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(this, R.string.enable_gps, Toast.LENGTH_SHORT).show();
                        //kitchenListFragment.updateLocation(getResources().getString(R.string.enable_gps));
                        break;
                    default:
                        break;
                }
                break;
        }

    }

    protected void startLocationUpdates() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            log("starting location updates");

        } else {
            log("permission not available");
            kitchenListFragment.onError("Could not fetch your location");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case GPS_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startLocationUpdates();

                break;


        }
    }

    private void createLocationRequest() {
        log("creating location request");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60 * 1000);// 10 second interval
        mLocationRequest.setFastestInterval(5000); // 5 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        // log("location changed:[ " + mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude());
        if (mLastLocation == null) return;
        CommonUtils.cacheLocation(getSharedPref(), mLastLocation);

        if (kitchenListFragment != null)
            kitchenListFragment.updateLocation(mLastLocation);
        AddressResultReceiver resultReceiver = new AddressResultReceiver(new Handler());
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(RECEIVER, resultReceiver);
        intent.putExtra(LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);

    }

    public Location getLocation() {
        return mLastLocation;
    }

    @Override
    public void setupKitchenDetail(Kitchen kitchen) {
        if (kitchen == null) return;
        lastKitchenDetailFragment = KitchenDetailFragment.newInstance(kitchen);
        addDetailFragment(lastKitchenDetailFragment);
    }

    @Override
    public void setupChatDetail(String channelId, String channelName, String userId) {
        if (TextUtils.isEmpty(channelId)) return;
        lastChannelDetailFragment = ChatActivityFragment.createInstance(channelId, channelName, userId);
        addDetailFragment(lastChannelDetailFragment);
    }

    @Override
    public void setupManageDishView(Dish dish) {
        if (dish == null && twoPane) manageKitchenFragment.clearSelection();
        addDetailFragment(AddDishFragment.newInstance(dish));
    }

    @Override
    public void setupStartKitchenTab() {
        addDetailFragment(null);

    }

    @Override
    public boolean isTwoPane() {
        return twoPane;
    }

    class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            String currentAddressText = resultData.getString(AppConstants.RESULT_DATA_KEY);
            if (kitchenListFragment != null && !TextUtils.isEmpty(currentAddressText))
                kitchenListFragment.updateLocation(currentAddressText);

            // Show a toast message if an address was found.


        }
    }

    public static void createIntent(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    private void addDetailFragment(Fragment fragment) {
        if (!twoPane) return;
        if (fragment == null) {
            CommonUtils.hideViews(detailContainer);
            return;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_container, fragment, DETAIL_FRAG_TAG)
                .commit();
        CommonUtils.showViews(detailContainer);

    }


    private void updateDetailFragment(int positon) {
        if (isUserKitchenOwner()) {
            switch (positon) {
                case OWNER_KITCHENS_NEARBY_TAB:
                    addDetailFragment(lastKitchenDetailFragment);
                    break;
                case MANAGE_KITCHEN_TAB:
                    setupManageDishView(null);
                    break;
                case CHAT_TAB:
                    addDetailFragment(lastChannelDetailFragment);
                    break;
            }
            return;
        }
        switch (positon) {
            case SETUP_KITCHEN_TAB:
                removeDetailContainer();
                break;
            case DEFAULT_KITCHENS_NEARBY_TAB:
                addDetailFragment(lastKitchenDetailFragment);
                break;
            case CHAT_TAB:
                addDetailFragment(lastChannelDetailFragment);
                break;
        }

    }

    private void removeDetailContainer() {

        CommonUtils.hideViews(detailContainer);
    }

    private void syncNotifications() {
        getFirebaseHelper().getCurrentUserNotificationsReference().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Notification notification = dataSnapshot.getValue(Notification.class);
                log("Received notification " + notification.title + " : " + notification.message);
                NotificationUtils.notifyUser(MainActivity.this, notification);
                dataSnapshot.getRef().removeValue();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
