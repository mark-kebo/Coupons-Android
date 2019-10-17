package com.vorozhbicky.dmitry.couponsforlove;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.vorozhbicky.dmitry.couponsforlove.Model.Coupon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static android.support.constraint.Constraints.TAG;

public class EditCouponActivity extends AppCompatActivity {
    static final int RESULT_LOAD_IMAGE = 1;

    private DatabaseReference mRef;
    private StorageReference mStorageRef;
    private FirebaseUser user;
    private EditText description;
    private ImageView imageView;
    private ProgressBar spinner;


    private boolean isNew;

    private Coupon coupon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_coupon);
        setTitle("Edit coupon");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        imageView = findViewById(R.id.imageCouponEdit);
        description = findViewById(R.id.editTextCouponEdit);
        spinner = findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
            }
        });

        mStorageRef = FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        String packName = "dima";
        final String email = "arolij.corp@gmail.com";
        if (user != null && Objects.requireNonNull(user.getEmail()).equals(email)) {
            packName = "kate";
        }
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mRef = FirebaseDatabase.getInstance().getReference(packName);
        Intent intent = getIntent();
        coupon = new Coupon(intent.getStringExtra("description"),
                intent.getStringExtra("image"));
        if (coupon.description.equals("null") && coupon.image.equals("null")) {
            isNew = true;
            description.setText("");
        } else {
            isNew = false;
            description.setText(coupon.description);
            if (!coupon.image.equals("") && !coupon.image.equals("null")) {
                Picasso.with(this).load(coupon.image).into(imageView);
            }
        }
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

    private void editElement() {
        Query query = mRef;
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Coupon coup = child.getValue(Coupon.class);
                    if (coup != null && coupon.description.equals(coup.description)) {
                        mRef.child(Objects.requireNonNull(child.getKey())).
                                child("description").setValue(description.getText().toString());
                        mRef.child(Objects.requireNonNull(child.getKey())).
                                child("image").setValue(coupon.image);
                        Log.d(TAG, "->" + coupon.image + " - " + description.getText().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });
    }

    public void saveEdit(View view) {
        if (isNew) {
            Coupon newCoupon = new Coupon(description.getText().toString(), coupon.image);
            mRef.push().setValue(newCoupon);
        } else {
            editElement();
        }
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        Bitmap bitmap = null;

        switch (requestCode) {
            case RESULT_LOAD_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = result.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (bitmap != null) {
                        uploadFile(bitmap);
                        imageView.setImageBitmap(bitmap);
                    }
                }
        }
    }

    private void uploadFile(Bitmap bitmap) {
        spinner.setVisibility(View.VISIBLE);
        final StorageReference imagesRef = mStorageRef.child("img/" + generateString() + ".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = imagesRef.putBytes(data);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return imagesRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    Log.d(TAG, "->" + downloadUri);
                    coupon.image = String.valueOf(downloadUri);
                    spinner.setVisibility(View.GONE);
                }
            }
        });
    }

    public static String generateString() {
        return UUID.randomUUID().toString();
    }
}
