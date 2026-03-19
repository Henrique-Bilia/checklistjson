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

public class EsbragMenuActivity extends AppCompatActivity {

    private ListView listViewEsbrag;
    private final List<String> esbragNomes = new ArrayList<>();
    private final List<String> esbragIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esbrag_menu);

        TextView tvTitulo = findViewById(R.id.tvEsbragTitulo);
        listViewEsbrag = findViewById(R.id.listViewEsbrag);
        android.widget.Button btnExportarModeloEsbrag = findViewById(R.id.btnExportarModeloEsbrag);
        android.widget.Button btnImportarOcrEsbrag = findViewById(R.id.btnImportarOcrEsbrag);

        tvTitulo.setText("Checklists ESBRAG/ESBRHAG");

        carregarChecklistsEsbragDoJson();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                esbragNomes
        );
        listViewEsbrag.setAdapter(adapter);

        listViewEsbrag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                String checklistId = esbragIds.get(position);
                String checklistNome = esbragNomes.get(position);

                Intent intent = new Intent(EsbragMenuActivity.this, ChecklistActivity.class);
                intent.putExtra("checklist_id", checklistId);
                intent.putExtra("checklist_nome", checklistNome);
                intent.putStringArrayListExtra("lista_ids", new ArrayList<>(esbragIds));
                intent.putStringArrayListExtra("lista_nomes", new ArrayList<>(esbragNomes));
                intent.putExtra("indice_atual", position);
                startActivity(intent);
            }
        });

        btnExportarModeloEsbrag.setOnClickListener(v -> {
            Intent intent = new Intent(EsbragMenuActivity.this, ModelExportActivity.class);
            intent.putExtra("model_key", "esbrag");
            intent.putExtra("model_name", "Modelo ESBRAG/ESBRHAG");
            startActivity(intent);
        });

        btnImportarOcrEsbrag.setOnClickListener(v -> {
            Intent intent = new Intent(EsbragMenuActivity.this, ChecklistHeaderActivity.class);
            intent.putExtra("header_key", "esbrag");
            intent.putExtra("header_titulo", "Modelo ESBRAG/ESBRHAG");
            intent.putExtra("destino_tipo", "esbrag_menu");
            intent.putExtra(ChecklistHeaderActivity.EXTRA_AUTO_IMPORT_OCR, true);
            intent.putExtra(ChecklistHeaderActivity.EXTRA_FINISH_AFTER_OCR, false);
            startActivity(intent);
        });
    }

    private void carregarChecklistsEsbragDoJson() {
        try {
            String json = lerArquivoAssets("checklist_esbrag.json");
            JSONObject root = new JSONObject(json);
            JSONArray checklistsArray = root.getJSONArray("checklists");

            for (int i = 0; i < checklistsArray.length(); i++) {
                JSONObject checklistObj = checklistsArray.getJSONObject(i);
                String id = checklistObj.getString("id");
                String nome = checklistObj.getString("nome");

                // Convenção: todos os checklists ESBRAG/ESBRHAG começam com "checklist_esbrag_"
                if (id.startsWith("checklist_esbrag_")) {
                    esbragIds.add(id);
                    esbragNomes.add(nome);
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

