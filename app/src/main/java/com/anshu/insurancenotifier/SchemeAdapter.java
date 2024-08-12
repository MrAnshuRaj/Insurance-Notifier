package com.anshu.insurancenotifier;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SchemeAdapter extends RecyclerView.Adapter<SchemeAdapter.SchemeViewHolder> {

    private List<Scheme> schemeList;

    public SchemeAdapter(List<Scheme> schemeList) {
        this.schemeList = schemeList;
    }

    @NonNull
    @Override
    public SchemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scheme, parent, false);
        return new SchemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SchemeViewHolder holder, int position) {
        Scheme scheme = schemeList.get(position);
        holder.schemeName.setText(scheme.getSchemeName());
        holder.renewalDate.setText("Renewal Date: " + scheme.getRenewalDate());
        holder.daysRemaining.setText("Days Remaining: " + scheme.getDaysRemaining());
    }

    @Override
    public int getItemCount() {
        return schemeList.size();
    }

    public static class SchemeViewHolder extends RecyclerView.ViewHolder {
        TextView schemeName, renewalDate, daysRemaining;

        public SchemeViewHolder(@NonNull View itemView) {
            super(itemView);
            schemeName = itemView.findViewById(R.id.schemeName);
            renewalDate = itemView.findViewById(R.id.renewalDate);
            daysRemaining = itemView.findViewById(R.id.daysRemaining);
        }
    }
}
