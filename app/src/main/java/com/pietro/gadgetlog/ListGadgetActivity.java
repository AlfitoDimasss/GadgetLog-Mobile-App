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
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pietro.gadgetlog.adapter.GadgetAdapter;
import com.pietro.gadgetlog.model.Gadget;
import com.pietro.gadgetlog.model.Laptop;
import com.pietro.gadgetlog.model.Manufacturer;
import com.pietro.gadgetlog.model.Phone;

import java.util.ArrayList;
import java.util.List;

public class ListGadgetActivity extends AppCompatActivity {
    private TextView tvTitle;
    private DatabaseReference database;
    private List<Gadget> gadgetList;
    private RecyclerView rvGadget;
    private GadgetAdapter adapter;
    private FloatingActionButton btnAddGadget;
    private Button btnPhone, btnLaptop;
    String gadgetType, manufacturerId, manufacturerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_gadget);

        btnAddGadget = findViewById(R.id.btnAddGadget);
        tvTitle = findViewById(R.id.tvTitle);
        rvGadget = findViewById(R.id.rvGadget);
        btnPhone = findViewById(R.id.btnPhone);
        btnLaptop = findViewById(R.id.btnLaptop);
        gadgetList = new ArrayList<>();

        rvGadget.setLayoutManager(new GridLayoutManager(this, 2));

        Manufacturer manufacturer = (Manufacturer) getIntent().getSerializableExtra("data");
        manufacturerId = manufacturer.getId();
        manufacturerName = manufacturer.getName();
        gadgetType = "phone";

        tvTitle.setText(manufacturer.getName() + " Gadget Catalog");
        database = FirebaseDatabase.getInstance().getReference("gadget").child(manufacturerId);

        database.child(gadgetType).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gadgetList.clear();
                if(snapshot.exists()) {
                    for(DataSnapshot data : snapshot.getChildren()) {
                        Phone phone = data.getValue(Phone.class);
                        gadgetList.add(phone);
                    }
                }
                adapter = new GadgetAdapter(ListGadgetActivity.this, gadgetList, manufacturerId, gadgetType);
                rvGadget.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnAddGadget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListGadgetActivity.this, AddGadgetActivity.class);
                intent.putExtra("id", manufacturerId);
                intent.putExtra("type", gadgetType);
                intent.putExtra("name", manufacturerName);
                startActivity(intent);
            }
        });

        btnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPhone.setBackgroundColor(getResources().getColor(R.color.white));
                btnPhone.setTextColor(getResources().getColor(R.color.purple_500));
                btnLaptop.setBackgroundColor(getResources().getColor(R.color.purple_500));
                btnLaptop.setTextColor(getResources().getColor(R.color.white));
                gadgetType = "phone";
                database.child(gadgetType).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        gadgetList.clear();
                        if(snapshot.exists()) {
                            for(DataSnapshot data : snapshot.getChildren()) {
                                Phone phone = data.getValue(Phone.class);
                                gadgetList.add(phone);
                            }
                        }
                        adapter = new GadgetAdapter(ListGadgetActivity.this, gadgetList, manufacturerId, gadgetType);
                        rvGadget.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        btnLaptop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnLaptop.setBackgroundColor(getResources().getColor(R.color.white));
                btnLaptop.setTextColor(getResources().getColor(R.color.purple_500));
                btnPhone.setBackgroundColor(getResources().getColor(R.color.purple_500));
                btnPhone.setTextColor(getResources().getColor(R.color.white));
                gadgetType = "laptop";
                database.child(gadgetType).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        gadgetList.clear();
                        if(snapshot.exists()) {
                            for(DataSnapshot data : snapshot.getChildren()) {
                                Laptop laptop = data.getValue(Laptop.class);
                                gadgetList.add(laptop);
                            }
                        }
                        adapter = new GadgetAdapter(ListGadgetActivity.this, gadgetList, manufacturerId, gadgetType);
                        rvGadget.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }
}