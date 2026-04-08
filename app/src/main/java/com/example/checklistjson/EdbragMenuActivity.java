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

public class EdbragMenuActivity extends AppCompatActivity {
    private ListView listViewEdbrag;
    private final List<String> edbragNomes = new ArrayList<>();
    private final List<String> edbragIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edbrag_menu);

        TextView tvTitulo = findViewById(R.id.tvEdbragTitulo);
        listViewEdbrag = findViewById(R.id.listViewEdbrag);
        android.widget.Button btnExportarModeloEdbrag = findViewById(R.id.btnExportarModeloEdbrag);
        android.widget.Button btnImportarOcrEdbrag = findViewById(R.id.btnImportarOcrEdbrag);

        tvTitulo.setText("Checklist EDBRAG-EUBRAG");

        carregarChecklistsEdbragDoJson();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                edbragNomes
        );
        listViewEdbrag.setAdapter(adapter);

        listViewEdbrag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                String checklistId = edbragIds.get(position);
                String checklistNome = edbragNomes.get(position);

                Intent intent = new Intent(EdbragMenuActivity.this, ChecklistActivity.class);
                intent.putExtra("checklist_id", checklistId);
                intent.putExtra("checklist_nome", checklistNome);
                intent.putStringArrayListExtra("lista_ids", new ArrayList<>(edbragIds));
                intent.putStringArrayListExtra("lista_nomes", new ArrayList<>(edbragNomes));
                intent.putExtra("indice_atual", position);
                startActivity(intent);
            }
        });

        btnExportarModeloEdbrag.setOnClickListener(v -> {
            Intent intent = new Intent(EdbragMenuActivity.this, ModelExportActivity.class);
            intent.putExtra("model_key", "edbrag");
            intent.putExtra("model_name", "Modelo EDBRAG/EUBRAG");
            startActivity(intent);
        });

        btnImportarOcrEdbrag.setOnClickListener(v -> {
            Intent intent = new Intent(EdbragMenuActivity.this, ChecklistHeaderActivity.class);
            intent.putExtra("header_key", "edbrag");
            intent.putExtra("header_titulo", "Modelo EDBRAG / EUBRAG");
            intent.putExtra("destino_tipo", "edbrag_menu");
            intent.putExtra(ChecklistHeaderActivity.EXTRA_AUTO_IMPORT_OCR, true);
            intent.putExtra(ChecklistHeaderActivity.EXTRA_FINISH_AFTER_OCR, false);
            startActivity(intent);
        });
    }

    private void carregarChecklistsEdbragDoJson() {
        try {
            String json = lerArquivoAssets("checklist_edbrag.json");
            JSONObject root = new JSONObject(json);
            JSONArray checklistsArray = root.getJSONArray("checklists");

            for (int i = 0; i < checklistsArray.length(); i++) {
                JSONObject checklistObj = checklistsArray.getJSONObject(i);
                String id = checklistObj.getString("id");
                String nome = checklistObj.getString("nome");

                // Convenção: todos os checklists EDBRAG / EUBRAG começam com "checklist_edbrag_"
                if (id.startsWith("checklist_edbrag_")) 
                    edbragIds.add(id);
                    edbragNomes.add(nome);
                }    
            
        }catch (Exception e) {
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