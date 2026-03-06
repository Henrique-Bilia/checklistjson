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

public class EdbrseMenuActivity extends AppCompatActivity {

    private ListView listViewEdbrse;
    private final List<String> edbrseNomes = new ArrayList<>();
    private final List<String> edbrseIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edbrse_menu);

        TextView tvTitulo = findViewById(R.id.tvEdbrseTitulo);
        listViewEdbrse = findViewById(R.id.listViewEdbrse);
        android.widget.Button btnExportarModeloEdbrse = findViewById(R.id.btnExportarModeloEdbrse);

        tvTitulo.setText("Checklists EDBRSE/EUBRSE");

        carregarChecklistsEdbrseDoJson();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                edbrseNomes
        );
        listViewEdbrse.setAdapter(adapter);

        listViewEdbrse.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                String checklistId = edbrseIds.get(position);
                String checklistNome = edbrseNomes.get(position);

                Intent intent = new Intent(EdbrseMenuActivity.this, ChecklistActivity.class);
                intent.putExtra("checklist_id", checklistId);
                intent.putExtra("checklist_nome", checklistNome);
                intent.putStringArrayListExtra("lista_ids", new ArrayList<>(edbrseIds));
                intent.putStringArrayListExtra("lista_nomes", new ArrayList<>(edbrseNomes));
                intent.putExtra("indice_atual", position);
                startActivity(intent);
            }
        });

        btnExportarModeloEdbrse.setOnClickListener(v -> {
            Intent intent = new Intent(EdbrseMenuActivity.this, ModelExportActivity.class);
            intent.putExtra("model_key", "edbrse");
            intent.putExtra("model_name", "Modelo EDBRSE/EUBRSE");
            startActivity(intent);
        });
    }

    private void carregarChecklistsEdbrseDoJson() {
        try {
            String json = lerArquivoAssets("checklist.json");
            JSONObject root = new JSONObject(json);
            JSONArray checklistsArray = root.getJSONArray("checklists");

            for (int i = 0; i < checklistsArray.length(); i++) {
                JSONObject checklistObj = checklistsArray.getJSONObject(i);
                String id = checklistObj.getString("id");
                String nome = checklistObj.getString("nome");

                // Convenção: todos os checklists EDBRSE/EUBRSE começam com "checklist_edbrse_"
                if (id.startsWith("checklist_edbrse_")) {
                    edbrseIds.add(id);
                    edbrseNomes.add(nome);
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

