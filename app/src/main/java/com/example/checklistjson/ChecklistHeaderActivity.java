package com.example.checklistjson;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ChecklistHeaderActivity extends AppCompatActivity {

    private String headerKey;
    private String headerTitulo;
    private String destinoTipo; // "single_checklist", "irbr_menu", "ucabr_menu"
    private String destinoChecklistId;
    private String destinoChecklistNome;

    private EditText etClienteObra;
    private EditText etModelo;
    private EditText etSn;
    private EditText etFluido;
    private EditText etTensao;
    private EditText etOp;
    private EditText etTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist_header);

        TextView tvTitulo = findViewById(R.id.tvHeaderTitulo);
        etClienteObra = findViewById(R.id.etClienteObra);
        etModelo = findViewById(R.id.etModelo);
        etSn = findViewById(R.id.etSn);
        etFluido = findViewById(R.id.etFluido);
        etTensao = findViewById(R.id.etTensao);
        etOp = findViewById(R.id.etOp);
        etTag = findViewById(R.id.etTag);
        Button btnContinuar = findViewById(R.id.btnContinuarChecklist);

        headerKey = getIntent().getStringExtra("header_key");
        headerTitulo = getIntent().getStringExtra("header_titulo");
        destinoTipo = getIntent().getStringExtra("destino_tipo");
        destinoChecklistId = getIntent().getStringExtra("destino_checklist_id");
        destinoChecklistNome = getIntent().getStringExtra("destino_checklist_nome");

        if (headerTitulo != null) {
            tvTitulo.setText("Dados do equipamento - " + headerTitulo);
        }

        carregarCabecalhoEquipamento();

        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarCabecalhoEquipamento();
                if ("single_checklist".equals(destinoTipo)) {
                    Intent intent = new Intent(ChecklistHeaderActivity.this, ChecklistActivity.class);
                    intent.putExtra("checklist_id", destinoChecklistId);
                    intent.putExtra("checklist_nome", destinoChecklistNome);
                    startActivity(intent);
                } else if ("irbr_menu".equals(destinoTipo)) {
                    Intent intent = new Intent(ChecklistHeaderActivity.this, IrbrMenuActivity.class);
                    startActivity(intent);
                } else if ("ucabr_menu".equals(destinoTipo)) {
                    Intent intent = new Intent(ChecklistHeaderActivity.this, UcabrMenuActivity.class);
                    startActivity(intent);
                } else if ("edbrse_menu".equals(destinoTipo)) {
                    Intent intent = new Intent(ChecklistHeaderActivity.this, EdbrseMenuActivity.class);
                    startActivity(intent);
                } else if ("esbrag_menu".equals(destinoTipo)) {
                    Intent intent = new Intent(ChecklistHeaderActivity.this, EsbragMenuActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void carregarCabecalhoEquipamento() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        etClienteObra.setText(prefs.getString(gerarChaveEquip(headerKey, "cliente_obra"), ""));
        etModelo.setText(prefs.getString(gerarChaveEquip(headerKey, "modelo"), ""));
        etSn.setText(prefs.getString(gerarChaveEquip(headerKey, "sn"), ""));
        etFluido.setText(prefs.getString(gerarChaveEquip(headerKey, "fluido"), ""));
        etTensao.setText(prefs.getString(gerarChaveEquip(headerKey, "tensao"), ""));
        etOp.setText(prefs.getString(gerarChaveEquip(headerKey, "op"), ""));
        etTag.setText(prefs.getString(gerarChaveEquip(headerKey, "tag"), ""));
    }

    private void salvarCabecalhoEquipamento() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(gerarChaveEquip(headerKey, "cliente_obra"), etClienteObra.getText().toString());
        editor.putString(gerarChaveEquip(headerKey, "modelo"), etModelo.getText().toString());
        editor.putString(gerarChaveEquip(headerKey, "sn"), etSn.getText().toString());
        editor.putString(gerarChaveEquip(headerKey, "fluido"), etFluido.getText().toString());
        editor.putString(gerarChaveEquip(headerKey, "tensao"), etTensao.getText().toString());
        editor.putString(gerarChaveEquip(headerKey, "op"), etOp.getText().toString());
        editor.putString(gerarChaveEquip(headerKey, "tag"), etTag.getText().toString());

        editor.apply();
    }

    public static String gerarChaveEquip(String headerKey, String campo) {
        return "modelo_" + headerKey + "_equip_" + campo;
    }
}

