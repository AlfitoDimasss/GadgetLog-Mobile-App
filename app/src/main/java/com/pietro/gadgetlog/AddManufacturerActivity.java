package com.pietro.gadgetlog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pietro.gadgetlog.model.Manufacturer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class AddManufacturerActivity extends AppCompatActivity {
    //    INSTANTIASI
    private DatabaseReference database;
    private Button btnSubmitManufacturer;
    private EditText etManufacturerName;
    private EditText etManufacturerCountry;
    private TextView tvTitle;
    private ImageView imgManufacturer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_manufacturer);

//        INISIALISASI
        etManufacturerName = findViewById(R.id.etManufacturerName);
        etManufacturerCountry = findViewById(R.id.etManufacturerCountry);
        btnSubmitManufacturer = findViewById(R.id.btnSubmitManufacturer);
        tvTitle = findViewById(R.id.tvTitle);
        imgManufacturer = findViewById(R.id.imgManufacturer);
        database = FirebaseDatabase.getInstance().getReference();

//        GET DATA DEV FROM INTENT IF AVAILABLE
        final Manufacturer manufacturer = (Manufacturer) getIntent().getSerializableExtra("data");

//        SET EVENT VARIABLE
        imgManufacturer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        if(manufacturer != null) {
            tvTitle.setText("Edit Manufacturer Data");
            etManufacturerName.setText(manufacturer.getName());
            etManufacturerCountry.setText(manufacturer.getCountry());
            Glide.with(getApplicationContext()).load(manufacturer.getImg()).into(imgManufacturer);
            btnSubmitManufacturer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    manufacturer.setName(etManufacturerName.getText().toString());
                    manufacturer.setCountry(etManufacturerCountry.getText().toString());
                    uploadEdit(manufacturer);
                }
            });
        } else {
            btnSubmitManufacturer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name = etManufacturerName.getText().toString();
                    String country = etManufacturerCountry.getText().toString();
                    uploadNew(name, country);
                }
            });
        }
    }

    //    SELECT IMAGE FUNCTION
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(AddManufacturerActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 10);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);//
                    startActivityForResult(Intent.createChooser(intent, "Select File"),20);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    //    GET IMAGE CHOOSEN
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 20) {
                Bitmap bm = null;
                if (data != null) {
                    try {
                        bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                imgManufacturer.setImageBitmap(bm);
            } else if (requestCode == 10) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");

                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                imgManufacturer.setImageBitmap(thumbnail);
            }
        }
    }

    //    UPLOAD IMAGE TO STORAGE
    private void uploadNew(String name, String country) {
        imgManufacturer.setDrawingCacheEnabled(true);
        imgManufacturer.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imgManufacturer.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        String newName = name.replaceAll(" ", "").toLowerCase();
        StorageReference reference = storage.getReference("images/manufacturer").child("IMG-"+ newName + "-" + new Date().getTime() + ".jpeg");
        UploadTask uploadTask = reference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if(taskSnapshot.getMetadata().getReference() != null) {
                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.getResult() != null) {
                                submitManufacturer(name, country, task.getResult().toString());
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Gagal", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadEdit(Manufacturer manufacturer) {
        imgManufacturer.setDrawingCacheEnabled(true);
        imgManufacturer.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imgManufacturer.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        String name = manufacturer.getName().replaceAll(" ", "").toLowerCase(Locale.ROOT);
        StorageReference reference = storage.getReference("images/manufacturer").child("IMG-"+ name + "-" + new Date().getTime() + ".jpeg");
        UploadTask uploadTask = reference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if(taskSnapshot.getMetadata().getReference() != null) {
                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.getResult() != null) {
                                FirebaseStorage.getInstance().getReferenceFromUrl(manufacturer.getImg()).delete();
                                manufacturer.setImg(task.getResult().toString());
                                updateManufacturer(manufacturer);
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Gagal", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //    SAVE DEVELOPER DATA TO DB
    private void submitManufacturer(String name, String country, String img) {
        Manufacturer manufacturer = new Manufacturer(name, country, img);
        String id = database.push().getKey();
        manufacturer.setId(id);

        database.child("manufacturer").child(id).setValue(manufacturer).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                etManufacturerName.setText("");
                etManufacturerCountry.setText("");
                imgManufacturer.setImageResource(R.drawable.ic_baseline_image_24);
                Toast.makeText(getApplicationContext(), "Data Berhasil Disiman", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //    UPDATE DEVELOPER DATA TO DB
    private void updateManufacturer(Manufacturer manufacturer) {
        database.child("manufacturer").child(manufacturer.getId()).setValue(manufacturer).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Data Berhasil Diupdate", Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }
}