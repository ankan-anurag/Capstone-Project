package space.ankan.golocal.screens.nearbykitchens;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import space.ankan.golocal.R;
import space.ankan.golocal.model.kitchens.Kitchen;
import space.ankan.golocal.utils.CommonUtils;

/**
 * Created by anurag on 18-Dec-15.
 */
public class KitchenAdapter extends RecyclerView.Adapter<KitchenListItemViewHolder> {

    private Context mContext;
    private ArrayList<Kitchen> kitchens;

    public KitchenAdapter(Context context, ArrayList<Kitchen> list) {
        this.mContext = context;
        this.kitchens = list;
    }

    public Kitchen getItem(int position) {
        return kitchens.get(position);
    }

    public ArrayList<Kitchen> getItems() {
        return kitchens;
    }

    @Override
    public KitchenListItemViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.list_item_kitchen, parent, false);
        return new KitchenListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final KitchenListItemViewHolder holder, int i) {
        final Kitchen kitchen = kitchens.get(i);
        holder.mView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                KitchenDetailActivity.createIntent(mContext, kitchen);
            }
        });

        if (!TextUtils.isEmpty(kitchen.imageUrl))
            Picasso.with(mContext).load(kitchen.imageUrl).into(holder.mKitchenImage);
        else holder.mKitchenImage.setAlpha(0.4f);

        if (kitchen.isFavourite)
            holder.mfavourite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_red_300_18dp));
        else
            holder.mfavourite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_light));

        holder.mfavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Implement persisting favourites and notifying for menu changes
                Toast.makeText(mContext, "This has not been implemented yet.", Toast.LENGTH_LONG).show();

            }
        });
        holder.mTitle.setText(kitchen.name);
        holder.mDesc.setText(kitchen.description);
        holder.rating.setText(String.valueOf(CommonUtils.roundTwoDecimals(kitchen.overallRating)));


    }

    @Override
    public int getItemCount() {
        if (kitchens == null)
            return 0;
        return kitchens.size();
    }

    public int getCount() {
        return getItemCount();
    }

    public void add(Kitchen kitchen) {
        this.kitchens.add(kitchen);
        this.notifyDataSetChanged();
    }

    public void clear() {
        this.kitchens.clear();
        this.notifyDataSetChanged();
    }

    public void addAll(List<Kitchen> kitchens) {
        if (!this.kitchens.isEmpty())
            return;
        this.kitchens.addAll(kitchens);
        this.notifyDataSetChanged();
    }

}
