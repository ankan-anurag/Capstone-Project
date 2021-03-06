package space.ankan.golocal.screens.mykitchen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import space.ankan.golocal.R;
import space.ankan.golocal.core.BaseFragment;
import space.ankan.golocal.model.kitchens.Dish;
import space.ankan.golocal.utils.CommonUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link space.ankan.golocal.screens.mykitchen.ManageKitchenFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManageKitchenFragment extends BaseFragment implements ChildEventListener {


    @BindView(R.id.dish_list)
    RecyclerView mRecyclerView;

    @BindView(R.id.empty_message)
    TextView mEmptyListText;

    private DishAdapter adapter;

    public ManageKitchenFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ManageKitchenFragment.
     */
    public static ManageKitchenFragment newInstance() {
        return new ManageKitchenFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mRootView = super.inflate(inflater, container, savedInstanceState, R.layout.fragment_manage_kitchen);
        if (mRootView == null) return null;
        ButterKnife.bind(this, mRootView);
        setupRecycler();
        syncWithFirebase();
        return mRootView;
    }

    private void setupRecycler() {
        adapter = new DishAdapter(getActivity(), new ArrayList<Dish>());
        adapter.setmTwoPaneListener(mTwoPaneListener);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(adapter);
    }

    private void syncWithFirebase() {
        CommonUtils.removeViews(mRecyclerView);
        CommonUtils.showViews(mEmptyListText);
        getFirebaseHelper().getDishesReference(getUserKitchenId()).addChildEventListener(this);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Dish dish = dataSnapshot.getValue(Dish.class);
        dish.key = dataSnapshot.getKey();
        adapter.add(dish);
        CommonUtils.showViews(mRecyclerView);
        CommonUtils.hideViews(mEmptyListText);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Dish dish = dataSnapshot.getValue(Dish.class);
        if (dish.key == null)
            adapter.add(dish);
        else
            adapter.replace(dish);
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

    public void clearSelection() {
        if (adapter == null) return;
        adapter.clearSelection();
    }
}




