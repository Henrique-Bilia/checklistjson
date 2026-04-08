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

public class DcbrMenuActivity extends AppCompatActivity {
    private ListView listViewDcbr;
    private final List<String> dcbrNomes = new ArrayList<>();
    private final List<String> dcbrIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dcbr_menu);

        TextView tvTitulo = findViewById(R.id.tvDcbrTitulo);
        listViewDcbr = findViewById(R.id.listViewDcbr);
        android.widget.Button btnExportarModeloDcbr = findViewById(R.id.btnExportarModeloDcbr);
        android.widget.Button btnImportarOcrDcbr = findViewById(R.id.btnImportarOcrDcbr);

        tvTitulo.setText("Checklist DCBR");

        carregarChecklistsdcbrDoJson();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                dcbrNomes
        );
        listViewDcbr.setAdapter(adapter);

        listViewDcbr.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                String checklistId = dcbrIds.get(position);
                String checklistNome = dcbrNomes.get(position);

                Intent intent = new Intent(DcbrMenuActivity.this, ChecklistActivity.class);
                intent.putExtra("checklist_id", checklistId);
                intent.putExtra("checklist_nome", checklistNome);
                intent.putStringArrayListExtra("lista_ids", new ArrayList<>(dcbrIds));
                intent.putStringArrayListExtra("lista_nomes", new ArrayList<>(dcbrNomes));
                intent.putExtra("indice_atual", position);
                startActivity(intent);
            }
        });

        btnExportarModeloDcbr.setOnClickListener(v -> {
            Intent intent = new Intent(DcbrMenuActivity.this, ModelExportActivity.class);
            intent.putExtra("model_key", "dcbr");
            intent.putExtra("model_name", "Modelo DCBR");
            startActivity(intent);
        });

        btnImportarOcrDcbr.setOnClickListener(v -> {
            Intent intent = new Intent(DcbrMenuActivity.this, ChecklistHeaderActivity.class);
            intent.putExtra("header_key", "dcbr");
            intent.putExtra("header_titulo", "Modelo DCBR");
            intent.putExtra("destino_tipo", "dcbr_menu");
            intent.putExtra(ChecklistHeaderActivity.EXTRA_AUTO_IMPORT_OCR, true);
            intent.putExtra(ChecklistHeaderActivity.EXTRA_FINISH_AFTER_OCR, false);
            startActivity(intent);
        });
    }

    private void carregarChecklistsdcbrDoJson() {
        try {
            String json = lerArquivoAssets("checklist_dcbr.json");
            JSONObject root = new JSONObject(json);
            JSONArray checklistsArray = root.getJSONArray("checklists");

            for (int i = 0; i < checklistsArray.length(); i++) {
                JSONObject checklistObj = checklistsArray.getJSONObject(i);
                String id = checklistObj.getString("id");
                String nome = checklistObj.getString("nome");

                // Convenção: todos os checklists DCBR começam com "checklist_dcbr_"
                if (id.startsWith("checklist_dcbr_")) 
                    dcbrIds.add(id);
                    dcbrNomes.add(nome);
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