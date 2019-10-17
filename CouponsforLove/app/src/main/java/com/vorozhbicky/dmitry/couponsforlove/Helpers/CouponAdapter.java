package com.vorozhbicky.dmitry.couponsforlove.Helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.vorozhbicky.dmitry.couponsforlove.Model.Coupon;
import com.vorozhbicky.dmitry.couponsforlove.R;

import java.util.List;
import java.util.Objects;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.ViewHolder>{

    private List<Coupon> couponList;
    private Context context;

    public CouponAdapter(Context context) {
        this.context = context;
    }

    public void setModels(List<Coupon> couponList) {
        this.couponList = couponList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View couponView;
        couponView = inflater.inflate(R.layout.item_coupon, parent, false);
        return new ViewHolder(couponView, context);
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Coupon coupon = couponList.get(position);
        holder.name.setText("Coupon #" + (position + 1));
        holder.description.setText(coupon.description);
        if (!coupon.image.equals("") && !coupon.image.equals("null")) {
            Picasso.with(context).load(coupon.image).error(R.drawable.heart).into(holder.image);
        }
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail("Used coupon: " + coupon.description);
            }
        });
    }

    private void sendEmail(String message) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = "arolij.corp@gmail.com";
        if (user != null && Objects.requireNonNull(user.getEmail()).equals(email)) {
            email = "keffinoel@gmail.com";
        }
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Request for Execution <3");
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(emailIntent);
    }

    @Override
    public int getItemCount() {
        return couponList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        TextView name, description;
        ImageView image;
        Button button;

        ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            button = itemView.findViewById(R.id.buttonUseCoupon);
            name = itemView.findViewById(R.id.wordText);
            description = itemView.findViewById(R.id.wordTextDef);
            image = itemView.findViewById(R.id.imageViewCoup);
        }
    }
}