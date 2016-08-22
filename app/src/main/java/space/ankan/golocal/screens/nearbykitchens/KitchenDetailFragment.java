package space.ankan.golocal.screens.nearbykitchens;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import space.ankan.golocal.R;
import space.ankan.golocal.core.BaseFragment;
import space.ankan.golocal.model.kitchens.Dish;
import space.ankan.golocal.model.kitchens.Kitchen;
import space.ankan.golocal.screens.mykitchen.DishAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class KitchenDetailFragment extends BaseFragment implements OnMapReadyCallback {

    private static final String LOG_CAT = KitchenDetailFragment.class.getSimpleName();
    public static final String MOVIE_INFO = "movie";

    private Kitchen mKitchen;

    private ShareActionProvider mShareActionProvider;

    @BindView(R.id.kitchen_description)
    TextView kitchenDescription;

    @BindView(R.id.dish_list)
    RecyclerView mDishesRecycler;
    private DishAdapter adapter;

    @BindView(R.id.kitchen_address)
    TextView kitchenAddress;

    private GoogleMap mMap;
    private Marker mMarker;

    public KitchenDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_kitchen_detail, container, false);
        ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);
        fill();
        SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_kitchen_detail, menu);
        MenuItem item = menu.findItem(R.id.menu_share_kitchen);
        mShareActionProvider = (android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(item);
        setShareIntent();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void fill() {

        if (getArguments() != null)
            mKitchen = (Kitchen) getArguments().getSerializable("kitchen");
        else
            Log.e(LOG_CAT, "Null arguments in the fragment");

        if (mKitchen == null) return;
        kitchenDescription.setText(mKitchen.description);
        kitchenAddress.setText(mKitchen.address);
        setupDishRecycler();

    }

    private void setupDishRecycler() {
        adapter = new DishAdapter(getActivity(), new ArrayList<Dish>(), true);
        mDishesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mDishesRecycler.setAdapter(adapter);
        syncWithFirebase();
    }

    private void syncWithFirebase() {
        getFirebaseHelper().getDishesReference(mKitchen.key).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Dish dish = dataSnapshot.getValue(Dish.class);
                adapter.add(dish);
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


    private void setShareIntent() {

        String shareText = "Hey there, I just found a great kitchen at GoLocal Kitchens. Check it out.. \n" +
                mKitchen.name + "\n"
                + "Description: " + mKitchen.description + "\n"
                + "Address: " + mKitchen.address + "\n";

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.setType("text/plain");
        if (mShareActionProvider != null)
            mShareActionProvider.setShareIntent(shareIntent);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng latLng = new LatLng(mKitchen.latitude, mKitchen.longitude);
        // Add some markers to the map, and add a data object to each marker.
        mMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(mKitchen.name));
        mMarker.setTag(0);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        // Zoom in, animating the camera.
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (getActivity().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            mMap.setMyLocationEnabled(true);

    }
}