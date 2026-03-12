package com.example.checklistjson;

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Camada de acesso aos dados de checklist armazenados em arquivos JSON.
 *
 * Responsável por:
 * - Ler arquivos de assets corretos para cada modelo/checklist.
 * - Localizar o checklist específico dentro do JSON.
 */
public class ChecklistRepository {

    private static String lerArquivoAssets(Context context, String nomeArquivo) throws Exception {
        AssetManager manager = context.getAssets();
        InputStream is = manager.open(nomeArquivo);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String linha;
        while ((linha = br.readLine()) != null) {
            sb.append(linha);
        }
        br.close();
        return sb.toString();
    }

    /**
     * Retorna o objeto JSON do checklist correspondente ao checklistId informado,
     * ou null se não for encontrado.
     */
    public static JSONObject getChecklistById(Context context, String checklistId) throws Exception {
        String json = lerArquivoAssets(context, ChecklistAssets.forChecklistId(checklistId));
        JSONObject root = new JSONObject(json);
        JSONArray checklistsArray = root.getJSONArray("checklists");

        for (int i = 0; i < checklistsArray.length(); i++) {
            JSONObject obj = checklistsArray.getJSONObject(i);
            if (obj.getString("id").equals(checklistId)) {
                return obj;
            }
        }

        return null;
    }

    /**
     * Retorna o array de checklists para um determinado modelo (modelKey).
     * A responsabilidade de filtrar por prefixo/id permanece na camada de uso.
     */
    public static JSONArray getChecklistsArrayForModel(Context context, String modelKey) throws Exception {
        String json = lerArquivoAssets(context, ChecklistAssets.forModelKey(modelKey));
        JSONObject root = new JSONObject(json);
        return root.getJSONArray("checklists");
    }
}

