package com.example.checklistjson;

public class ChecklistItem {

    private String id;
    private String titulo;
    private boolean checked;

    public ChecklistItem(String id, String titulo, boolean checked) {
        this.id = id;
        this.titulo = titulo;
        this.checked = checked;
    }

    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}

