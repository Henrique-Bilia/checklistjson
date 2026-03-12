package com.example.checklistjson;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ChecklistHeaderActivity extends AppCompatActivity {

    private String headerKey;   // chave atual (modelo ou modelo_op_tag)
    private String modelKey;    // apenas o modelo base (irbr, ucabr, ...)
    private String headerTitulo;
    private String destinoTipo; // "single_checklist", "irbr_menu", "ucabr_menu"
    private String destinoChecklistId;
    private String destinoChecklistNome;

    private EditText etClienteObra;
    private EditText etModelo;
    private EditText etSn;
    private EditText etFluido;
    private EditText etTensao;
    private EditText etElaboradoPor;
    private EditText etDataElaboracao;
    private EditText etAprovadoPor;
    private EditText etDataAprovacao;
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
        etElaboradoPor = findViewById(R.id.etElaboradoPor);
        etDataElaboracao = findViewById(R.id.etDataElaboracao);
        etAprovadoPor = findViewById(R.id.etAprovadoPor);
        etDataAprovacao = findViewById(R.id.etDataAprovacao);
        etOp = findViewById(R.id.etOp);
        etTag = findViewById(R.id.etTag);
        Button btnContinuar = findViewById(R.id.btnContinuarChecklist);
        Button btnSelecionarMaquina = findViewById(R.id.btnSelecionarMaquina);

        headerKey = getIntent().getStringExtra("header_key");
        modelKey = headerKey; // por compatibilidade, headerKey original é o modelo
        headerTitulo = getIntent().getStringExtra("header_titulo");
        destinoTipo = getIntent().getStringExtra("destino_tipo");
        destinoChecklistId = getIntent().getStringExtra("destino_checklist_id");
        destinoChecklistNome = getIntent().getStringExtra("destino_checklist_nome");

        if (headerTitulo != null) {
            tvTitulo.setText("Dados do equipamento - " + headerTitulo);
        }

        // Descobre máquina atual (se existir) para este modelo
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        String currentEquipKey = getCurrentEquipKey(prefs, modelKey);
        if (currentEquipKey != null) {
            headerKey = currentEquipKey;
        }

        carregarCabecalhoEquipamento();

        // DatePickers para as datas de elaboração e aprovação
        etDataElaboracao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDatePickerParaCampo(etDataElaboracao);
            }
        });

        etDataAprovacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDatePickerParaCampo(etDataAprovacao);
            }
        });

        btnSelecionarMaquina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogSelecionarMaquina();
            }
        });

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
                } else if ("cabr_menu".equals(destinoTipo)) {
                    Intent intent = new Intent(ChecklistHeaderActivity.this, CabrMenuActivity.class);
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
        etElaboradoPor.setText(prefs.getString(gerarChaveEquip(headerKey, "elaborado_por"), ""));
        etDataElaboracao.setText(prefs.getString(gerarChaveEquip(headerKey, "data_elaboracao"), ""));
        etAprovadoPor.setText(prefs.getString(gerarChaveEquip(headerKey, "aprovado_por"), ""));
        etDataAprovacao.setText(prefs.getString(gerarChaveEquip(headerKey, "data_aprovacao"), ""));
        etOp.setText(prefs.getString(gerarChaveEquip(headerKey, "op"), ""));
        etTag.setText(prefs.getString(gerarChaveEquip(headerKey, "tag"), ""));
    }

    private void salvarCabecalhoEquipamento() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // calcula uma chave única por máquina usando modelo + OP + TAG
        String op = etOp.getText().toString().trim();
        String tag = etTag.getText().toString().trim();
        String novoHeaderKey = modelKey;
        if (!op.isEmpty() || !tag.isEmpty()) {
            novoHeaderKey = gerarEquipKey(modelKey, op, tag);
        }
        headerKey = novoHeaderKey;

        editor.putString(gerarChaveEquip(headerKey, "cliente_obra"), etClienteObra.getText().toString());
        editor.putString(gerarChaveEquip(headerKey, "modelo"), etModelo.getText().toString());
        editor.putString(gerarChaveEquip(headerKey, "sn"), etSn.getText().toString());
        editor.putString(gerarChaveEquip(headerKey, "fluido"), etFluido.getText().toString());
        editor.putString(gerarChaveEquip(headerKey, "tensao"), etTensao.getText().toString());
        editor.putString(gerarChaveEquip(headerKey, "elaborado_por"), etElaboradoPor.getText().toString());
        editor.putString(gerarChaveEquip(headerKey, "data_elaboracao"), etDataElaboracao.getText().toString());
        editor.putString(gerarChaveEquip(headerKey, "aprovado_por"), etAprovadoPor.getText().toString());
        editor.putString(gerarChaveEquip(headerKey, "data_aprovacao"), etDataAprovacao.getText().toString());
        editor.putString(gerarChaveEquip(headerKey, "op"), op);
        editor.putString(gerarChaveEquip(headerKey, "tag"), tag);

        // registra esta máquina na lista e como máquina atual do modelo
        addEquipToList(prefs, editor, modelKey, headerKey);
        setCurrentEquipKey(editor, modelKey, headerKey);

        editor.apply();
    }

    private void mostrarDatePickerParaCampo(final EditText campo) {
        java.util.Calendar calendario = java.util.Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String dia = String.format("%02d", dayOfMonth);
                        String mes = String.format("%02d", month + 1);
                        String dataFormatada = dia + "/" + mes + "/" + year;
                        campo.setText(dataFormatada);
                    }
                },
                calendario.get(java.util.Calendar.YEAR),
                calendario.get(java.util.Calendar.MONTH),
                calendario.get(java.util.Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    public static String gerarChaveEquip(String headerKey, String campo) {
        return "modelo_" + headerKey + "_equip_" + campo;
    }

    private static String gerarEquipKey(String modelKey, String op, String tag) {
        return modelKey + "_op_" + op + "_tag_" + tag;
    }

    private static String getEquipListKey(String modelKey) {
        return "modelo_" + modelKey + "_equip_list";
    }

    private static String getCurrentEquipPrefKey(String modelKey) {
        return "modelo_" + modelKey + "_equip_current_key";
    }

    private static void addEquipToList(SharedPreferences prefs,
                                       SharedPreferences.Editor editor,
                                       String modelKey,
                                       String equipKey) {
        String list = prefs.getString(getEquipListKey(modelKey), "");
        if (list == null || list.isEmpty()) {
            editor.putString(getEquipListKey(modelKey), equipKey);
        } else if (!list.contains(equipKey)) {
            editor.putString(getEquipListKey(modelKey), list + ";" + equipKey);
        }
    }

    private static void setCurrentEquipKey(SharedPreferences.Editor editor,
                                           String modelKey,
                                           String equipKey) {
        editor.putString(getCurrentEquipPrefKey(modelKey), equipKey);
    }

    public static String getCurrentEquipKey(SharedPreferences prefs, String modelKey) {
        return prefs.getString(getCurrentEquipPrefKey(modelKey), null);
    }

    private void mostrarDialogSelecionarMaquina() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        String list = prefs.getString(getEquipListKey(modelKey), "");
        if (list == null || list.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Máquinas")
                    .setMessage("Nenhuma máquina salva para este modelo.\nPreencha os dados e salve para criar uma.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        String[] keys = list.split(";");
        String[] labels = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            String equipKey = keys[i];
            String[] opTag = extrairOpTag(equipKey);
            String op = opTag[0];
            String tag = opTag[1];
            labels[i] = "OP " + (op.isEmpty() ? "-" : op) + " / TAG " + (tag.isEmpty() ? "-" : tag);
        }

        new AlertDialog.Builder(this)
                .setTitle("Selecionar máquina")
                .setItems(labels, (dialog, which) -> {
                    String selecionada = keys[which];
                    headerKey = selecionada;

                    SharedPreferences.Editor editor = prefs.edit();
                    setCurrentEquipKey(editor, modelKey, selecionada);
                    editor.apply();

                    carregarCabecalhoEquipamento();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private static String[] extrairOpTag(String equipKey) {
        String op = "";
        String tag = "";
        if (equipKey != null) {
            int idxOp = equipKey.indexOf("_op_");
            int idxTag = equipKey.indexOf("_tag_");
            if (idxOp >= 0 && idxTag > idxOp) {
                op = equipKey.substring(idxOp + 4, idxTag);
                tag = equipKey.substring(idxTag + 5);
            }
        }
        return new String[]{op, tag};
    }
}

