package edu.uga.cs.shopsync.frontend.fragments;

import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.models.BasketItemModel;
import edu.uga.cs.shopsync.frontend.Constants;
import edu.uga.cs.shopsync.frontend.utils.ChildEventListenerFragment;
import edu.uga.cs.shopsync.frontend.utils.TextWatcherAdapter;
import edu.uga.cs.shopsync.utils.CallbackReceiver;
import edu.uga.cs.shopsync.utils.Props;
import edu.uga.cs.shopsync.utils.UtilMethods;

/**
 * This fragment displays the basket items for the current user.
 */
public class BasketItemsFragment extends ChildEventListenerFragment {

    private static final String TAG = "BasketItemsFragment";

    public static final String ACTION_INITIALIZE_BASKET_ITEMS = "ACTION_INITIALIZE_BASKET_ITEMS";
    public static final String ACTION_UPDATE_BASKET_ITEM = "ACTION_UPDATE_BASKET_ITEM";
    public static final String ACTION_PURCHASE_BASKET_ITEM = "ACTION_PURCHASE_BASKET_ITEM";
    public static final String ACTION_REMOVE_BASKET_ITEM = "ACTION_REMOVE_BASKET_ITEM";
    public static final String ACTION_FETCH_ITEM_NAME_BY_SHOPPING_ITEM_UID =
            "ACTION_FETCH_ITEM_NAME_BY_SHOPPING_ITEM_UID";
    public static final String PROP_BASKET_ITEMS = "PROP_BASKET_ITEMS";

    private final List<BasketItemModel> basketItems;
    private final BasketItemsAdapter adapter;

    private CallbackReceiver callbackReceiver;

    /**
     * Default constructor for BasketItemsFragment.
     */
    public BasketItemsFragment() {
        super();

        Log.d(TAG, "BasketItemsFragment: constructor called");

        basketItems = new ArrayList<>();
        adapter = new BasketItemsAdapter();
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

        // TODO: implement landscape mode
        /*
        View view;

        // Check if landscape mode
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            view = inflater.inflate(R.layout.fragment_basket_items_landscape, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_basket_items, container, false);
        }
        */

        View view = inflater.inflate(R.layout.fragment_basket_items, container, false);

        // set up the recycler view and its adapter
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewBasketItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        callbackReceiver.onCallback(ACTION_INITIALIZE_BASKET_ITEMS, Props.of(Pair.create(
                PROP_BASKET_ITEMS, basketItems), Pair.create(Constants.ADAPTER, adapter)));

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
        Log.d(TAG, "onChildAdded: called with snapshot = " + snapshot + " and previous " +
                "child name = " + previousChildName);

        BasketItemModel basketItem = snapshot.getValue(BasketItemModel.class);
        if (basketItem == null || basketItem.getShoppingItemUid() == null ||
                basketItem.getShoppingItemUid().isBlank() ||
                basketItem.getShoppingBasketUid() == null ||
                basketItem.getShoppingBasketUid().isBlank()) {
            Log.d(TAG, "onChildAdded: basketItem is null or has null or blank shoppingItemUid or " +
                    "shoppingBasketUid. Snapshot = " + snapshot);
            return;
        }

        Log.d(TAG, "onChildAdded: basketItem = " + basketItem);

        // TODO: very inefficient to add to beginning of array list, a better way to do this
        basketItems.add(0, basketItem);

        adapter.notifyItemInserted(0);
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        Log.d(TAG, "onChildChanged: called with snapshot = " + snapshot + " and previous " +
                "child name = " + previousChildName);

        BasketItemModel basketItem = snapshot.getValue(BasketItemModel.class);
        if (basketItem == null || basketItem.getShoppingItemUid() == null ||
                basketItem.getShoppingItemUid().isBlank() ||
                basketItem.getShoppingBasketUid() == null ||
                basketItem.getShoppingBasketUid().isBlank()) {
            Log.d(TAG, "onChildAdded: basketItem is null or has null or blank shoppingItemUid or " +
                    "shoppingBasketUid. Snapshot = " + snapshot);
            return;
        }

        Log.d(TAG, "onChildChanged: basketItem = " + basketItem);

        int index = basketItems.indexOf(basketItem);
        if (index == -1) {
            Log.e(TAG, "onChildChanged: basketItem not found in basketItems");
            return;
        }

        basketItems.set(index, basketItem);
        adapter.notifyItemChanged(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
        Log.d(TAG, "onChildRemoved: called with snapshot = " + snapshot);

        String key = snapshot.getKey();
        if (key == null) {
            Log.e(TAG, "onChildRemoved: snapshot key is null");
            return;
        }
        if (key.equals(Constants.BASKET_ITEMS_DB_KEY)) {
            try {
                Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                Log.d(TAG, "onChildRemoved: map = " + map);
            } catch (Exception e) {
                Log.e(TAG, "onChildRemoved: snapshot value is not a map for key = " + key, e);
                return;
            }
        }

        BasketItemModel basketItem = snapshot.getValue(BasketItemModel.class);
        if (basketItem == null || basketItem.getShoppingItemUid() == null ||
                basketItem.getShoppingItemUid().isBlank() ||
                basketItem.getShoppingBasketUid() == null ||
                basketItem.getShoppingBasketUid().isBlank()) {
            Log.d(TAG, "onChildRemoved: basketItem is null or has null or blank shoppingItemUid " +
                    "or shoppingBasketUid. Basket item = " + basketItem + ", snapshot = " + snapshot);
            return;
        }

        Log.d(TAG, "onChildRemoved: basketItem = " + basketItem);

        int index = basketItems.indexOf(basketItem);
        if (index == -1) {
            Log.e(TAG, "onChildRemoved: basketItem not found in basketItems");
            return;
        }

        basketItems.remove(index);
        adapter.notifyItemRemoved(index);
    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        Log.d(TAG, "onChildMoved: called with snapshot = " + snapshot + " and previous " +
                "child name = " + previousChildName);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Log.e(TAG, "onCancelled: called with error = " + error);
    }

    public class BasketItemsAdapter extends RecyclerView.Adapter<BasketItemsAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_basket_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(basketItems.get(position));
        }

