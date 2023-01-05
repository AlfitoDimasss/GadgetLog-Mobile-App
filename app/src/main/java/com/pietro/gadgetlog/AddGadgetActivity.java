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
import com.pietro.gadgetlog.model.Gadget;
import com.pietro.gadgetlog.model.Laptop;
import com.pietro.gadgetlog.model.Phone;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class AddGadgetActivity extends AppCompatActivity {
    //    INSTANTIASI
    private DatabaseReference database;
    private Button btnSubmitGadget;
    private EditText etGadgetName;
    private EditText etGadgetBrand;
    private EditText etGadgetPrice;
    private TextView tvTitle;
    private ImageView imgGadget;
    String manufacturerId, manufacturerName;
    String gadgetType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gadget);

//        INISIALISASI
        etGadgetName = findViewById(R.id.etGadgetName);
        etGadgetBrand = findViewById(R.id.etGadgetBrand);
        btnSubmitGadget = findViewById(R.id.btnSubmitGadget);
        imgGadget = findViewById(R.id.imgGadget);
        manufacturerId = getIntent().getStringExtra("id");
        gadgetType = getIntent().getStringExtra("type");
        manufacturerName = getIntent().getStringExtra("name");
        tvTitle = findViewById(R.id.tvTitle);
        etGadgetPrice = findViewById(R.id.etGadgetPrice);

        database = FirebaseDatabase.getInstance().getReference("gadget").child(manufacturerId).child(gadgetType);
        Gadget gadget;

        if(gadgetType.equals("phone")) {
            gadget = (Phone) getIntent().getSerializableExtra("data");
        } else {
            gadget = (Laptop) getIntent().getSerializableExtra("data");
        }

        tvTitle.setText("Add " + gadgetType + " Data");
        etGadgetBrand.setText(manufacturerName);

        if(gadget != null) {
            tvTitle.setText("Edit " + gadgetType + " Data");
            etGadgetName.setText(gadget.getName());
            etGadgetBrand.setText(gadget.getBrand());
            etGadgetPrice.setText(gadget.getPrice());
            Glide.with(getApplicationContext()).load(gadget.getImg()).into(imgGadget);
            btnSubmitGadget.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gadget.setName(etGadgetName.getText().toString());
                    gadget.setBrand(etGadgetBrand.getText().toString());
                    gadget.setPrice(etGadgetPrice.getText().toString());
                    uploadEdit(gadget);
                }
            });
        } else {
            btnSubmitGadget.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name = etGadgetName.getText().toString();
                    String brand = etGadgetBrand.getText().toString();
                    String price = etGadgetPrice.getText().toString();
                    upload(name, brand, price);
                }
            });
        }

//        SET VARIABLE EVENT
        imgGadget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
    }

    //    SELECT IMAGE FUNCTION
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(AddGadgetActivity.this);
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
                imgGadget.setImageBitmap(bm);
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

                imgGadget.setImageBitmap(thumbnail);
            }
        }
    }

    //    UPLOAD IMAGE TO STORAGE
    private void upload(String name, String brand, String price) {
        imgGadget.setDrawingCacheEnabled(true);
        imgGadget.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imgGadget.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        String newName = name.replaceAll(" ", "").toLowerCase();
        StorageReference reference = storage.getReference("images/gadget").child(manufacturerId).child(gadgetType).child("IMG-"+ newName + "-" + new Date().getTime() + ".jpeg");
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
                                submitGadget(name, brand, task.getResult().toString(), price);
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Gagal", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadEdit(Gadget gadget) {
        imgGadget.setDrawingCacheEnabled(true);
        imgGadget.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imgGadget.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        String name = gadget.getName().replaceAll(" ", "");
        StorageReference reference = storage.getReference("images/gadget").child(manufacturerId).child(gadgetType).child("IMG-"+ name + "-" + new Date().getTime() + ".jpeg");
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
                                FirebaseStorage.getInstance().getReferenceFromUrl(gadget.getImg()).delete();
                                gadget.setImg(task.getResult().toString());
                                updateGadget(gadget);
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Gagal", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //    SAVE GAME TO DB
    private void submitGadget(String name, String genre, String img, String price) {
        Gadget gadget;
        if(gadgetType.equals("phone")) {
            gadget = new Phone(name, genre, img, price);
        } else {
            gadget = new Laptop(name, genre, img, price);
        }
        String id = database.push().getKey();
        gadget.setId(id);

        database.child(id).setValue(gadget).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                etGadgetName.setText("");
                etGadgetBrand.setText("");
                etGadgetPrice.setText("");
                imgGadget.setImageResource(R.drawable.ic_baseline_image_24);
                Toast.makeText(getApplicationContext(), "Data Berhasil Disimpan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGadget(Gadget gadget) {
        database.child(gadget.getId()).setValue(gadget).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Data Berhasil Diubah", Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }
}