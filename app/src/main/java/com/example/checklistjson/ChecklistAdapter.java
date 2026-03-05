package com.example.checklistjson;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

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

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setText(item.getTitulo());
        holder.checkBox.setChecked(item.isChecked());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);
            salvarEstadoNoSharedPreferences(item.getId(), isChecked);
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

    private void salvarEstadoNoSharedPreferences(String itemId, boolean checked) {
        SharedPreferences prefs = context.getSharedPreferences("checklists_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String chave = ChecklistActivity.gerarChavePref(checklistId, itemId);
        editor.putBoolean(chave, checked);
        editor.apply();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBoxItem);
        }
    }
}

