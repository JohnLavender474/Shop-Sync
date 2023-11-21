package edu.uga.cs.shopsync.frontend.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.models.ShopSyncModel;
import edu.uga.cs.shopsync.frontend.Constants;

public class MyShopSyncsActivity extends BaseActivity {

    private static final String TAG = "MyShopSyncsActivity";

    /**
     * RecyclerView Adapter for displaying ShopSyncModel items.
     */
    public static class ShopSyncsRecyclerViewAdapter
            extends RecyclerView.Adapter<ShopSyncsRecyclerViewAdapter.ViewHolder> {

        private final List<ShopSyncModel> shopSyncList;
        private final Context context;

        /**
         * Constructor for ShopSyncsRecyclerViewAdapter.
         *
         * @param context      The context of the calling activity.
         * @param shopSyncList The list of ShopSyncModel items to be displayed.
         */
        public ShopSyncsRecyclerViewAdapter(Context context, List<ShopSyncModel> shopSyncList) {
            this.context = context;
            this.shopSyncList = shopSyncList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.shop_sync_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ShopSyncModel shopSyncModel = shopSyncList.get(position);
            holder.bind(shopSyncModel);
        }

        @Override
        public int getItemCount() {
            return shopSyncList.size();
        }

        /**
         * ViewHolder class for holding views associated with ShopSyncModel items.
         */
        public class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView shopSyncNameTextView;
            private final TextView usersTextView;
            private final Button goToShopSyncButton;

            /**
             * Constructor for ViewHolder.
             *
             * @param itemView The view associated with the ViewHolder.
             */
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                shopSyncNameTextView = itemView.findViewById(R.id.textViewShopSyncName);
                usersTextView = itemView.findViewById(R.id.textViewUsers);
                goToShopSyncButton = itemView.findViewById(R.id.buttonGoToShopSync);
            }

            /**
             * Bind method to associate data from a ShopSyncModel to the views in the ViewHolder.
             *
             * @param shopSyncModel The ShopSyncModel item to be displayed.
             */
            public void bind(ShopSyncModel shopSyncModel) {
                String shopSyncNameText = "Name: " + shopSyncModel.getName();
                shopSyncNameTextView.setText(shopSyncNameText);

                // TODO: usersTextView.setText(number of users);

                goToShopSyncButton.setOnClickListener(v -> {
                    Log.d(TAG, "go to shop sync button clicked for shop sync: " + shopSyncModel);

                    Intent intent = new Intent(context, ShopSyncActivity.class);
                    intent.putExtra(Constants.SHOP_SYNC_UID_EXTRA, shopSyncModel.getUid());
                    context.startActivity(intent);
                });
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_shop_syncs);

        // check if the user is signed in
        FirebaseUser currentUser = checkIfUserIsLoggedInAndFetch(true);
        Log.d(TAG,
              "onCreate: user signed in with email " + currentUser.getEmail() + " and id (" +
                      currentUser.getUid() + ")");

        // set up the recycler view
        List<ShopSyncModel> shopSyncs = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerViewShopSyncs);
        ShopSyncsRecyclerViewAdapter adapter = new ShopSyncsRecyclerViewAdapter(this, shopSyncs);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        applicationGraph.shopSyncsService().getShopSyncsForUser(currentUser.getUid())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onCreate: task for shop syncs is complete");

                        DataSnapshot dataSnapshot = task.getResult();
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            // Convert each child's value into a ShopSyncModel object
                            ShopSyncModel shopSync = childSnapshot.getValue(ShopSyncModel.class);
                            if (shopSync != null) {
                                shopSyncs.add(shopSync);
                            }
                        }

                        Log.d(TAG,
                              "onCreate: retrieved " + shopSyncs.size() +
                                      " shop syncs");

                        // notify the adapter that the data set has changed
                        adapter.notifyDataSetChanged();
                    } else {
                        signOutOnErrorAndRedirectToMainActivity("Failed to retrieve shop syncs. " +
                                                                        "Signing out now.");
                    }
                });

        // TODO: fix issue where shop syncs are not being fetched
        //  properly
        /*
        applicationGraph.shopSyncsService()
                .getShopSyncsWithUserUid(currentUser.getUid())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onCreate: task for shop syncs is complete");

                        // gather all the shop syncs into a list
                        DataSnapshot dataSnapshot = task.getResult();
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            // Convert each child's value into a ShopSyncModel object
                            ShopSyncModel shopSync = childSnapshot.getValue(ShopSyncModel.class);
                            if (shopSync != null) {
                                shopSyncs.add(shopSync);
                            }
                        }
                        Log.d(TAG, "onCreate: retrieved " + shopSyncs.size() + " shop syncs");

                        // notify the adapter that the data set has changed
                        adapter.notifyDataSetChanged();
                    } else {
                        signOutOnErrorAndRedirectToMainActivity("Failed to retrieve shop syncs. " +
                                                                        "Signing out now.");
                    }
                });
         */
    }

}