        @Override
        public int getItemCount() {
            return basketItems.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView textViewItemName;
            private final EditText editTextQuantity;
            private final TextView textViewQuantityWarning;
            private final EditText editTextPricePerUnit;
            private final TextView textViewPriceWarning;
            private final Button buttonUpdate;
            private final Button buttonPurchase;
            private final Button buttonRemove;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewItemName = itemView.findViewById(R.id.textViewItemName);
                editTextQuantity = itemView.findViewById(R.id.editTextQuantity);
                textViewQuantityWarning = itemView.findViewById(R.id.textViewQuantityWarning);
                editTextPricePerUnit = itemView.findViewById(R.id.editTextPricePerUnit);
                textViewPriceWarning = itemView.findViewById(R.id.textViewPriceWarning);

                buttonUpdate = itemView.findViewById(R.id.buttonUpdate);
                buttonPurchase = itemView.findViewById(R.id.buttonPurchase);
                buttonRemove = itemView.findViewById(R.id.buttonRemove);
            }

            void bind(BasketItemModel item) {
                // fetch and display the item name
                callbackReceiver.onCallback(ACTION_FETCH_ITEM_NAME_BY_SHOPPING_ITEM_UID, Props.of(
                        Pair.create(Constants.SHOPPING_ITEM, item.getShoppingItemUid()),
                        Pair.create(Constants.TEXT_VIEW, textViewItemName)));

                // set up edit text quantity
                TextWatcher quantityTextWatcher = new TextWatcherAdapter() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (!UtilMethods.isLong(s.toString())) {
                            textViewQuantityWarning.setVisibility(View.VISIBLE);
                            return;
                        }
                        long quantity = Long.parseLong(s.toString());
                        textViewQuantityWarning.setVisibility(quantity == 0 ? View.VISIBLE :
                                                                      View.INVISIBLE);
                    }
                };
                editTextQuantity.setText(String.valueOf(item.getQuantity()));
                editTextQuantity.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        editTextQuantity.addTextChangedListener(quantityTextWatcher);
                    } else {
                        editTextQuantity.removeTextChangedListener(quantityTextWatcher);

                        /*
                        String text = editTextQuantity.getText().toString();

                        if (!UtilMethods.isLong(text)) {
                            Toast.makeText(getContext(), "Input must be a number!",
                                           Toast.LENGTH_SHORT).show();
                            editTextQuantity.setText(String.valueOf(item.getQuantity()));
                            return;
                        }

                        long newQuantity = Long.parseLong(editTextQuantity.getText().toString());

                        // check if the new quantity is 0
                        if (newQuantity <= 0) {
                            Toast.makeText(getContext(), "Quantity cannot be 0!",
                                           Toast.LENGTH_SHORT).show();
                            editTextQuantity.setText(String.valueOf(item.getQuantity()));
                            return;
                        }

                        // check if the new quantity is the same as the old quantity
                        if (newQuantity == item.getQuantity()) {
                            return;
                        }

                        // update the new quantity
                        item.setQuantity(newQuantity);
                        callbackReceiver.onCallback(
                                ACTION_UPDATE_BASKET_ITEM,
                                Props.of(Pair.create(Constants.BASKET_ITEM, item)));

                         */
                    }
                });

                // set up edit text price per unit
                TextWatcher pricePerUnitTextWatcher = new TextWatcherAdapter() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (!UtilMethods.isDouble(s.toString())) {
                            textViewPriceWarning.setVisibility(View.VISIBLE);
                            return;
                        }
                        double pricePerUnit = Double.parseDouble(s.toString());
                        textViewPriceWarning.setVisibility(pricePerUnit == 0 ? View.VISIBLE :
                                                                   View.INVISIBLE);
                    }
                };
                editTextPricePerUnit.setText(String.valueOf(item.getPricePerUnit()));
                editTextPricePerUnit.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        editTextPricePerUnit.addTextChangedListener(pricePerUnitTextWatcher);
                    } else {
                        editTextPricePerUnit.removeTextChangedListener(pricePerUnitTextWatcher);

                        /*
                        String text = editTextPricePerUnit.getText().toString();
                        if (!UtilMethods.isDouble(text)) {
                            Toast.makeText(getContext(), "Input must be a number!",
                                           Toast.LENGTH_SHORT).show();
                            editTextPricePerUnit.setText(String.valueOf(item.getPricePerUnit()));
                            return;
                        }

                        double newPricePerUnit = Double.parseDouble(editTextPricePerUnit.getText()
                                                                            .toString());

                        // check if the new price per unit is 0
                        if (newPricePerUnit == 0) {
                            Toast.makeText(getContext(), "Price per unit cannot be 0",
                                           Toast.LENGTH_SHORT).show();
                            editTextPricePerUnit.setText(String.valueOf(item.getPricePerUnit()));
                            return;
                        }

                        // check if the new price per unit is the same as the old price per unit
                        if (newPricePerUnit == item.getPricePerUnit()) {
                            return;
                        }

                        // round to 2 decimal places
                        newPricePerUnit =
                                Double.parseDouble(UtilMethods.roundToDecimalPlaces
                                (newPricePerUnit, 2));

                        // update the new price per unit
                        item.setPricePerUnit(newPricePerUnit);
                        callbackReceiver.onCallback(
                                ACTION_UPDATE_BASKET_ITEM,
                                Props.of(Pair.create(Constants.BASKET_ITEM, item)));

                         */
                    }
                });

                // set up update button
                buttonUpdate.setOnClickListener(v -> {
                    String text = editTextQuantity.getText().toString();

                    if (!UtilMethods.isLong(text)) {
                        Toast.makeText(getContext(), "Input must be a number!",
                                       Toast.LENGTH_SHORT).show();
                        editTextQuantity.setText(String.valueOf(item.getQuantity()));
                        return;
                    }

                    long newQuantity = Long.parseLong(editTextQuantity.getText().toString());

                    // check if the new quantity is 0
                    if (newQuantity <= 0) {
                        Toast.makeText(getContext(), "Quantity cannot be 0!",
                                       Toast.LENGTH_SHORT).show();
                        editTextQuantity.setText(String.valueOf(item.getQuantity()));
                        return;
                    }

                    text = editTextPricePerUnit.getText().toString();

                    // check that price per unit is a number
                    if (!UtilMethods.isDouble(text)) {
                        Toast.makeText(getContext(), "Price per unit must be a number!",
                                       Toast.LENGTH_SHORT).show();
                        editTextPricePerUnit.setText(String.valueOf(item.getPricePerUnit()));
                        return;
                    }
                    double newPricePerUnit = Double.parseDouble(editTextPricePerUnit.getText()
                                                                        .toString());

                    // check that price per unit is not negative
                    if (newPricePerUnit < 0) {
                        Toast.makeText(getContext(), "Price per unit cannot be negative!",
                                       Toast.LENGTH_SHORT).show();
                        editTextPricePerUnit.setText(String.valueOf(item.getPricePerUnit()));
                        return;
                    }

                    // round to 2 decimal places
                    newPricePerUnit = Double.parseDouble(UtilMethods.roundToDecimalPlaces
                            (newPricePerUnit, 2));

                    Runnable onSuccess = () -> Toast.makeText(getContext(), "Basket item updated!",
                                                              Toast.LENGTH_SHORT).show();
                    Runnable onFailure = () -> Toast.makeText(getContext(), "Failed to update " +
                            "basket item!", Toast.LENGTH_SHORT).show();

                    // update the values of the basket item
                    item.setQuantity(newQuantity);
                    item.setPricePerUnit(newPricePerUnit);
                    callbackReceiver.onCallback(ACTION_UPDATE_BASKET_ITEM, Props.of(
                            Pair.create(Constants.BASKET_ITEM, item),
                            Pair.create(Constants.ON_SUCCESS, onSuccess),
                            Pair.create(Constants.ON_FAILURE, onFailure)));
                });

                // set up purchase button
                buttonPurchase.setOnClickListener(v -> {
                    if (callbackReceiver == null) {
                        Log.e(TAG, "bind: callbackReceiver is null");
                    } else {
                        callbackReceiver.onCallback(ACTION_PURCHASE_BASKET_ITEM, Props.of(
                                Pair.create(Constants.BASKET_ITEM, item)));
                    }
                });

                // set up remove button
                buttonRemove.setOnClickListener(v -> {
                    if (callbackReceiver == null) {
                        Log.e(TAG, "bind: callbackReceiver is null");
                    } else {
                        callbackReceiver.onCallback(ACTION_REMOVE_BASKET_ITEM, Props.of(
                                Pair.create(Constants.BASKET_ITEM, item)));
                    }
                });
            }

        }
    }
}

