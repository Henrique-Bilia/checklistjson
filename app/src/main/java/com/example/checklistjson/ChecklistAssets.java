package com.example.checklistjson;

public class ChecklistAssets {

    /**
     * Retorna o arquivo de assets que contém a definição
     * do checklist baseado no seu id completo.
     */
    public static String forChecklistId(String checklistId) {
        if (checklistId == null) {
            return "checklist.json";
        }

        if (checklistId.startsWith("checklist_irbr_")) {
            return "checklist_irbr.json";
        } else if (checklistId.startsWith("checklist_ucabr_")) {
            return "checklist_ucabr.json";
        } else if (checklistId.startsWith("checklist_edbrse_")) {
            return "checklist_edbrse.json";
        } else if (checklistId.startsWith("checklist_esbrag_")) {
            return "checklist_esbrag.json";
        } else if (checklistId.startsWith("checklist_cabr_")) {
            return "checklist_cabr.json";
        } else {
            // fallback para o exemplo genérico
            return "checklist.json";
        }
    }

    /**
     * Retorna o arquivo de assets correspondente a uma chave de modelo
     * (ucabr, irbr, edbrse, esbrag, cabr, manutencao, etc).
     */
    public static String forModelKey(String modelKey) {
        if ("irbr".equals(modelKey)) {
            return "checklist_irbr.json";
        } else if ("ucabr".equals(modelKey)) {
            return "checklist_ucabr.json";
        } else if ("edbrse".equals(modelKey)) {
            return "checklist_edbrse.json";
        } else if ("esbrag".equals(modelKey)) {
            return "checklist_esbrag.json";
        } else if ("cabr".equals(modelKey)) {
            return "checklist_cabr.json";
        } else if ("manutencao".equals(modelKey)) {
            return "checklist.json";
        } else {
            // fallback genérico
            return "checklist.json";
        }
    }
}

