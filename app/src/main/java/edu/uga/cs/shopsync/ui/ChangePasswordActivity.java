package edu.uga.cs.shopsync.ui;

import android.os.Bundle;

import edu.uga.cs.shopsync.ApplicationGraph;
import edu.uga.cs.shopsync.R;

public class ChangePasswordActivity extends BaseActivity {

    private static final String TAG = "ChangePasswordActivity";

    public ChangePasswordActivity() {
        super();
    }

    ChangePasswordActivity(ApplicationGraph applicationGraph) {
        super(applicationGraph);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
    }
}
