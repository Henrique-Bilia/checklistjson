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

public class CabrMenuActivity extends AppCompatActivity {

    private ListView listViewCabr;
    private final List<String> cabrNomes = new ArrayList<>();
    private final List<String> cabrIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cabr_menu);

        TextView tvTitulo = findViewById(R.id.tvCabrTitulo);
        listViewCabr = findViewById(R.id.listViewCabr);
        android.widget.Button btnExportarModeloCabr = findViewById(R.id.btnExportarModeloCabr);

        tvTitulo.setText("Checklists CABR");

        carregarChecklistsCabrDoJson();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                cabrNomes
        );
        listViewCabr.setAdapter(adapter);

        listViewCabr.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                String checklistId = cabrIds.get(position);
                String checklistNome = cabrNomes.get(position);

                Intent intent = new Intent(CabrMenuActivity.this, ChecklistActivity.class);
                intent.putExtra("checklist_id", checklistId);
                intent.putExtra("checklist_nome", checklistNome);
                intent.putStringArrayListExtra("lista_ids", new ArrayList<>(cabrIds));
                intent.putStringArrayListExtra("lista_nomes", new ArrayList<>(cabrNomes));
                intent.putExtra("indice_atual", position);
                startActivity(intent);
            }
        });

        btnExportarModeloCabr.setOnClickListener(v -> {
            Intent intent = new Intent(CabrMenuActivity.this, ModelExportActivity.class);
            intent.putExtra("model_key", "cabr");
            intent.putExtra("model_name", "Modelo CABR");
            startActivity(intent);
        });
    }

    private void carregarChecklistsCabrDoJson() {
        try {
            String json = lerArquivoAssets("checklist.json");
            JSONObject root = new JSONObject(json);
            JSONArray checklistsArray = root.getJSONArray("checklists");

            for (int i = 0; i < checklistsArray.length(); i++) {
                JSONObject checklistObj = checklistsArray.getJSONObject(i);
                String id = checklistObj.getString("id");
                String nome = checklistObj.getString("nome");

                // Convenção: todos os checklists CABR começam com "checklist_cabr_"
                if (id.startsWith("checklist_cabr_")) {
                    cabrIds.add(id);
                    cabrNomes.add(nome);
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

