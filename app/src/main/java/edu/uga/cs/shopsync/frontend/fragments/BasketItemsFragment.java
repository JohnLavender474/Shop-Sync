package edu.uga.cs.shopsync.frontend.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.models.BasketItemModel;

public class BasketItemsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_basket_items, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewBasketItems);

        // Initialize basket items (replace this with your actual data retrieval)
        List<BasketItemModel> basketItems = getBasketItems();

        // Set up the RecyclerView and its adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        BasketItemsAdapter adapter = new BasketItemsAdapter(basketItems);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<BasketItemModel> getBasketItems() {
        // Replace this with your actual data retrieval logic
        List<BasketItemModel> items = new ArrayList<>();
        // Add basket items as needed
        return items;
    }

    private static class BasketItemsAdapter
            extends RecyclerView.Adapter<BasketItemsAdapter.ViewHolder> {

        private final List<BasketItemModel> items;

        BasketItemsAdapter(List<BasketItemModel> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_basket_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView textViewItemName;
            private final EditText editTextQuantity;
            private final EditText editTextPricePerUnit;
            private final Button buttonUpdateItem;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewItemName = itemView.findViewById(R.id.textViewItemName);
                editTextQuantity = itemView.findViewById(R.id.editTextQuantity);
                editTextPricePerUnit = itemView.findViewById(R.id.editTextPricePerUnit);
                buttonUpdateItem = itemView.findViewById(R.id.buttonUpdateItem);
            }

            void bind(BasketItemModel item) {
                textViewItemName.setText(item.getShoppingItem().getName());
                editTextQuantity.setText(String.valueOf(item.getQuantity()));
                editTextPricePerUnit.setText(String.valueOf(item.getPricePerUnit()));

                // Set up the update button click listener
                buttonUpdateItem.setOnClickListener(v -> {
                    // Perform update logic (update quantity and price per unit)
                    updateBasketItem(item);
                });
            }

            private void updateBasketItem(BasketItemModel item) {
                // Replace this with your actual logic for handling the update
                long newQuantity = Long.parseLong(editTextQuantity.getText().toString());
                double newPricePerUnit = Double.parseDouble(editTextPricePerUnit.getText()
                                                                    .toString());

                // Update the item
                item.setQuantity(newQuantity);
                item.setPricePerUnit(newPricePerUnit);

                // Notify the adapter that the data set has changed
                notifyDataSetChanged();
            }
        }
    }
}

