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
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.exceptions.IllegalNullValueException;
import edu.uga.cs.shopsync.backend.exceptions.TaskFailureException;
import edu.uga.cs.shopsync.backend.models.ShopSyncModel;
import edu.uga.cs.shopsync.frontend.Constants;
import edu.uga.cs.shopsync.frontend.dtos.ShopSyncDto;

public class MyShopSyncsActivity extends BaseActivity {

    private static final String TAG = "MyShopSyncsActivity";

    /**
     * RecyclerView Adapter for displaying ShopSyncModel items.
     */
    public static class ShopSyncsRecyclerViewAdapter
            extends RecyclerView.Adapter<ShopSyncsRecyclerViewAdapter.ViewHolder> {

        private final List<ShopSyncDto> shopSyncs;
        private final Context context;

        /**
         * Constructor for ShopSyncsRecyclerViewAdapter.
         *
         * @param context   The context of the calling activity.
         * @param shopSyncs The list of ShopSyncModel items to be displayed.
         */
        public ShopSyncsRecyclerViewAdapter(Context context, List<ShopSyncDto> shopSyncs) {
            this.context = context;
            this.shopSyncs = shopSyncs;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_shop_sync, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ShopSyncDto shopSync = shopSyncs.get(position);
            holder.bind(shopSync);
        }

        @Override
        public int getItemCount() {
            return shopSyncs.size();
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
             * @param shopSyncDto The ShopSyncDto item to be displayed.
             */
            public void bind(ShopSyncDto shopSyncDto) {
                // set the text view to display the shop sync name
                String shopSyncNameText = "Name: " + shopSyncDto.getName();
                shopSyncNameTextView.setText(shopSyncNameText);

                // set the text view to display the number of users in the shop sync
                String usersText = "Users: " + shopSyncDto.getUserUids().size();
                usersTextView.setText(usersText);

                // set up the button to go to the shop sync activity on click
                goToShopSyncButton.setOnClickListener(v -> {
                    Log.d(TAG, "go to shop sync button clicked for shop sync: " + shopSyncDto);

                    Intent intent = new Intent(context, ShopSyncActivity.class);
                    intent.putExtra(Constants.SHOP_SYNC_UID, shopSyncDto.getUid());
                    context.startActivity(intent);
                });
            }
        }
    }

    private List<ShopSyncDto> shopSyncs;
    private ShopSyncsRecyclerViewAdapter adapter;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if the user is signed in
        FirebaseUser currentUser = checkIfUserIsLoggedInAndFetch(true);
        if (currentUser == null) {
            return;
        }

        Log.d(TAG,
              "onCreate: user signed in with email " + currentUser.getEmail() + " and id (" +
                      currentUser.getUid() + ")");

        setContentView(R.layout.activity_my_shop_syncs);

        // set up the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // set up back button
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });

        // set up refresh button
        Button refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(v -> refresh());

        // set up create shop sync button
        Button createShopSyncButton = findViewById(R.id.buttonCreateShopSync);
        createShopSyncButton.setOnClickListener(v -> {
            Log.d(TAG, "Create shop sync button clicked");
            Intent intent = new Intent(this, CreateShopSyncActivity.class);
            startActivity(intent);
        });

        // set up the recycler view
        shopSyncs = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerViewShopSyncs);
        adapter = new ShopSyncsRecyclerViewAdapter(this, shopSyncs);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        refresh();
    }

    private void refresh() {
        FirebaseUser currentUser = checkIfUserIsLoggedInAndFetch(true);
        if (currentUser == null) {
            return;
        }
        shopSyncs.clear();

        // consumer that for each shop sync uid gets the corresponding ShopSyncModel from the
        // database, then converts the model to a dto and adds the dto to the recycler adapter
        Consumer<List<String>> shopSyncUidsConsumer = shopSyncUids ->
                shopSyncUids.forEach(shopSyncUid -> applicationGraph.shopSyncsService()
                        .getShopSyncWithUid(shopSyncUid)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DataSnapshot dataSnapshot = task.getResult();

                                // if the task was successful but the data snapshot is null,
                                // throw an illegal state exception
                                if (dataSnapshot == null) {
                                    throw new IllegalNullValueException("getShopSyncWithUid: " +
                                                                                "task was " +
                                                                                "successful " +
                                                                                "but data " +
                                                                                "snapshot is" +
                                                                                " null");
                                }

                                // get the shop sync model
                                edu.uga.cs.shopsync.backend.models.ShopSyncModel shopSyncModel =
                                        dataSnapshot.getValue(ShopSyncModel.class);
                                if (shopSyncModel == null) {
                                    throw new IllegalNullValueException("getShopSyncWithUid: " +
                                                                                "task was " +
                                                                                "successful " +
                                                                                "but shop sync " +
                                                                                "model " +
                                                                                "is null");
                                }

                                // convert the shop sync model to a dto
                                ShopSyncDto shopSyncDto = ShopSyncDto.fromModel(shopSyncModel);

                                // consumer that gets the user uids for the shop sync and adds
                                // them to the shop sync dto and then adds the shop sync dto to
                                // the list of shop syncs in the recycler adapter
                                Consumer<List<String>> userUidsConsumer = userUids -> {
                                    shopSyncDto.setUserUids(userUids);
                                    shopSyncs.add(shopSyncDto);
                                    adapter.notifyDataSetChanged();
                                };

                                // get the user uids for the shop sync
                                applicationGraph.shopSyncsService()
                                        .getUsersForShopSync(shopSyncDto.getUid(),
                                                             userUidsConsumer, null);
                            } else {
                                Log.d(TAG,
                                      "onCreate: failed to retrieve shop sync with uid " + shopSyncUid);
                                throw new TaskFailureException(task, "getShopSyncWithUid: failed " +
                                        "to retrieve shop sync with uid " + shopSyncUid);
                            }
                        }));

        // get the shop syncs for the current user
        applicationGraph.shopSyncsService().getShopSyncsForUser(currentUser.getUid(),
                                                                shopSyncUidsConsumer, null);
    }

}
