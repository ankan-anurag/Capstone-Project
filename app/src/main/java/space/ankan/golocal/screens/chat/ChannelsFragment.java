package space.ankan.golocal.screens.chat;

import android.content.Context;
import android.os.Bundle;
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
import space.ankan.golocal.model.channels.Channel;
import space.ankan.golocal.utils.CommonUtils;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class ChannelsFragment extends BaseFragment implements ChildEventListener {

    private View mRootView;
    private ChannelsAdapter adapter;

    @BindView(R.id.list)
    RecyclerView mRecyclerView;

    @BindView(R.id.empty_message)
    TextView mEmptyListText;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChannelsFragment() {
    }

    @SuppressWarnings("unused")
    public static ChannelsFragment newInstance(int columnCount) {
        return new ChannelsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = super.inflate(inflater, container, savedInstanceState, R.layout.fragment_channels_list);
        if (mRootView == null) return null;
        ButterKnife.bind(this, mRootView);
        setupRecycler();
        syncWithFirebase();
        return mRootView;
    }

    private void setupRecycler() {
        Context context = mRootView.getContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new ChannelsAdapter(new ArrayList<Channel>(), getActivity());
        adapter.setTwoPaneListener(mTwoPaneListener);
        mRecyclerView.setAdapter(adapter);

    }

    private void syncWithFirebase() {
        CommonUtils.removeViews(mRecyclerView);
        CommonUtils.showViews(mEmptyListText);
        getFirebaseHelper().getUserChannels().addChildEventListener(this);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        adapter.add(dataSnapshot.getValue(Channel.class));
        CommonUtils.showViews(mRecyclerView);
        CommonUtils.hideViews(mEmptyListText);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        adapter.modify(dataSnapshot.getValue(Channel.class), dataSnapshot.getKey());
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
}
