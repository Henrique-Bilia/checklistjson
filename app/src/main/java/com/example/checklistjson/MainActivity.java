package com.example.checklistjson;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ListView listViewChecklists;
    private final String[] opcoesMenu = new String[]{
            "Modelo Manutenção (Exemplo)",
            "Modelo IRBR",
            "Modelo UCABR",
            "Modelo EDBRSE/EUBRSE",
            "Modelo ESBRAG/ESBRHAG"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewChecklists = findViewById(R.id.listViewChecklists);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                opcoesMenu
        );
        listViewChecklists.setAdapter(adapter);

        listViewChecklists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position == 0) {
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, ChecklistHeaderActivity.class);
                    intent.putExtra("header_key", "manutencao");
                    intent.putExtra("header_titulo", "Modelo Manutenção (Exemplo)");
                    intent.putExtra("destino_tipo", "single_checklist");
                    intent.putExtra("destino_checklist_id", "checklist_manutencao");
                    intent.putExtra("destino_checklist_nome", "Checklist de Manutenção (Exemplo)");
                    startActivity(intent);
                } else if (position == 1) {
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, ChecklistHeaderActivity.class);
                    intent.putExtra("header_key", "irbr");
                    intent.putExtra("header_titulo", "Modelo IRBR");
                    intent.putExtra("destino_tipo", "irbr_menu");
                    startActivity(intent);
                } else if (position == 2) {
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, ChecklistHeaderActivity.class);
                    intent.putExtra("header_key", "ucabr");
                    intent.putExtra("header_titulo", "Modelo UCABR");
                    intent.putExtra("destino_tipo", "ucabr_menu");
                    startActivity(intent);
                } else if (position == 3) {
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, ChecklistHeaderActivity.class);
                    intent.putExtra("header_key", "edbrse");
                    intent.putExtra("header_titulo", "Modelo EDBRSE/EUBRSE");
                    intent.putExtra("destino_tipo", "edbrse_menu");
                    startActivity(intent);
                } else if (position == 4) {
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, ChecklistHeaderActivity.class);
                    intent.putExtra("header_key", "esbrag");
                    intent.putExtra("header_titulo", "Modelo ESBRAG/ESBRHAG");
                    intent.putExtra("destino_tipo", "esbrag_menu");
                    startActivity(intent);
                }
            }
        });
    }
}

