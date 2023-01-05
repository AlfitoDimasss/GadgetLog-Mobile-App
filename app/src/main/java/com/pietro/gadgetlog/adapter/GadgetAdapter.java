package com.pietro.gadgetlog.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.pietro.gadgetlog.AddGadgetActivity;
import com.pietro.gadgetlog.model.Gadget;
import com.pietro.gadgetlog.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class GadgetAdapter extends RecyclerView.Adapter<GadgetAdapter.GadgetViewHolder> {
    private Context context;
    private List<Gadget> gadgetList;
    private String manufacturerId;
    private String gadgetType;

    public GadgetAdapter(Context context, List<Gadget> gadgetList, String manufacturerId, String gadgetType) {
        this.context = context;
        this.gadgetList = gadgetList;
        this.manufacturerId = manufacturerId;
        this.gadgetType = gadgetType;
    }

    @NonNull
    @Override
    public GadgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_gadget, parent, false);
        return new GadgetViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GadgetViewHolder holder, int position) {
        holder.tvGadgetName.setText(gadgetList.get(position).getName());
        Glide.with(context).load(gadgetList.get(position).getImg()).into(holder.imgGadget);
        if(gadgetList.get(position).getPrice() != null) {
            double price = Double.parseDouble(gadgetList.get(position).getPrice());
            NumberFormat kursId = NumberFormat.getCurrencyInstance(Locale.ITALY);
            String newPrice = kursId.format(price);
            newPrice = newPrice.substring(0, newPrice.length()-5);
            holder.tvGadgetPrice.setText("IDR "+ newPrice);
        }

        holder.cvGadget.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final CharSequence[] items = { "Edit", "Delete", "Cancel" };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals("Edit")) {
                            Intent intent = new Intent(context, AddGadgetActivity.class);
                            intent.putExtra("data", gadgetList.get(holder.getAdapterPosition()));
                            intent.putExtra("id", manufacturerId);
                            intent.putExtra("type", gadgetType);
                            context.startActivity(intent);
                        } else if (items[item].equals("Delete")) {
                            delete(gadgetList.get(holder.getAdapterPosition()));
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
        return gadgetList.size();
    }
    private void delete(Gadget gadget) {
        FirebaseStorage.getInstance().getReferenceFromUrl(gadget.getImg()).delete();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("gadget").child(manufacturerId).child(gadgetType);
        database.child(gadget.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Data Berhasil Dihapus", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class GadgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvGadgetName;
        TextView tvGadgetPrice;
        ImageView imgGadget;
        CardView cvGadget;
        public GadgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGadgetName = itemView.findViewById(R.id.tvGadgetName);
            imgGadget = itemView.findViewById(R.id.imgGadget);
            cvGadget = itemView.findViewById(R.id.cvGadget);
            tvGadgetPrice = itemView.findViewById(R.id.tvGadgetPrice);
        }
    }
}

