package com.pietro.gadgetlog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pietro.gadgetlog.adapter.ManufacturerAdapter;
import com.pietro.gadgetlog.model.Manufacturer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //    INSTANTIASI
    private FloatingActionButton btnAddManufacturer;
    private DatabaseReference database;
    private List<Manufacturer> manufacturerList;
    private RecyclerView rvManufacturer;
    private ManufacturerAdapter adapter;
    private Button buttonLogout;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        INISIALISASI
        btnAddManufacturer = findViewById(R.id.btnAddManufacturer);
        rvManufacturer = findViewById(R.id.rvManufacturer);
        database = FirebaseDatabase.getInstance().getReference();
        manufacturerList = new ArrayList<>();
        buttonLogout = findViewById(R.id.buttonLogout);

//        SET VARIABLE
        rvManufacturer.setLayoutManager(new GridLayoutManager(this, 2));

        btnAddManufacturer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddManufacturerActivity.class);
                startActivity(intent);
            }
        });

        buttonLogout.setOnClickListener(v ->{
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

//        GET DATA FROM DB
        database.child("manufacturer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                manufacturerList.clear();
                if(snapshot.exists()) {
                    for(DataSnapshot data : snapshot.getChildren()) {
                        Manufacturer dev = data.getValue(Manufacturer.class);
                        manufacturerList.add(dev);
                    }
                }
                adapter = new ManufacturerAdapter(MainActivity.this, manufacturerList);
                rvManufacturer.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}