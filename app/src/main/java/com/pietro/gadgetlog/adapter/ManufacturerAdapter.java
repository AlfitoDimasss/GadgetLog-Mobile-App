package com.pietro.gadgetlog.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.pietro.gadgetlog.AddManufacturerActivity;
import com.pietro.gadgetlog.ListGadgetActivity;
import com.pietro.gadgetlog.model.Manufacturer;
import com.pietro.gadgetlog.R;

import java.util.List;

public class ManufacturerAdapter extends RecyclerView.Adapter<ManufacturerAdapter.ManufacturerViewHolder> {
    private Context context;
    private List<Manufacturer> manufacturerList;

    public ManufacturerAdapter(Context context, List<Manufacturer> manufacturerList) {
        this.context = context;
        this.manufacturerList = manufacturerList;
    }

    @NonNull
    @Override
    public ManufacturerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_manufacturer, parent, false);
        return new ManufacturerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ManufacturerViewHolder holder, int position) {
        holder.tvManufacturerName.setText(manufacturerList.get(position).getName());
        if(manufacturerList.get(position).getImg() != null) {
            Glide.with(context).load(manufacturerList.get(position).getImg()).into(holder.imgManufacturer);
        }

        holder.cvManufacturer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ListGadgetActivity.class);
                intent.putExtra("data", manufacturerList.get(holder.getAdapterPosition()));
                context.startActivity(intent);
            }
        });

        holder.cvManufacturer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final CharSequence[] items = { "Edit", "Delete", "Cancel" };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals("Edit")) {
                            Intent intent = new Intent(context, AddManufacturerActivity.class);
                            intent.putExtra("data", manufacturerList.get(holder.getAdapterPosition()));
                            context.startActivity(intent);
                        } else if (items[item].equals("Delete")) {
                            delete(manufacturerList.get(holder.getAdapterPosition()));
                        } else if (items[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return manufacturerList.size();
    }

    private void delete(Manufacturer manufacturer) {
        FirebaseStorage.getInstance().getReferenceFromUrl(manufacturer.getImg()).delete();
        FirebaseDatabase.getInstance().getReference("gadget").child(manufacturer.getId()).removeValue();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("manufacturer");
        database.child(manufacturer.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Data Berhasil Dihapus", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class ManufacturerViewHolder extends RecyclerView.ViewHolder {
        TextView tvManufacturerName;
        ImageView imgManufacturer;
        CardView cvManufacturer;
        public ManufacturerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvManufacturerName = itemView.findViewById(R.id.tvManufacturerName);
            imgManufacturer = itemView.findViewById(R.id.imgManufacturer);
            cvManufacturer = itemView.findViewById(R.id.cvManufacturer);
        }
    }
}

