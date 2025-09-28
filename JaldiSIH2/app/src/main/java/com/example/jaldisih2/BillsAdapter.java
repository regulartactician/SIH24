package com.example.jaldisih2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BillsAdapter extends RecyclerView.Adapter<BillsAdapter.BillViewHolder> {
    private List<BillItem> billItemList;

    public BillsAdapter(List<BillItem> billItemList) {
        this.billItemList = billItemList;
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        BillItem billItem = billItemList.get(position);
        holder.slno.setText(billItem.getSlno());
        holder.serviceName.setText(billItem.getServiceName());
        holder.date.setText(billItem.getDate());
        holder.cost.setText(billItem.getCost());
    }

    @Override
    public int getItemCount() {
        return billItemList.size();
    }

    public static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView slno, serviceName, date, cost;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            slno = itemView.findViewById(R.id.slno);
            serviceName = itemView.findViewById(R.id.serviceName);
            date = itemView.findViewById(R.id.date);
            cost = itemView.findViewById(R.id.cost);
        }
    }
}
