package com.example.checklistjson;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ModelExportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String modelKey = getIntent().getStringExtra("model_key");
        String modelName = getIntent().getStringExtra("model_name");

        if (modelKey == null) {
            finish();
            return;
        }

        exportarModeloParaPdf(modelKey, modelName);
        finish();
    }

    private void exportarModeloParaPdf(String modelKey, String modelName) {
        try {
            SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);

            // usa, se existir, a máquina atual selecionada para este modelo
            String headerKeyForModel = ChecklistHeaderActivity.getCurrentEquipKey(prefs, modelKey);
            if (headerKeyForModel == null) {
                headerKeyForModel = modelKey;
            }

            String clienteObra = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKeyForModel, "cliente_obra"), "");
            String modelo = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKeyForModel, "modelo"), "");
            String sn = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKeyForModel, "sn"), "");
            String fluido = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKeyForModel, "fluido"), "");
            String tensao = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKeyForModel, "tensao"), "");
            String op = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKeyForModel, "op"), "");
            String tag = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKeyForModel, "tag"), "");
            String elaboradoPor = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKeyForModel, "elaborado_por"), "");
            String dataElaboracao = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKeyForModel, "data_elaboracao"), "");
            String aprovadoPor = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKeyForModel, "aprovado_por"), "");
            String dataAprovacao = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKeyForModel, "data_aprovacao"), "");

            List<ChecklistDefinition> checklists = carregarChecklistsDoModelo(modelKey);
            if (checklists.isEmpty()) {
                Toast.makeText(this, "Nenhum checklist encontrado para o modelo", Toast.LENGTH_LONG).show();
                return;
            }

            PdfDocument document = new PdfDocument();
            Paint titlePaint = new Paint();
            titlePaint.setTextSize(16);
            titlePaint.setFakeBoldText(true);

            Paint textPaint = new Paint();
            textPaint.setTextSize(10);

            int pageNumber = 1;

            for (ChecklistDefinition def : checklists) {
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();

                int x = 40;
                int y = 50;
                int lineHeight = 14;

                canvas.drawText(modelName + " - " + def.nome, x, y, titlePaint);
                y += lineHeight * 2;

                // Dados do equipamento apenas na primeira página do modelo
                if (pageNumber == 1) {
                    canvas.drawText("Cliente/Obra: " + clienteObra, x, y, textPaint); y += lineHeight;
                    canvas.drawText("Modelo: " + modelo, x, y, textPaint); y += lineHeight;
                    canvas.drawText("S/N: " + sn, x, y, textPaint); y += lineHeight;
                    canvas.drawText("Fluído: " + fluido, x, y, textPaint); y += lineHeight;
                    canvas.drawText("Tensão: " + tensao, x, y, textPaint); y += lineHeight;
                    canvas.drawText("OP: " + op, x, y, textPaint); y += lineHeight;
                    canvas.drawText("TAG: " + tag, x, y, textPaint); y += lineHeight;
                    canvas.drawText("Elaborado por: " + elaboradoPor, x, y, textPaint); y += lineHeight;
                    canvas.drawText("Data da elaboração: " + dataElaboracao, x, y, textPaint); y += lineHeight;
                    canvas.drawText("Aprovado por: " + aprovadoPor, x, y, textPaint); y += lineHeight;
                    canvas.drawText("Data da aprovação: " + dataAprovacao, x, y, textPaint); y += lineHeight * 2;
                }

                String responsavel = prefs.getString(ChecklistActivity.gerarChaveResp(def.id), "");
                String data = prefs.getString(ChecklistActivity.gerarChaveData(def.id), "");
                String obsGeral = prefs.getString(ChecklistActivity.gerarChaveObs(def.id), "");

                // IT específico do checklist (prioritário) ou, se vazio, IT do modelo
                String itChecklist = prefs.getString(ChecklistActivity.gerarChaveIt(def.id), "");
                String itModelo = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKeyForModel, "it"), "");
                String it = !itChecklist.isEmpty() ? itChecklist : itModelo;

                canvas.drawText("Responsável: " + responsavel, x, y, textPaint); y += lineHeight;
                canvas.drawText("Data: " + data, x, y, textPaint); y += lineHeight;
                canvas.drawText("IT: " + it, x, y, textPaint); y += lineHeight * 2;

                canvas.drawText("Itens:", x, y, titlePaint); y += lineHeight;

                for (ChecklistItemStatus item : def.itens) {
                    String prefixo;
                    if ("SIM".equals(item.status)) {
                        prefixo = "[SIM] ";
                    } else if ("NA".equals(item.status)) {
                        prefixo = "[N/A] ";
                    } else {
                        prefixo = "[   ] ";
                    }

                    String linha = prefixo + item.titulo;
                    if (linha.length() > 90) {
                        String parte1 = linha.substring(0, 90);
                        String parte2 = linha.substring(90);
                        canvas.drawText(parte1, x, y, textPaint); y += lineHeight;
                        canvas.drawText("    " + parte2, x, y, textPaint); y += lineHeight;
                    } else {
                        canvas.drawText(linha, x, y, textPaint); y += lineHeight;
                    }

                    if (y > 800) {
                        break;
                    }
                }

                y += lineHeight;
                canvas.drawText("Observação geral:", x, y, titlePaint); y += lineHeight;

                if (!obsGeral.isEmpty()) {
                    String[] linhasObs = obsGeral.split("\n");
                    for (String l : linhasObs) {
                        canvas.drawText(l, x, y, textPaint);
                        y += lineHeight;
                    }
                }

                document.finishPage(page);
                pageNumber++;
            }

            File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (dir == null) {
                dir = getFilesDir();
            }

            String fileName = "modelo_" + modelKey + "_" + System.currentTimeMillis() + ".pdf";
            File file = new File(dir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();

            Toast.makeText(this, "PDF do modelo salvo em: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            try {
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(android.content.Intent.createChooser(shareIntent, "Compartilhar modelo em PDF"));
            } catch (Exception e) {
                // se falhar o compartilhamento, ao menos o arquivo está salvo
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao gerar PDF do modelo", Toast.LENGTH_LONG).show();
        }
    }

    private List<ChecklistDefinition> carregarChecklistsDoModelo(String modelKey) throws Exception {
        String prefix;
        if ("irbr".equals(modelKey)) {
            prefix = "checklist_irbr_";
        } else if ("ucabr".equals(modelKey)) {
            prefix = "checklist_ucabr_";
        } else if ("edbrse".equals(modelKey)) {
            prefix = "checklist_edbrse_";
        } else if ("esbrag".equals(modelKey)) {
            prefix = "checklist_esbrag_";
        } else if ("manutencao".equals(modelKey)) {
            prefix = "checklist_manutencao";
        } else {
            prefix = "";
        }

        List<ChecklistDefinition> lista = new ArrayList<>();

        String json = lerArquivoAssets("checklist.json");
        JSONObject root = new JSONObject(json);
        JSONArray checklistsArray = root.getJSONArray("checklists");
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);

        for (int i = 0; i < checklistsArray.length(); i++) {
            JSONObject obj = checklistsArray.getJSONObject(i);
            String id = obj.getString("id");
            String nome = obj.getString("nome");

            if (!prefix.isEmpty()) {
                if (prefix.equals("checklist_manutencao")) {
                    if (!"checklist_manutencao".equals(id)) continue;
                } else if (!id.startsWith(prefix)) {
                    continue;
                }
            }

            JSONArray itensArray = obj.getJSONArray("itens");
            List<ChecklistItemStatus> itens = new ArrayList<>();

            for (int j = 0; j < itensArray.length(); j++) {
                JSONObject itemObj = itensArray.getJSONObject(j);
                String itemId = itemObj.getString("id");
                String titulo = itemObj.getString("titulo");

                String status = prefs.getString(ChecklistActivity.gerarChaveStatusEquip(this, id, itemId), "");

                // compatibilidade com chave antiga booleana
                if (status.isEmpty()) {
                    boolean antigo = prefs.getBoolean(ChecklistActivity.gerarChavePref(id, itemId), false);
                    if (antigo) status = "SIM";
                }

                itens.add(new ChecklistItemStatus(itemId, titulo, status));
            }

            lista.add(new ChecklistDefinition(id, nome, itens));
        }

        return lista;
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

    private static class ChecklistDefinition {
        String id;
        String nome;
        List<ChecklistItemStatus> itens;

        ChecklistDefinition(String id, String nome, List<ChecklistItemStatus> itens) {
            this.id = id;
            this.nome = nome;
            this.itens = itens;
        }
    }

    private static class ChecklistItemStatus {
        String id;
        String titulo;
        String status;

        ChecklistItemStatus(String id, String titulo, String status) {
            this.id = id;
            this.titulo = titulo;
            this.status = status;
        }
    }
}

