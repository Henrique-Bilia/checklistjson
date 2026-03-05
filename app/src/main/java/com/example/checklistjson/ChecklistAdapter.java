package com.example.checklistjson;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ViewHolder> {

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, ChecklistItem item);
    }

    private final List<ChecklistItem> itens;
    private final String checklistId;
    private final Context context;
    private final OnItemLongClickListener longClickListener;

    public ChecklistAdapter(List<ChecklistItem> itens, String checklistId, Context context, OnItemLongClickListener longClickListener) {
        this.itens = itens;
        this.checklistId = checklistId;
        this.context = context;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checklist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChecklistItem item = itens.get(position);

        holder.tvTitulo.setText(item.getTitulo());

        // Evita loops de evento
        holder.rbSim.setOnCheckedChangeListener(null);
        holder.rbNa.setOnCheckedChangeListener(null);

        holder.rbSim.setChecked(item.isSim());
        holder.rbNa.setChecked(item.isNa());

        holder.rbSim.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                item.setStatus("SIM");
                holder.rbNa.setChecked(false);
                salvarStatusNoSharedPreferences(item.getId(), "SIM");
            } else if (!holder.rbNa.isChecked()) {
                item.setStatus("");
                salvarStatusNoSharedPreferences(item.getId(), "");
            }
        });

        holder.rbNa.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                item.setStatus("NA");
                holder.rbSim.setChecked(false);
                salvarStatusNoSharedPreferences(item.getId(), "NA");
            } else if (!holder.rbSim.isChecked()) {
                item.setStatus("");
                salvarStatusNoSharedPreferences(item.getId(), "");
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(holder.getAdapterPosition(), item);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    private void salvarStatusNoSharedPreferences(String itemId, String status) {
        SharedPreferences prefs = context.getSharedPreferences("checklists_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String chave = ChecklistActivity.gerarChaveStatus(checklistId, itemId);
        editor.putString(chave, status);
        editor.apply();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        RadioButton rbSim;
        RadioButton rbNa;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvItemTitulo);
            rbSim = itemView.findViewById(R.id.rbSim);
            rbNa = itemView.findViewById(R.id.rbNa);
        }
    }
}

