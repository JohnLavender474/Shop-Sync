package edu.uga.cs.shopsync.frontend.fragments;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.List;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.models.PurchasedItemModel;
import edu.uga.cs.shopsync.frontend.Constants;
import edu.uga.cs.shopsync.utils.ArraySetList;
import edu.uga.cs.shopsync.utils.CallbackReceiver;
import edu.uga.cs.shopsync.utils.Props;

public class PurchasedItemsFragment extends Fragment implements ChildEventListener {

    private static final String TAG = "PurchasedItemsFragment";

    public static final String ACTION_INITIALIZE_PURCHASED_ITEMS =
            "ACTION_INITIALIZE_PURCHASED_ITEMS";
    public static final String ACTION_UNDO_PURCHASE = "ACTION_UNDO_PURCHASE";
    public static final String ACTION_DELETE_PURCHASE = "ACTION_DELETE_PURCHASE";
    public static final String ACTION_EDIT_COST_PER_UNIT = "ACTION_EDIT_COST_PER_UNIT";
    public static final String ACTION_EDIT_QUANTITY = "ACTION_EDIT_QUANTITY";
    public static final String PROP_PURCHASED_ITEMS = "PROP_PURCHASED_ITEMS";

    private final List<PurchasedItemModel> purchasedItems;
    private final PurchasedItemsAdapter adapter;

    private CallbackReceiver callbackReceiver;

    public PurchasedItemsFragment() {
        super();

        Log.d(TAG, "PurchasedItemsFragment: called");

        callbackReceiver = null;
        purchasedItems = new ArraySetList<>();
        adapter = new PurchasedItemsAdapter(purchasedItems);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");

        // if the activity does not implement CallbackReceiver, then throw an exception
        // otherwise, set the callbackReceiver to the activity
        if (callbackReceiver == null) {
            if (!(getActivity() instanceof CallbackReceiver)) {
                Log.e(TAG, "onCreateView: Activity must implement CallbackReceiver");
                throw new ClassCastException("Activity must implement CallbackReceiver");
            }

            Log.d(TAG, "onCreateView: Activity implements CallbackReceiver");
            callbackReceiver = (CallbackReceiver) getActivity();
        }

        View view = inflater.inflate(R.layout.fragment_purchased_items, container, false);

        // set up the RecyclerView and its adapter
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewPurchasedItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        callbackReceiver.onCallback(ACTION_INITIALIZE_PURCHASED_ITEMS, Props.of(
                Pair.create(PROP_PURCHASED_ITEMS, purchasedItems),
                Pair.create(Constants.ADAPTER, adapter)));

        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: called");
        super.onStart();

        if (!(getActivity() instanceof CallbackReceiver)) {
            Log.e(TAG, "onStart: Activity must implement CallbackReceiver");
            throw new ClassCastException("Activity must implement CallbackReceiver");
        }

        Log.d(TAG, "onStart: Activity implements CallbackReceiver");
        callbackReceiver = (CallbackReceiver) getActivity();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();

        Log.d(TAG, "onDestroy: setting callbackReceiver to null");
        callbackReceiver = null;
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        PurchasedItemModel purchasedItem = snapshot.getValue(PurchasedItemModel.class);
        Log.d(TAG, "onChildAdded: purchasedItem = " + purchasedItem);

        if (purchasedItem == null) {
            Log.e(TAG, "onChildAdded: purchasedItem is null");
            return;
        }

        // TODO: very inefficient to add to beginning of array list, a better way to do this
        purchasedItems.add(0, purchasedItem);


        adapter.notifyItemInserted(0);
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    private class PurchasedItemsAdapter
            extends RecyclerView.Adapter<PurchasedItemsAdapter.ViewHolder> {

        private final List<PurchasedItemModel> purchasedItems;

        PurchasedItemsAdapter(List<PurchasedItemModel> purchasedItems) {
            this.purchasedItems = purchasedItems;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_purchased_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(purchasedItems.get(position));
        }

        @Override
        public int getItemCount() {
            return purchasedItems.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView textViewEmail;
            private final TextView textViewTotalCost;
            private final Button buttonUndoPurchase;
            private final Button buttonDeletePurchase;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO: fix
                /*
                textViewUsername = itemView.findViewById(R.id.textViewUsername);
                textViewTotalCost = itemView.findViewById(R.id.textViewTotalCost);
                textViewTimeOfPurchase = itemView.findViewById(R.id.textViewTimeOfPurchase);
                 */
            }

            void bind(PurchasedItemModel purchasedItem) {
                // Bind data to views
                textViewUsername.setText(purchasedItem.getUserEmail());

                // TODO: add as meta data to the shop sync
                // textViewTotalCost.setText(String.valueOf(purchasedItem.getTotalCost()));
                // textViewTimeOfPurchase.setText(purchasedItem.getTimeOfPurchase());

                // Set up click listener for modification (you need to implement this)
                itemView.setOnClickListener(v -> {
                    // Handle modifications to the Purchase Group
                    // Implement modification logic here
                });
            }
        }
    }
}

