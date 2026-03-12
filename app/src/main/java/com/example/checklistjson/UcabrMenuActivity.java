package com.example.checklistjson;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class UcabrMenuActivity extends AppCompatActivity {

    private ListView listViewUcabr;
    private final List<String> ucabrNomes = new ArrayList<>();
    private final List<String> ucabrIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ucabr_menu);

        TextView tvTitulo = findViewById(R.id.tvUcabrTitulo);
        listViewUcabr = findViewById(R.id.listViewUcabr);
        android.widget.Button btnExportarModeloUcabr = findViewById(R.id.btnExportarModeloUcabr);

        tvTitulo.setText("Checklists UCABR");

        carregarChecklistsUcabrDoJson();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                ucabrNomes
        );
        listViewUcabr.setAdapter(adapter);

        listViewUcabr.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                String checklistId = ucabrIds.get(position);
                String checklistNome = ucabrNomes.get(position);

                Intent intent = new Intent(UcabrMenuActivity.this, ChecklistActivity.class);
                intent.putExtra("checklist_id", checklistId);
                intent.putExtra("checklist_nome", checklistNome);
                intent.putStringArrayListExtra("lista_ids", new ArrayList<>(ucabrIds));
                intent.putStringArrayListExtra("lista_nomes", new ArrayList<>(ucabrNomes));
                intent.putExtra("indice_atual", position);
                startActivity(intent);
            }
        });

        btnExportarModeloUcabr.setOnClickListener(v -> {
            Intent intent = new Intent(UcabrMenuActivity.this, ModelExportActivity.class);
            intent.putExtra("model_key", "ucabr");
            intent.putExtra("model_name", "Modelo UCABR");
            startActivity(intent);
        });
    }

    private void carregarChecklistsUcabrDoJson() {
        try {
            String json = lerArquivoAssets("checklist_ucabr.json");
            JSONObject root = new JSONObject(json);
            JSONArray checklistsArray = root.getJSONArray("checklists");

            for (int i = 0; i < checklistsArray.length(); i++) {
                JSONObject checklistObj = checklistsArray.getJSONObject(i);
                String id = checklistObj.getString("id");
                String nome = checklistObj.getString("nome");

                // Convenção: todos os checklists UCABR começam com "checklist_ucabr_"
                if (id.startsWith("checklist_ucabr_")) {
                    ucabrIds.add(id);
                    ucabrNomes.add(nome);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String lerArquivoAssets(String nomeArquivo) throws Exception {
        AssetManager manager = getAssets();
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
}

