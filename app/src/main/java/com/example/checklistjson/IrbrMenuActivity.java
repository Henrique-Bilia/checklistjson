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

public class IrbrMenuActivity extends AppCompatActivity {

    private ListView listViewIrbr;
    private final List<String> irbrNomes = new ArrayList<>();
    private final List<String> irbrIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irbr_menu);

        TextView tvTitulo = findViewById(R.id.tvIrbrTitulo);
        listViewIrbr = findViewById(R.id.listViewIrbr);
        android.widget.Button btnExportarModeloIrbr = findViewById(R.id.btnExportarModeloIrbr);
        android.widget.Button btnImportarOcrIrbr = findViewById(R.id.btnImportarOcrIrbr);

        tvTitulo.setText("Checklists IRBR");

        carregarChecklistsIrbrDoJson();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                irbrNomes
        );
        listViewIrbr.setAdapter(adapter);

        listViewIrbr.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                String checklistId = irbrIds.get(position);
                String checklistNome = irbrNomes.get(position);

                Intent intent = new Intent(IrbrMenuActivity.this, ChecklistActivity.class);
                intent.putExtra("checklist_id", checklistId);
                intent.putExtra("checklist_nome", checklistNome);
                intent.putStringArrayListExtra("lista_ids", new ArrayList<>(irbrIds));
                intent.putStringArrayListExtra("lista_nomes", new ArrayList<>(irbrNomes));
                intent.putExtra("indice_atual", position);
                startActivity(intent);
            }
        });

        btnExportarModeloIrbr.setOnClickListener(v -> {
            Intent intent = new Intent(IrbrMenuActivity.this, ModelExportActivity.class);
            intent.putExtra("model_key", "irbr");
            intent.putExtra("model_name", "Modelo IRBR");
            startActivity(intent);
        });

        btnImportarOcrIrbr.setOnClickListener(v -> {
            Intent intent = new Intent(IrbrMenuActivity.this, ChecklistHeaderActivity.class);
            intent.putExtra("header_key", "irbr");
            intent.putExtra("header_titulo", "Modelo IRBR");
            intent.putExtra("destino_tipo", "irbr_menu");
            intent.putExtra(ChecklistHeaderActivity.EXTRA_AUTO_IMPORT_OCR, true);
            // Não finaliza após OCR: deixa o usuário ver/ajustar os dados do equipamento.
            intent.putExtra(ChecklistHeaderActivity.EXTRA_FINISH_AFTER_OCR, false);
            startActivity(intent);
        });
    }

    private void carregarChecklistsIrbrDoJson() {
        try {
            String json = lerArquivoAssets("checklist_irbr.json");
            JSONObject root = new JSONObject(json);
            JSONArray checklistsArray = root.getJSONArray("checklists");

            for (int i = 0; i < checklistsArray.length(); i++) {
                JSONObject checklistObj = checklistsArray.getJSONObject(i);
                String id = checklistObj.getString("id");
                String nome = checklistObj.getString("nome");

                // Convenção: todos os checklists IRBR começam com "checklist_irbr_"
                if (id.startsWith("checklist_irbr_")) {
                    irbrIds.add(id);
                    irbrNomes.add(nome);
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

