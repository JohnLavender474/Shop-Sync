package edu.uga.cs.shopsync.frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;
import java.util.Map;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.exceptions.IllegalNullValueException;
import edu.uga.cs.shopsync.backend.models.ShopSyncModel;
import edu.uga.cs.shopsync.frontend.Constants;
import edu.uga.cs.shopsync.utils.UtilMethods;

/**
 * This activity is used to display the result of settling the cost of a shop sync.
 */
public class SettleTheCostActivity extends BaseActivity {

    private static final String TAG = "SettleTheCostActivity";

    private TextView shopSyncNameTextView;
    private TextView shopSyncDescriptionTextView;
    private TableLayout userCostsTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String shopSyncUid = getIntent().getStringExtra(Constants.SHOP_SYNC_UID);
        if (shopSyncUid == null) {
            Log.e(TAG, "onCreate: shopSyncUid is null");
            throw new IllegalNullValueException("shopSyncUid is null");
        }

        String totalCost = getIntent().getStringExtra(Constants.TOTAL_COST);
        if (totalCost == null) {
            Log.e(TAG, "onCreate: totalCost is null");
            throw new IllegalNullValueException("totalCost is null");
        }

        setContentView(R.layout.activity_settle_the_cost);

        shopSyncNameTextView = findViewById(R.id.textViewShopSyncName);
        shopSyncDescriptionTextView = findViewById(R.id.textViewDescription);

        TextView totalCostTextView = findViewById(R.id.textViewTotalCost);
        String totalCostText = "Total Cost: $" + totalCost;
        totalCostTextView.setText(totalCostText);

        TextView averageCostTextView = findViewById(R.id.textViewAverageCost);

        userCostsTable = findViewById(R.id.tableUserCosts);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: backButton clicked");

            Intent intent = new Intent(this, ShopSyncActivity.class);
            intent.putExtra(Constants.SHOP_SYNC_UID, shopSyncUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            finish();
        });

        applicationGraph.shopSyncsService()
                .getShopSyncWithUid(shopSyncUid)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        Log.d(TAG, "getShopSyncWithUid: dataSnapshot = " + dataSnapshot);
                        if (dataSnapshot == null) {
                            Log.e(TAG, "getShopSyncWithUid: dataSnapshot is null");
                            throw new IllegalNullValueException("dataSnapshot is null");
                        }

                        ShopSyncModel shopSync = dataSnapshot.getValue(ShopSyncModel.class);
                        Log.d(TAG, "getShopSyncWithUid: shopSync = " + shopSync);
                        if (shopSync == null) {
                            Log.e(TAG, "getShopSyncWithUid: shopSync is null");
                            throw new IllegalNullValueException("shopSync is null");
                        }

                        shopSyncNameTextView.setText(shopSync.getName());
                        shopSyncDescriptionTextView.setText(shopSync.getDescription());

                        String averageCost = UtilMethods.truncateToDecimalPlaces(
                                Double.parseDouble(totalCost) / shopSync.getShoppingBaskets()
                                        .size(), 2);
                        String averageCostText = "Average Cost: $" + averageCost;
                        averageCostTextView.setText(averageCostText);

                        computeTotalCosts(shopSync);
                    }
                });
    }

    private void computeTotalCosts(ShopSyncModel shopSync) {
        Map<String, Double> userCostsByUid = new HashMap<>();
        shopSync.getPurchasedItems().values().forEach(purchasedItemModel -> {
            String userUid = purchasedItemModel.getBasketItem().getShoppingBasketUid();
            if (userUid == null) {
                Log.e(TAG, "computeTotalCosts: userUid is null");
                throw new IllegalNullValueException("userUid is null");
            }

            if (!userCostsByUid.containsKey(userUid)) {
                userCostsByUid.put(userUid, 0.0);
            }

            Double cost = userCostsByUid.get(userUid);
            if (cost == null) {
                Log.e(TAG, "computeTotalCosts: cost is null");
                throw new IllegalNullValueException("cost is null");
            }

            long quantity = purchasedItemModel.getBasketItem().getQuantity();
            double pricePerUnit = purchasedItemModel.getBasketItem().getPricePerUnit();

            cost += quantity * pricePerUnit;
            userCostsByUid.put(userUid, cost);
        });
        Log.d(TAG, "computeTotalCosts: userCostsByUid = " + userCostsByUid);

        userCostsByUid.forEach((userUid, cost) -> applicationGraph.usersService()
                .getUserProfileWithUid(userUid)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        Log.d(TAG,
                              "computeTotalCosts / getUserProfileWithUid: dataSnapshot = " + dataSnapshot);
                        if (dataSnapshot == null) {
                            Log.e(TAG, "computeTotalCosts / getUserProfileWithUid: dataSnapshot " +
                                    "is null");
                            throw new IllegalNullValueException("dataSnapshot is null");
                        }

                        String username = dataSnapshot.child("username").getValue(String.class);
                        Log.d(TAG,
                              "computeTotalCosts / getUserProfileWithUid: username = " + username);
                        if (username == null) {
                            Log.e(TAG, "computeTotalCosts: username is null");
                            throw new IllegalNullValueException("username is null");
                        }

                        TableRow tableRow = new TableRow(this);
                        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                                TableLayout.LayoutParams.WRAP_CONTENT,
                                TableLayout.LayoutParams.WRAP_CONTENT);
                        userCostsTable.addView(tableRow, tableLayoutParams);

                        TextView userCostTextView = new TextView(this);
                        String userCostText =
                                username + ": \t$" + UtilMethods.truncateToDecimalPlaces(cost, 2);
                        userCostTextView.setText(userCostText);
                        userCostTextView.setTextSize(16);
                        tableRow.addView(userCostTextView);
                    }
                }));

        deleteAllPurchasedItems(shopSync.getUid());
    }

    private void deleteAllPurchasedItems(String shopSyncUid) {
        applicationGraph.shopSyncsService().deleteAllPurchasedItems(shopSyncUid);
    }

}
