package com.example.checklistjson;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

import java.util.LinkedHashSet;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ListView listViewChecklists;
    private ActivityResultLauncher<IntentSenderRequest> docScanLauncher;
    private GmsDocumentScanner docScanner;
    private TextRecognizer textRecognizer;
    private String ultimoTextoOcr = "";
    private final String[] opcoesMenu = new String[]{
            "Modelo IRBR",
            "Modelo UCABR",
            "Modelo EDBRSE/EUBRSE",
            "Modelo ESBRAG/ESBRHAG",
            "Modelo CABR"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewChecklists = findViewById(R.id.listViewChecklists);
        Button btnImportarOcrMain = findViewById(R.id.btnImportarOcrMain);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        docScanner = GmsDocumentScanning.getClient(
                new GmsDocumentScannerOptions.Builder()
                        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                        .setGalleryImportAllowed(true)
                        .setResultFormats(
                                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
                        )
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

        btnImportarOcrMain.setOnClickListener(v -> mostrarOpcoesImportacaoOcr());

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
                    intent.putExtra("header_key", "irbr");
                    intent.putExtra("header_titulo", "Modelo IRBR");
                    intent.putExtra("destino_tipo", "irbr_menu");
                    startActivity(intent);
                } else if (position == 1) {
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, ChecklistHeaderActivity.class);
                    intent.putExtra("header_key", "ucabr");
                    intent.putExtra("header_titulo", "Modelo UCABR");
                    intent.putExtra("destino_tipo", "ucabr_menu");
                    startActivity(intent);
                } else if (position == 2) {
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, ChecklistHeaderActivity.class);
                    intent.putExtra("header_key", "edbrse");
                    intent.putExtra("header_titulo", "Modelo EDBRSE/EUBRSE");
                    intent.putExtra("destino_tipo", "edbrse_menu");
                    startActivity(intent);
                } else if (position == 3) {
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, ChecklistHeaderActivity.class);
                    intent.putExtra("header_key", "esbrag");
                    intent.putExtra("header_titulo", "Modelo ESBRAG/ESBRHAG");
                    intent.putExtra("destino_tipo", "esbrag_menu");
                    startActivity(intent);
                } else if (position == 4) {
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, ChecklistHeaderActivity.class);
                    intent.putExtra("header_key", "cabr");
                    intent.putExtra("header_titulo", "Modelo CABR");
                    intent.putExtra("destino_tipo", "cabr_menu");
                    startActivity(intent);
                }
            }
        });
    }

    private void mostrarOpcoesImportacaoOcr() {
        // O scanner já oferece câmera + importar da galeria dentro do próprio fluxo.
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

    private void aplicarImportacaoOcr(String textoOcr) {
        WorkOrderOcrParser.Parsed parsed = WorkOrderOcrParser.parse(textoOcr);
        if (parsed == null || TextUtils.isEmpty(parsed.op) || parsed.modelUnits.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Importar (OCR)")
                    .setMessage("Não consegui identificar OP e/ou modelo na imagem.\n\nDica: precisa ter \"OP: 5254\" e uma linha do tipo \"UCABR... - 3 UN\".")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        WorkOrderOcrParser.ModelUnits mu = parsed.modelUnits.get(0); // parser já ordena por prioridade
        String modelKey = mu.modelKey;
        int units = mu.units <= 0 ? 1 : mu.units;
        String clienteObra = parsed.clienteObra == null ? "" : parsed.clienteObra.trim();
        String fluido = parsed.fluido == null ? "" : parsed.fluido.trim();
        String tensao = parsed.tensao == null ? "" : parsed.tensao.trim();

        String msg = "Detectado:\n"
                + "- Modelo: " + modelKey.toUpperCase(Locale.ROOT) + "\n"
                + "- OP: " + parsed.op + "\n"
                + "- Quantidade (UN): " + units + "\n"
                + "- Cliente/Obra: " + (clienteObra.isEmpty() ? "-" : clienteObra) + "\n"
                + "- Fluído: " + (fluido.isEmpty() ? "-" : fluido) + "\n"
                + "- Tensão: " + (tensao.isEmpty() ? "-" : tensao) + "\n"
                + "- TAGs que serão criadas: 01 até " + String.format(Locale.ROOT, "%02d", units)
                + "\n\nDeseja aplicar agora?";

        int finalUnits = units;
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Importar (OCR)")
                .setMessage(msg)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aplicar", (d, w) -> {
                    aplicarOpETagsNoModelo(modelKey, parsed.op, finalUnits, clienteObra, fluido, tensao);
                    abrirHeaderDoModelo(modelKey);
                })
                .setNeutralButton("Ver texto OCR", (d, w) -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Texto OCR bruto")
                            .setMessage(ultimoTextoOcr == null ? "" : ultimoTextoOcr)
                            .setPositiveButton("Fechar", null)
                            .show();
                });

        builder.show();
    }

    private static String gerarEquipKey(String modelKey, String op, String tag) {
        return modelKey + "_op_" + op + "_tag_" + tag;
    }

    private static String gerarChaveEquip(String headerKey, String campo) {
        return "modelo_" + headerKey + "_equip_" + campo;
    }

    private static String getEquipListKey(String modelKey) {
        return "modelo_" + modelKey + "_equip_list";
    }

    private static String getCurrentEquipPrefKey(String modelKey) {
        return "modelo_" + modelKey + "_equip_current_key";
    }

    private void aplicarOpETagsNoModelo(String modelKey, String op, int units, String clienteObra, String fluido, String tensao) {
        if (op == null) op = "";
        op = op.trim();
        if (units <= 0) units = 1;

        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String listKey = getEquipListKey(modelKey);
        String existing = prefs.getString(listKey, "");
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        if (existing != null && !existing.trim().isEmpty()) {
            for (String k : existing.split(";")) {
                if (k != null && !k.trim().isEmpty()) keys.add(k.trim());
            }
        }

        for (int i = 1; i <= units; i++) {
            String tag = String.format(Locale.ROOT, "%02d", i);
            String equipKey = gerarEquipKey(modelKey, op, tag);
            keys.add(equipKey);

            editor.putString(gerarChaveEquip(equipKey, "op"), op);
            editor.putString(gerarChaveEquip(equipKey, "tag"), tag);
            editor.putString(gerarChaveEquip(equipKey, "modelo"), labelDoModelo(modelKey));
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

        String equipKeyAtual = gerarEquipKey(modelKey, op, "01");
        editor.putString(getCurrentEquipPrefKey(modelKey), equipKeyAtual);
        editor.apply();
    }

    private static String labelDoModelo(String modelKey) {
        if ("irbr".equals(modelKey)) return "IRBR";
        if ("ucabr".equals(modelKey)) return "UCABR";
        if ("edbrse".equals(modelKey)) return "EDBRSE/EUBRSE";
        if ("esbrag".equals(modelKey)) return "ESBRAG/ESBRHAG";
        if ("cabr".equals(modelKey)) return "CABR";
        return modelKey == null ? "" : modelKey.toUpperCase(Locale.ROOT);
    }

    private void abrirHeaderDoModelo(String modelKey) {
        Intent intent = new Intent(this, ChecklistHeaderActivity.class);
        String destinoTipo;
        String titulo;
        switch (modelKey) {
            case "irbr":
                destinoTipo = "irbr_menu";
                titulo = "Modelo IRBR";
                break;
            case "ucabr":
                destinoTipo = "ucabr_menu";
                titulo = "Modelo UCABR";
                break;
            case "edbrse":
                destinoTipo = "edbrse_menu";
                titulo = "Modelo EDBRSE/EUBRSE";
                break;
            case "esbrag":
                destinoTipo = "esbrag_menu";
                titulo = "Modelo ESBRAG/ESBRHAG";
                break;
            case "cabr":
                destinoTipo = "cabr_menu";
                titulo = "Modelo CABR";
                break;
            default:
                Toast.makeText(this, "Modelo não suportado: " + modelKey, Toast.LENGTH_LONG).show();
                return;
        }
        intent.putExtra("header_key", modelKey);
        intent.putExtra("header_titulo", titulo);
        intent.putExtra("destino_tipo", destinoTipo);
        startActivity(intent);
    }
}

