package edu.uga.cs.shopsync.frontend.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.models.PurchasedItemModel;

public class PurchasedItemsFragment extends Fragment implements ChildEventListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_purchased_items, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewPurchasedItems);

        // TODO: Initialize purchased items (replace this with your actual data retrieval)
        List<PurchasedItemModel> purchasedItems = new ArrayList<>();

        // Set up the RecyclerView and its adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        PurchasedItemsAdapter adapter = new PurchasedItemsAdapter(purchasedItems);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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

    private static class PurchasedItemsAdapter
            extends RecyclerView.Adapter<PurchasedItemsAdapter.ViewHolder> {

        private final List<PurchasedItemModel> purchasedItems;

        PurchasedItemsAdapter(List<PurchasedItemModel> purchasedItems) {
            this.purchasedItems = purchasedItems;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_purchase_group, parent, false);
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

        private static class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView textViewUsername;
            private final TextView textViewTotalCost;
            private final TextView textViewTimeOfPurchase;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewUsername = itemView.findViewById(R.id.textViewUsername);
                textViewTotalCost = itemView.findViewById(R.id.textViewTotalCost);
                textViewTimeOfPurchase = itemView.findViewById(R.id.textViewTimeOfPurchase);
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

