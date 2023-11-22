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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_items, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewShoppingItems);

        // Initialize shopping items (replace this with your actual data retrieval)
        List<ShoppingItemModel> shoppingItems = getShoppingItems();

        // Set up the RecyclerView and its adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ShoppingItemsAdapter adapter = new ShoppingItemsAdapter(shoppingItems);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<ShoppingItemModel> getShoppingItems() {
        // Replace this with your actual data retrieval logic
        List<ShoppingItemModel> items = new ArrayList<>();
        items.add(new ShoppingItemModel("1", "Shop Sync", "Item 1", false));
        items.add(new ShoppingItemModel("2", "Shop Sync", "Item 2", true));
        // Add more items as needed
        return items;
    }

    private static class ShoppingItemsAdapter
            extends RecyclerView.Adapter<ShoppingItemsAdapter.ViewHolder> {

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
            private final EditText editTextInBasket;
            private final Button buttonSetInMyShoppingBasket;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                editTextItemName = itemView.findViewById(R.id.editTextItemName);
                editTextInBasket = itemView.findViewById(R.id.editTextInBasket);
                buttonSetInMyShoppingBasket =
                        itemView.findViewById(R.id.buttonSetInMyShoppingBasket);
            }

            void bind(ShoppingItemModel item) {
                // Bind data to views
                editTextItemName.setText(item.getName());
                String inBasketText = "In a basket: " + (item.isInBasket() ? "Yes" : "No");
                editTextInBasket.setText(inBasketText);

                // TODO: listen to updates to shopping item model and update the UI accordingly
                /*
                item.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                    @Override
                    public void onPropertyChanged(Observable sender, int propertyId) {
                        // TODO: disable in basket text view if not in basket
                        // TODO: show basket text view if in basket

                        // TODO: disable add to basket button if in basket
                        // TODO: enable add to basket button if not in basket
                    }
                 */

                // Set up the purchase button click listener
                buttonSetInMyShoppingBasket.setOnClickListener(v -> {
                    // Perform purchase logic (remove from shopping items, add to purchased items)
                    // You'll need to implement this part based on your requirements
                    setItemInMyBasket(item);
                });
            }

            private void setItemInMyBasket(ShoppingItemModel item) {
                // TODO: do not remove item?
                // shoppingItems.remove(item);

                // Add item to shopping basket, mark this item as in basket, and update
                // the database and UI

                // Notify the adapter that the data set has changed
                notifyDataSetChanged();
            }
        }
    }
}

