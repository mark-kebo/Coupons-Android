package com.vorozhbicky.dmitry.couponsforlove.Helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.vorozhbicky.dmitry.couponsforlove.EditCouponActivity;
import com.vorozhbicky.dmitry.couponsforlove.Model.Coupon;
import com.vorozhbicky.dmitry.couponsforlove.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static android.support.constraint.Constraints.TAG;

public class MyCouponAdapter extends RecyclerView.Adapter<MyCouponAdapter.ViewHolder> {

    private List<Coupon> couponList;
    private Context context;
    private Activity activity;
    private DatabaseReference mRef;
    private FirebaseUser user;

    public MyCouponAdapter(Context context, Activity activity) {
        this.activity = activity;
        this.context = context;
    }

    public void setModels(List<Coupon> couponList) {
        this.couponList = couponList;
        user = FirebaseAuth.getInstance().getCurrentUser();

        mRef = FirebaseDatabase.getInstance().getReference(user.getUid()).child("coupons");
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyCouponAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View couponImageView;
        couponImageView = inflater.inflate(R.layout.item_my_coupon, parent, false);
        return new MyCouponAdapter.ViewHolder(couponImageView, context);
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(@NonNull final MyCouponAdapter.ViewHolder holder, final int position) {
        final Coupon coupon = couponList.get(position);
        holder.name.setText("Coupon #" + (position + 1));
        holder.description.setText(coupon.description);
        if (!coupon.image.equals("") && !coupon.image.equals("null")) {
            Picasso.with(context).load(coupon.image).error(R.drawable.heart).into(holder.image);
        }
        holder.buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditCouponActivity.class);
                intent.putExtra("description", coupon.description);
                intent.putExtra("image", coupon.image);
                activity.startActivity(intent);
            }
        });
        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteElement(coupon);
            }
        });

    }


    private void deleteElement(final Coupon coupon) {
        Query query = mRef;
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Coupon coup = child.getValue(Coupon.class);
                    if (coup != null && coupon.image.equals(coup.image) &&
                            coupon.description.equals(coup.description)) {
                        mRef.child(Objects.requireNonNull(child.getKey())).removeValue();
                        Log.d(TAG, "Deleted: " + child.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return couponList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        TextView name, description;
        Button buttonEdit, buttonDelete;
        ImageView image;

        ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            name = itemView.findViewById(R.id.wordText2);
            description = itemView.findViewById(R.id.descriptionText);
            image = itemView.findViewById(R.id.imageViewCoup2);
        }
    }
}