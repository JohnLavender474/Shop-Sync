package edu.uga.cs.shopsync.frontend.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;

public class ShoppingItemsFragment extends Fragment {

    private List<ShoppingItemModel> shoppingItems;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_items, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewShoppingItems);

        // Initialize shopping items (replace this with your actual data retrieval)
        shoppingItems = getShoppingItems();

        // Set up the RecyclerView and its adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ShoppingItemsAdapter adapter = new ShoppingItemsAdapter(shoppingItems);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<ShoppingItemModel> getShoppingItems() {
        // Replace this with your actual data retrieval logic
        List<ShoppingItemModel> items = new ArrayList<>();
        items.add(new ShoppingItemModel("1", "Item 1", 2, 10.0));
        items.add(new ShoppingItemModel("2", "Item 2", 1, 5.0));
        // Add more items as needed
        return items;
    }

    private class ShoppingItemsAdapter extends RecyclerView.Adapter<ShoppingItemsAdapter.ViewHolder> {

        private final List<ShoppingItemModel> items;

        ShoppingItemsAdapter(List<ShoppingItemModel> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_shopping_item, parent, false);
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

            private final EditText editTextItemName;
            private final EditText editTextQuantity;
            private final EditText editTextPricePerUnit;
            private final Button buttonPurchase;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                editTextItemName = itemView.findViewById(R.id.editTextItemName);
                editTextQuantity = itemView.findViewById(R.id.editTextQuantity);
                editTextPricePerUnit = itemView.findViewById(R.id.editTextPricePerUnit);
                buttonPurchase = itemView.findViewById(R.id.buttonPurchase);
            }

            void bind(ShoppingItemModel item) {
                // Bind data to views
                editTextItemName.setText(item.getName());
                editTextQuantity.setText(String.valueOf(item.getQuantity()));
                editTextPricePerUnit.setText(String.valueOf(item.getPricePerUnit()));

                // Set up the purchase button click listener
                buttonPurchase.setOnClickListener(v -> {
                    // Perform purchase logic (remove from shopping items, add to purchased items)
                    // You'll need to implement this part based on your requirements
                    purchaseItem(item);
                });
            }

            private void purchaseItem(ShoppingItemModel item) {
                // Replace this with your actual logic for handling the purchase
                shoppingItems.remove(item);
                // Add item to purchased items
                // Notify the adapter that the data set has changed
                notifyDataSetChanged();
            }
        }
    }
}

