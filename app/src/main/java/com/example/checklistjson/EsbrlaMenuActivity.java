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

public class EsbrlaMenuActivity extends AppCompatActivity {

    private ListView listViewEsbrla;
    private final List<String> esbrlaNomes = new ArrayList<>();
    private final List<String> esbrlaIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esbrla_menu);

        TextView tvTitulo = findViewById(R.id.tvEsbrlaTitulo);
        listViewEsbrla = findViewById(R.id.listViewEsbrla);
        android.widget.Button btnExportarModeloEsbrla = findViewById(R.id.btnExportarModeloEsbrla);
        android.widget.Button btnImportarOcrEsbrla = findViewById(R.id.btnImportarOcrEsbrla);

        tvTitulo.setText("Checklists ESBR-ESBRH-ESLA");

        carregarChecklistsEsbrlaDoJson();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                esbrlaNomes
        );
        listViewEsbrla.setAdapter(adapter);

        listViewEsbrla.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                String checklistId = esbrlaIds.get(position);
                String checklistNome = esbrlaNomes.get(position);

                Intent intent = new Intent(EsbrlaMenuActivity.this, ChecklistActivity.class);
                intent.putExtra("checklist_id", checklistId);
                intent.putExtra("checklist_nome", checklistNome);
                intent.putStringArrayListExtra("lista_ids", new ArrayList<>(esbrlaIds));
                intent.putStringArrayListExtra("lista_nomes", new ArrayList<>(esbrlaNomes));
                intent.putExtra("indice_atual", position);
                startActivity(intent);
            }
        });

        btnExportarModeloEsbrla.setOnClickListener(v -> {
            Intent intent = new Intent(EsbrlaMenuActivity.this, ModelExportActivity.class);
            intent.putExtra("model_key", "esbrla");
            intent.putExtra("model_name", "Modelo ESBR-ESBRH-ESLA");
            startActivity(intent);
        });

        btnImportarOcrEsbrla.setOnClickListener(v -> {
            Intent intent = new Intent(EsbrlaMenuActivity.this, ChecklistHeaderActivity.class);
            intent.putExtra("header_key", "esbrla");
            intent.putExtra("header_titulo", "Modelo ESBR-ESBRH-ESLA");
            intent.putExtra("destino_tipo", "esbrla_menu");
            intent.putExtra(ChecklistHeaderActivity.EXTRA_AUTO_IMPORT_OCR, true);
            intent.putExtra(ChecklistHeaderActivity.EXTRA_FINISH_AFTER_OCR, false);
            startActivity(intent);
        });
    }

    private void carregarChecklistsEsbrlaDoJson() {
        try {
            String json = lerArquivoAssets("checklist_esbrla.json");
            JSONObject root = new JSONObject(json);
            JSONArray checklistsArray = root.getJSONArray("checklists");

            for (int i = 0; i < checklistsArray.length(); i++) {
                JSONObject checklistObj = checklistsArray.getJSONObject(i);
                String id = checklistObj.getString("id");
                String nome = checklistObj.getString("nome");

                if (id.startsWith("checklist_esbrla_")) {
                    esbrlaIds.add(id);
                    esbrlaNomes.add(nome);
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

