package com.vorozhbicky.dmitry.couponsforlove;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vorozhbicky.dmitry.couponsforlove.Helpers.CouponAdapter;
import com.vorozhbicky.dmitry.couponsforlove.Model.Coupon;
import com.vorozhbicky.dmitry.couponsforlove.Model.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CouponsActivity extends AppCompatActivity {
    private static final String TAG = "FIREBASE";
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private CouponAdapter couponAdapter;
    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupons);
        spinner = findViewById(R.id.progressBar1);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        couponAdapter = new CouponAdapter(getApplicationContext());

        if (user != null) {
            mRef = FirebaseDatabase.getInstance().getReference(user.getUid());
        }

        final RecyclerView recyclerView = findViewById(R.id.listForCoupons);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        spinner.setVisibility(View.VISIBLE);

        Query query = mRef.child("userInfo");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                com.vorozhbicky.dmitry.couponsforlove.Model.UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                Log.d(TAG, "->" + dataSnapshot.toString());
                Query query = FirebaseDatabase.getInstance().getReference(Objects.requireNonNull(userInfo).pairUniqId).child("coupons");
                query.addValueEventListener(new ValueEventListener() {
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
                        couponAdapter.setModels(coupons);
                        recyclerView.setAdapter(couponAdapter);
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.my_coupons_settings:
                intent = new Intent(getApplicationContext(), MyCouponsActivity.class);
                startActivity(intent);
                return true;
            case R.id.about_settings:
                intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(intent);
                return true;
            case R.id.sign_out_settings:
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(CouponsActivity.this);
                builder.setTitle("Sign Out")
                        .setMessage("Are you sure you want to sign out?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "signOutWithEmail:success");
                                mAuth.signOut();
                                Intent intent = new Intent(CouponsActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
    }
}
