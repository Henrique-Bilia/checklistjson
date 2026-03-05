package com.example.checklistjson;

public class ChecklistItem {

    private String id;
    private String titulo;
    // "SIM", "NA" ou "" (nenhum marcado)
    private String status;

    public ChecklistItem(String id, String titulo, String status) {
        this.id = id;
        this.titulo = titulo;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSim() {
        return "SIM".equals(status);
    }

    public boolean isNa() {
        return "NA".equals(status);
    }
}

