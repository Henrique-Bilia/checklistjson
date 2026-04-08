package com.example.checklistjson;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WallMenuActivity extends androidx.appcompat.app.AppCompatActivity {

    private ListView listViewWall;
    private final List<String> wallNomes = new ArrayList<>();
    private final List<String> wallIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall_menu);

        TextView tvTitulo = findViewById(R.id.tvWallTitulo);
        listViewWall = findViewById(R.id.listViewWall);
        Button btnExportarModeloWall = findViewById(R.id.btnExportarModeloWall);
        Button btnImportarOcrWall = findViewById(R.id.btnImportarOcrWall);

        tvTitulo.setText("Checklists WALL (WUBR/WDBR)");

        carregarChecklistsWallDoJson();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                wallNomes
        );
        listViewWall.setAdapter(adapter);

        listViewWall.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                String checklistId = wallIds.get(position);
                String checklistNome = wallNomes.get(position);

                Intent intent = new Intent(WallMenuActivity.this, ChecklistActivity.class);
                intent.putExtra("checklist_id", checklistId);
                intent.putExtra("checklist_nome", checklistNome);
                intent.putStringArrayListExtra("lista_ids", new ArrayList<>(wallIds));
                intent.putStringArrayListExtra("lista_nomes", new ArrayList<>(wallNomes));
                intent.putExtra("indice_atual", position);
                startActivity(intent);
            }
        });

        btnExportarModeloWall.setOnClickListener(v -> {
            Intent intent = new Intent(WallMenuActivity.this, ModelExportActivity.class);
            intent.putExtra("model_key", "wall");
            intent.putExtra("model_name", "Modelo WALL (WUBR/WDBR)");
            startActivity(intent);
        });

        btnImportarOcrWall.setOnClickListener(v -> {
            Intent intent = new Intent(WallMenuActivity.this, ChecklistHeaderActivity.class);
            intent.putExtra("header_key", "wall");
            intent.putExtra("header_titulo", "Modelo WALL (WUBR/WDBR)");
            intent.putExtra("destino_tipo", "wall_menu");
            intent.putExtra(ChecklistHeaderActivity.EXTRA_AUTO_IMPORT_OCR, true);
            intent.putExtra(ChecklistHeaderActivity.EXTRA_FINISH_AFTER_OCR, false);
            startActivity(intent);
        });
    }

    private void carregarChecklistsWallDoJson() {
        try {
            String json = lerArquivoAssets("checklist_wall.json");
            JSONObject root = new JSONObject(json);
            JSONArray checklistsArray = root.getJSONArray("checklists");

            for (int i = 0; i < checklistsArray.length(); i++) {
                JSONObject checklistObj = checklistsArray.getJSONObject(i);
                String id = checklistObj.getString("id");
                String nome = checklistObj.getString("nome");

                // Convenção: checklists WALL começam com "checklist_wall_"
                if (id.startsWith("checklist_wall_")) {
                    wallIds.add(id);
                    wallNomes.add(nome);
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

