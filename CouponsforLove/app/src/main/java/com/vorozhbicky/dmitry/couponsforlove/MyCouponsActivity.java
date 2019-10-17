package com.vorozhbicky.dmitry.couponsforlove;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vorozhbicky.dmitry.couponsforlove.Helpers.MyCouponAdapter;
import com.vorozhbicky.dmitry.couponsforlove.Model.Coupon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyCouponsActivity extends AppCompatActivity {
    private DatabaseReference mRef;
    private MyCouponAdapter myCouponAdapter;
    private ProgressBar spinner;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_coupons);
        spinner = findViewById(R.id.progressBar2);
        mRef = FirebaseDatabase.getInstance().getReference();
        myCouponAdapter = new MyCouponAdapter(getApplicationContext(), this);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        setTitle("My coupons");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final RecyclerView recyclerView = findViewById(R.id.listForMyCoupons);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        spinner.setVisibility(View.VISIBLE);

        String packName = "dima";
        final String email = "arolij.corp@gmail.com";
        if (user != null && Objects.requireNonNull(user.getEmail()).equals(email)) {
            packName = "kate";
        }

        mRef = FirebaseDatabase.getInstance().getReference(packName);

        Query query = mRef;

        query.addValueEventListener(new ValueEventListener() {
            private static final String TAG = "Firebase:";

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Coupon> coupons = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    coupons.add(child.getValue(Coupon.class));
                }
                if (coupons.isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "No coupons listed!",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                myCouponAdapter.setModels(coupons);
                recyclerView.setAdapter(myCouponAdapter);
                Log.d(TAG, "->" + dataSnapshot.toString());
                spinner.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addNewCoupon(View view) {
        Intent intent = new Intent(this, EditCouponActivity.class);
        intent.putExtra("description", "null");
        intent.putExtra("image", "null");
        startActivity(intent);
    }
}