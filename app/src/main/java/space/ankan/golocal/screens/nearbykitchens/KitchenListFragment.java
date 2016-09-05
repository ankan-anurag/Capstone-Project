package space.ankan.golocal.screens.nearbykitchens;

import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import space.ankan.golocal.R;
import space.ankan.golocal.core.AppConstants;
import space.ankan.golocal.core.BaseFragment;
import space.ankan.golocal.model.kitchens.Kitchen;
import space.ankan.golocal.screens.MainActivity;
import space.ankan.golocal.utils.CommonUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class KitchenListFragment extends BaseFragment implements GeoQueryEventListener {

    @BindView(R.id.content_kitchen_list)
    RecyclerView mRecyclerView;

    private View mRootView;
    private KitchenAdapter adapter;
    private double range = 10; //default
    private Location location;
    private GeoQuery mQuery;
    private AlertDialog rangePicker;
    private EditText rangeInput;

    @BindView(R.id.current_location)
    TextView currentLocation;

    public KitchenListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_kitchen_list, container, false);
        ButterKnife.bind(this, mRootView);
        setupRecycler();
        syncWithFirebase();
        setHasOptionsMenu(true);
        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_kitchen_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.range_set:
                pickRange();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void pickRange() {

        if (rangePicker == null) {
            rangeInput = new EditText(getActivity());
            rangeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rangeInput.setLayoutParams(lp);

            rangePicker = new AlertDialog.Builder(getActivity())
                    .setMessage("Enter range in Kms")
                    .setView(rangeInput)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            CommonUtils.closeKeyBoard(getActivity());
                        }
                    })
                    .setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            double r;
                            try {
                                r = Double.parseDouble(rangeInput.getText().toString());
                            } catch (Exception e) {
                                return;
                            }

                            if (r <= 0) {
                                Toast.makeText(getActivity(), R.string.range_less_than_zero, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (r == range) return;
                            range = r;
                            CommonUtils.closeKeyBoard(getActivity());
                            syncWithFirebase();

                        }
                    }).create();
        }

        rangeInput.setText(String.valueOf((int) range));
        rangeInput.setSelection(rangeInput.getText().length());
        rangePicker.show();

    }

    private void setupRecycler() {
        if (adapter == null)
            adapter = new KitchenAdapter(getActivity(), new ArrayList<Kitchen>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(adapter);
        location = ((MainActivity) getActivity()).getLocation();
    }

    public void updateLocation(Location location) {

        if (this.location != null && location.getLatitude() == this.location.getLatitude() && location.getLongitude() == this.location.getLongitude())
            return;
        if (this.location != null) return; //FIXME do not refresh too often
        this.location = location;
        syncWithFirebase();
    }

    @Override
    public void onResume() {
        super.onResume();
        syncWithFirebase();
    }


    private void syncWithFirebase() {
        dLog("syncing with firebase");

        if (adapter == null)
            adapter = new KitchenAdapter(getActivity(), new ArrayList<Kitchen>());

        if (mQuery != null)
            mQuery.removeAllListeners();

        adapter.clear();

        if (location == null)
            Toast.makeText(getActivity(), R.string.enable_gps, Toast.LENGTH_LONG);

        else {
            mQuery = getFirebaseHelper().buildQueryForKitchens(new GeoLocation(location.getLatitude(), location.getLongitude()),
                    range);
            mQuery.addGeoQueryEventListener(this);
        }
        CommonUtils.closeKeyBoard(getActivity());

    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        getFirebaseHelper().getKitchenReference(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Kitchen kitchen = dataSnapshot.getValue(Kitchen.class);
                    if (kitchen == null) return;
                    kitchen.key = dataSnapshot.getKey();
                    adapter.add(kitchen);
                    dLog("kitchen key: " + kitchen.key);
                } catch (DatabaseException e) {
                    Log.e(AppConstants.TAG, "Error retrieving item with key " + dataSnapshot.getKey() + ": " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onKeyExited(String key) {

    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {

    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {

    }

    public void updateLocation(String currentAddressText) {
        if (currentLocation == null) return;
        currentLocation.setText(currentAddressText);
    }
}
