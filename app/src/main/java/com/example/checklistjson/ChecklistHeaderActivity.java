package com.example.checklistjson;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.IntentSenderRequest;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.Locale;

public class ChecklistHeaderActivity extends AppCompatActivity {

    public static final String EXTRA_AUTO_IMPORT_OCR = "auto_import_ocr";
    public static final String EXTRA_FINISH_AFTER_OCR = "finish_after_ocr";

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

    private ActivityResultLauncher<IntentSenderRequest> docScanLauncher;
    private GmsDocumentScanner docScanner;
    private TextRecognizer textRecognizer;
    private boolean finishAfterOcr = false;
    private String ultimoTextoOcr = "";

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
        Button btnImportarOcr = findViewById(R.id.btnImportarOcr);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        docScanner = GmsDocumentScanning.getClient(
                new GmsDocumentScannerOptions.Builder()
                        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                        .setGalleryImportAllowed(true)
                        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                        .build()
        );
        docScanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) return;
                    com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult r =
                            com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult.fromActivityResultIntent(result.getData());
                    if (r == null || r.getPages() == null || r.getPages().isEmpty()) return;
                    Uri pageUri = r.getPages().get(0).getImageUri();
                    if (pageUri != null) importarPorOcr(pageUri);
                }
        );

        headerKey = getIntent().getStringExtra("header_key");
        modelKey = headerKey; // por compatibilidade, headerKey original é o modelo
        headerTitulo = getIntent().getStringExtra("header_titulo");
        destinoTipo = getIntent().getStringExtra("destino_tipo");
        destinoChecklistId = getIntent().getStringExtra("destino_checklist_id");
        destinoChecklistNome = getIntent().getStringExtra("destino_checklist_nome");
        finishAfterOcr = getIntent() != null && getIntent().getBooleanExtra(EXTRA_FINISH_AFTER_OCR, false);

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
        preencherModeloSeVazio();

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

        btnImportarOcr.setOnClickListener(v -> mostrarOpcoesImportacaoOcr());

        boolean autoImport = getIntent() != null && getIntent().getBooleanExtra(EXTRA_AUTO_IMPORT_OCR, false);
        if (autoImport) {
            // precisa ser pós-layout/registro do launcher
            btnImportarOcr.post(this::mostrarOpcoesImportacaoOcr);
        }

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
                } else if ("esbrla_menu".equals(destinoTipo)) {
                    Intent intent = new Intent(ChecklistHeaderActivity.this, EsbrlaMenuActivity.class);
                    startActivity(intent);
                } else if ("cabr_menu".equals(destinoTipo)) {
                    Intent intent = new Intent(ChecklistHeaderActivity.this, CabrMenuActivity.class);
                    startActivity(intent);
                    } else if ("edbrag_menu".equals(destinoTipo)) {
                    Intent intent = new Intent(ChecklistHeaderActivity.this, EdbragMenuActivity.class);
                    startActivity(intent);
                } else if ("wall_menu".equals(destinoTipo)) {
                    Intent intent = new Intent(ChecklistHeaderActivity.this, WallMenuActivity.class);
                    startActivity(intent);
                } else if ("dcbr_menu".equals(destinoTipo)) {
                    Intent intent = new Intent(ChecklistHeaderActivity.this, DcbrMenuActivity.class);
                    startActivity(intent);
            }
    }});
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (textRecognizer != null) textRecognizer.close();
        } catch (Exception ignored) {
        }
    }

    private void importarPorOcr(Uri uri) {
        try {
            InputImage image = InputImage.fromFilePath(this, uri);
            textRecognizer.process(image)
                    .addOnSuccessListener(result -> {
                        ultimoTextoOcr = result == null ? "" : result.getText();
                        aplicarImportacaoOcr(ultimoTextoOcr);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Falha ao ler texto da imagem: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível abrir a imagem: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarOpcoesImportacaoOcr() {
        String[] opcoes = new String[]{"Scanner (câmera/galeria)"};
        new AlertDialog.Builder(this)
                .setTitle("Importar (OCR)")
                .setItems(opcoes, (d, which) -> {
                    iniciarScannerDocumento();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void iniciarScannerDocumento() {
        try {
            docScanner.getStartScanIntent(this)
                    .addOnSuccessListener(intentSender ->
                            docScanLauncher.launch(new IntentSenderRequest.Builder(intentSender).build())
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Falha ao iniciar scanner: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        } catch (Exception e) {
            Toast.makeText(this, "Falha ao iniciar scanner: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void aplicarImportacaoOcr(String textoOcr) {
        WorkOrderOcrParser.Parsed parsed = WorkOrderOcrParser.parse(textoOcr);
        if (parsed == null || parsed.op == null || parsed.op.trim().isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Importar (OCR)")
                    .setMessage("Não consegui identificar a OP na imagem.\n\nDica: verifique se aparece algo como \"OP: 5254\".")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // Para o teste: usamos o modelo atual da tela. Se o OCR indicar outro modelo, apenas avisamos.
        WorkOrderOcrParser.ModelUnits mu = null;
        for (WorkOrderOcrParser.ModelUnits x : parsed.modelUnits) {
            if (x != null && modelKey != null && modelKey.equals(x.modelKey)) {
                mu = x;
                break;
            }
        }
        if (mu == null && !parsed.modelUnits.isEmpty()) {
            mu = parsed.modelUnits.get(0);
        }

        int units = mu != null ? mu.units : 0;
        if (units <= 0) units = 1;
        String modeloDetectado = mu != null ? mu.modelKey : "";

        String avisoModelo = "";
        if (modeloDetectado != null && !modeloDetectado.isEmpty() && modelKey != null && !modelKey.equals(modeloDetectado)) {
            avisoModelo = "\n\nAtenção: a imagem parece ser do modelo " + modeloDetectado.toUpperCase(Locale.ROOT)
                    + ", mas você está no modelo " + modelKey.toUpperCase(Locale.ROOT) + ".";
        }

        String fluidoDetectado = parsed.fluido == null ? "" : parsed.fluido.trim();
        String tensaoDetectada = parsed.tensao == null ? "" : parsed.tensao.trim();
        String clienteObraDetectado = parsed.clienteObra == null ? "" : parsed.clienteObra.trim();

        String msg = "Detectado:\n"
                + "- OP: " + parsed.op + "\n"
                + "- Quantidade (UN): " + units + "\n"
                + "- Cliente/Obra: " + (clienteObraDetectado.isEmpty() ? "-" : clienteObraDetectado) + "\n"
                + "- Fluído: " + (fluidoDetectado.isEmpty() ? "-" : fluidoDetectado) + "\n"
                + "- Tensão: " + (tensaoDetectada.isEmpty() ? "-" : tensaoDetectada) + "\n"
                + "- TAGs que serão criadas: 01 até " + String.format(Locale.ROOT, "%02d", units)
                + avisoModelo
                + "\n\nDeseja aplicar agora?";

        int finalUnits = units;
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Importar (OCR)")
                .setMessage(msg)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aplicar", (d, w) -> {
                    aplicarOpETags(parsed.op, finalUnits, clienteObraDetectado, fluidoDetectado, tensaoDetectada);
                });

        if (ultimoTextoOcr != null && !ultimoTextoOcr.trim().isEmpty()) {
            builder.setNeutralButton("Ver texto OCR", (d, w) -> {
                new AlertDialog.Builder(this)
                        .setTitle("Texto OCR bruto")
                        .setMessage(ultimoTextoOcr)
                        .setPositiveButton("Fechar", null)
                        .show();
            });
        }

        builder.show();
    }

    private void aplicarOpETags(String op, int units, String clienteObra, String fluido, String tensao) {
        if (op == null) op = "";
        if (units <= 0) units = 1;

        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Corrige o bug: não dá para depender de prefs.getString() dentro do loop,
        // porque o editor ainda não aplicou as alterações. Então montamos a lista em memória e gravamos 1 vez.
        String listKey = getEquipListKey(modelKey);
        String existing = prefs.getString(listKey, "");
        java.util.LinkedHashSet<String> keys = new java.util.LinkedHashSet<>();
        if (existing != null && !existing.trim().isEmpty()) {
            for (String k : existing.split(";")) {
                if (k != null && !k.trim().isEmpty()) keys.add(k.trim());
            }
        }

        for (int i = 1; i <= units; i++) {
            String tag = String.format(Locale.ROOT, "%02d", i);
            String equipKey = gerarEquipKey(modelKey, op.trim(), tag);
            keys.add(equipKey);

            // garante OP/TAG persistidos para cada máquina criada
            editor.putString(gerarChaveEquip(equipKey, "op"), op.trim());
            editor.putString(gerarChaveEquip(equipKey, "tag"), tag);
            // modelo também não deve precisar ser digitado novamente
            editor.putString(gerarChaveEquip(equipKey, "modelo"), labelDoModeloAtual());
            if (clienteObra != null && !clienteObra.trim().isEmpty()) {
                editor.putString(gerarChaveEquip(equipKey, "cliente_obra"), clienteObra.trim());
            }

            if (fluido != null && !fluido.trim().isEmpty()) {
                editor.putString(gerarChaveEquip(equipKey, "fluido"), fluido.trim());
            }
            if (tensao != null && !tensao.trim().isEmpty()) {
                editor.putString(gerarChaveEquip(equipKey, "tensao"), tensao.trim());
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String k : keys) {
            if (sb.length() > 0) sb.append(";");
            sb.append(k);
        }
        editor.putString(listKey, sb.toString());

        String equipKeyAtual = gerarEquipKey(modelKey, op.trim(), "01");
        setCurrentEquipKey(editor, modelKey, equipKeyAtual);
        editor.apply();

        headerKey = equipKeyAtual;
        carregarCabecalhoEquipamento();
        preencherModeloSeVazio();
        preencherClienteObraSeVazio();

        Toast.makeText(this, "OP " + op.trim() + " importada. Selecionada TAG 01.", Toast.LENGTH_SHORT).show();
        if (finishAfterOcr) {
            finish();
        }
    }

    private void preencherModeloSeVazio() {
        if (etModelo == null) return;
        String atual = etModelo.getText() == null ? "" : etModelo.getText().toString().trim();
        if (!atual.isEmpty()) return;
        etModelo.setText(labelDoModeloAtual());
    }

    private void preencherClienteObraSeVazio() {
        if (etClienteObra == null) return;
        String atual = etClienteObra.getText() == null ? "" : etClienteObra.getText().toString().trim();
        if (!atual.isEmpty()) return;
        // Se não houver valor salvo, não inventa aqui (o OCR já salva quando detectar).
        // Mantemos esta função para manter simetria e permitir futuras regras.
    }

    private String labelDoModeloAtual() {
        if ("irbr".equals(modelKey)) return "IRBR";
        if ("ucabr".equals(modelKey)) return "UCABR";
        if ("edbrse".equals(modelKey)) return "EDBRSE/EUBRSE";
        if ("esbrag".equals(modelKey)) return "ESBRAG/ESBRHAG";
        if ("cabr".equals(modelKey)) return "CABR";
        if ("wall".equals(modelKey)) return "WALL (WUBR/WDBR)";
        if ("edbrag".equals(modelKey)) return "EDBRAG/EUBRAG";
        if ("dcbr".equals(modelKey)) return "DCBR";
        return modelKey == null ? "" : modelKey.toUpperCase(Locale.ROOT);
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

