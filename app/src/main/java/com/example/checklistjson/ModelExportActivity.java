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

                // desenha cabeçalho do checklist (e do modelo na primeira página)
                canvas.drawText(modelName + " - " + def.nome, x, y, titlePaint);
                y += lineHeight * 2;

                if (pageNumber == 1) {
                    int col1X = x;
                    int col2X = x + 260; // segunda coluna à direita

                    canvas.drawText("Cliente/Obra: " + clienteObra, col1X, y, textPaint);
                    canvas.drawText("Modelo: " + modelo,        col2X, y, textPaint);
                    y += lineHeight;

                    canvas.drawText("S/N: " + sn,               col1X, y, textPaint);
                    canvas.drawText("Fluído: " + fluido,        col2X, y, textPaint);
                    y += lineHeight;

                    canvas.drawText("Tensão: " + tensao,        col1X, y, textPaint);
                    canvas.drawText("OP: " + op,                col2X, y, textPaint);
                    y += lineHeight;

                    canvas.drawText("TAG: " + tag,              col1X, y, textPaint);
                    canvas.drawText("Elaborado por: " + elaboradoPor, col2X, y, textPaint);
                    y += lineHeight;

                    canvas.drawText("Data da elaboração: " + dataElaboracao, col1X, y, textPaint);
                    canvas.drawText("Aprovado por: " + aprovadoPor,          col2X, y, textPaint);
                    y += lineHeight;

                    canvas.drawText("Data da aprovação: " + dataAprovacao, col1X, y, textPaint);
                    y += lineHeight * 2;
                }

                boolean isLiberacaoFinal =
                        "checklist_ucabr_liberacao_final".equals(def.id) ||
                        "checklist_irbr_liberacao_final".equals(def.id) ||
                        "checklist_edbrse_liberacao_final".equals(def.id) ||
                        "checklist_esbrag_liberacao_final".equals(def.id) ||
                        "checklist_cabr_liberacao_final".equals(def.id);

                String responsavel;
                String data;
                String obsGeral;
                if (isLiberacaoFinal) {
                    responsavel = prefs.getString(ChecklistActivity.gerarChaveRespEquip(this, def.id), "");
                    data = prefs.getString(ChecklistActivity.gerarChaveDataEquip(this, def.id), "");
                    obsGeral = prefs.getString(ChecklistActivity.gerarChaveObsEquip(this, def.id), "");
                } else {
                    responsavel = prefs.getString(ChecklistActivity.gerarChaveResp(def.id), "");
                    data = prefs.getString(ChecklistActivity.gerarChaveData(def.id), "");
                    obsGeral = prefs.getString(ChecklistActivity.gerarChaveObs(def.id), "");
                }

                String itChecklist = prefs.getString(ChecklistActivity.gerarChaveIt(def.id), "");
                String itModelo = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKeyForModel, "it"), "");
                String it = !itChecklist.isEmpty() ? itChecklist : itModelo;

                canvas.drawText("Responsável: " + responsavel, x, y, textPaint); y += lineHeight;
                if (isLiberacaoFinal) {
                    canvas.drawText("Data da liberação: " + data, x, y, textPaint); y += lineHeight * 2;
                } else {
                    canvas.drawText("Data: " + data, x, y, textPaint); y += lineHeight;
                    canvas.drawText("IT: " + it, x, y, textPaint); y += lineHeight * 2;
                }

                if (!isLiberacaoFinal) {
                    canvas.drawText("Itens:", x, y, titlePaint); y += lineHeight;

                    for (ChecklistItemStatus item : def.itens) {
                        // se não couber na página atual, cria nova página de continuação
                        if (y > 800) {
                            document.finishPage(page);
                            pageNumber++;
                            pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                            page = document.startPage(pageInfo);
                            canvas = page.getCanvas();
                            x = 40;
                            y = 50;

                            canvas.drawText(modelName + " - " + def.nome + " (continuação)", x, y, titlePaint);
                            y += lineHeight * 2;
                            canvas.drawText("Itens (continuação):", x, y, titlePaint);
                            y += lineHeight;
                        }

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
                    }

                    y += lineHeight;
                    if (y > 800) {
                        document.finishPage(page);
                        pageNumber++;
                        pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();
                        x = 40;
                        y = 50;
                        canvas.drawText(modelName + " - " + def.nome + " (continuação)", x, y, titlePaint);
                        y += lineHeight * 2;
                    }
                }

                canvas.drawText("Observação geral:", x, y, titlePaint); y += lineHeight;

                if (!obsGeral.isEmpty()) {
                    String[] linhasObs = obsGeral.split("\n");
                    for (String l : linhasObs) {
                        if (y > 800) {
                            document.finishPage(page);
                            pageNumber++;
                            pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                            page = document.startPage(pageInfo);
                            canvas = page.getCanvas();
                            x = 40;
                            y = 50;
                            canvas.drawText(modelName + " - " + def.nome + " (continuação)", x, y, titlePaint);
                            y += lineHeight * 2;
                            canvas.drawText("Observação geral (continuação):", x, y, titlePaint);
                            y += lineHeight;
                        }
                        canvas.drawText(l, x, y, textPaint);
                        y += lineHeight;
                    }
                }

                // Dados especiais dos compressores (C1 e C2) no PDF do modelo
                if ("checklist_ucabr_compressores".equals(def.id)
                        || "checklist_irbr_compressores".equals(def.id)
                        || "checklist_edbrse_compressores".equals(def.id)
                        || "checklist_cabr_compressores".equals(def.id)) {

                    // quebra de página se necessário antes do bloco
                    if (y > 760) {
                        document.finishPage(page);
                        pageNumber++;
                        pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();
                        x = 40;
                        y = 50;
                        canvas.drawText(modelName + " - " + def.nome + " (dados dos compressores)", x, y, titlePaint);
                        y += lineHeight * 2;
                    } else {
                        y += lineHeight * 2;
                        canvas.drawText("Dados dos compressores (C1 e C2)", x, y, titlePaint);
                        y += lineHeight;
                    }

                    String c1Tensao = prefs.getString(ChecklistActivity.gerarChaveComp(this, def.id, "c1_tensao"), "");
                    String c1Fluido = prefs.getString(ChecklistActivity.gerarChaveComp(this, def.id, "c1_fluido"), "");
                    String c1Oleo = prefs.getString(ChecklistActivity.gerarChaveComp(this, def.id, "c1_oleo"), "");
                    String c1Data = prefs.getString(ChecklistActivity.gerarChaveComp(this, def.id, "c1_data"), "");
                    String c1Resp = prefs.getString(ChecklistActivity.gerarChaveComp(this, def.id, "c1_resp"), "");

                    String c2Tensao = prefs.getString(ChecklistActivity.gerarChaveComp(this, def.id, "c2_tensao"), "");
                    String c2Fluido = prefs.getString(ChecklistActivity.gerarChaveComp(this, def.id, "c2_fluido"), "");
                    String c2Oleo = prefs.getString(ChecklistActivity.gerarChaveComp(this, def.id, "c2_oleo"), "");
                    String c2Data = prefs.getString(ChecklistActivity.gerarChaveComp(this, def.id, "c2_data"), "");
                    String c2Resp = prefs.getString(ChecklistActivity.gerarChaveComp(this, def.id, "c2_resp"), "");

                    canvas.drawText("Compressor C1:", x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Data: " + c1Data, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Responsável: " + c1Resp, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Tensão (Hz): " + c1Tensao, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Fluido: " + c1Fluido, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Tipo de óleo: " + c1Oleo, x, y, textPaint); y += lineHeight;

                    y += lineHeight;
                    canvas.drawText("Compressor C2:", x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Data: " + c2Data, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Responsável: " + c2Resp, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Tensão (Hz): " + c2Tensao, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Fluido: " + c2Fluido, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Tipo de óleo: " + c2Oleo, x, y, textPaint); y += lineHeight;
                }

                // Dados especiais do Teste de estanqueidade / hidrostático (C1 e C2) no PDF do modelo
                if ("checklist_ucabr_teste_estanqueidade".equals(def.id)
                        || "checklist_irbr_teste_estanqueidade".equals(def.id)
                        || "checklist_edbrse_teste_estanqueidade".equals(def.id)
                        || "checklist_esbrag_teste_hidrostatico".equals(def.id)
                        || "checklist_cabr_teste_estanqueidade".equals(def.id)) {

                    if (y > 760) {
                        document.finishPage(page);
                        pageNumber++;
                        PdfDocument.PageInfo pageInfo2 = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                        page = document.startPage(pageInfo2);
                        canvas = page.getCanvas();
                        x = 40;
                        y = 50;
                        canvas.drawText(modelName + " - " + def.nome + " (dados do teste)", x, y, titlePaint);
                        y += lineHeight * 2;
                    } else {
                        y += lineHeight * 2;
                        canvas.drawText("Dados do teste de estanqueidade / hidrostático", x, y, titlePaint);
                        y += lineHeight;
                    }

                    String c1Data = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "c1_data"), "");
                    String c1Resp = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "c1_resp"), "");
                    String c1HoraInicio = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "c1_hora_inicio"), "");
                    String c1HoraFim = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "c1_hora_fim"), "");
                    String c1PsiInicio = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "c1_psi_inicio"), "");
                    String c1PsiFim = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "c1_psi_fim"), "");

                    String c2Data = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "c2_data"), "");
                    String c2Resp = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "c2_resp"), "");
                    String c2HoraInicio = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "c2_hora_inicio"), "");
                    String c2HoraFim = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "c2_hora_fim"), "");
                    String c2PsiInicio = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "c2_psi_inicio"), "");
                    String c2PsiFim = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "c2_psi_fim"), "");

                    String obsTeste = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "obs"), "");
                    String numManC1 = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "num_manometro_c1"), "");
                    String numManC2 = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "num_manometro_c2"), "");
                    String dataCalC1 = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "data_calibracao_c1"), "");
                    String dataCalC2 = prefs.getString(ChecklistActivity.gerarChaveEstanq(this, def.id, "data_calibracao_c2"), "");

                    canvas.drawText("Circuito C1:", x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Data: " + c1Data, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Hora início/fim: " + c1HoraInicio + " / " + c1HoraFim, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  PSI início/fim: " + c1PsiInicio + " / " + c1PsiFim, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Responsável: " + c1Resp, x, y, textPaint); y += lineHeight;

                    y += lineHeight;
                    canvas.drawText("Circuito C2:", x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Data: " + c2Data, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Hora início/fim: " + c2HoraInicio + " / " + c2HoraFim, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  PSI início/fim: " + c2PsiInicio + " / " + c2PsiFim, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Responsável: " + c2Resp, x, y, textPaint); y += lineHeight;

                    y += lineHeight;
                    canvas.drawText("Observação do teste: " + obsTeste, x, y, textPaint); y += lineHeight;

                    y += lineHeight;
                    canvas.drawText("Instrumentos:", x, y, textPaint); y += lineHeight;
                    canvas.drawText("  C1 - Nº manômetro / Data calibração: " + numManC1 + " / " + dataCalC1, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  C2 - Nº manômetro / Data calibração: " + numManC2 + " / " + dataCalC2, x, y, textPaint); y += lineHeight;
                }

                // Dados especiais de pressão das válvulas de alívio (Montagem frigorífica) no PDF do modelo
                if ("checklist_ucabr_montagem_frigorifica".equals(def.id)
                        || "checklist_irbr_montagem_frigorifica".equals(def.id)
                        || "checklist_cabr_montagem_frigorifica".equals(def.id)) {

                    if (y > 760) {
                        document.finishPage(page);
                        pageNumber++;
                        pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();
                        x = 40;
                        y = 50;
                        canvas.drawText(modelName + " - " + def.nome + " (pressão válvulas de alívio)", x, y, titlePaint);
                        y += lineHeight * 2;
                    } else {
                        y += lineHeight * 2;
                        canvas.drawText("Pressão das válvulas de alívio", x, y, titlePaint);
                        y += lineHeight;
                    }

                    String p1Dia1 = prefs.getString(ChecklistActivity.gerarChavePressaoValvula(this, def.id, "alivio1_dia1"), "");
                    String p1Dia2 = prefs.getString(ChecklistActivity.gerarChavePressaoValvula(this, def.id, "alivio1_dia2"), "");
                    String p2Dia1 = prefs.getString(ChecklistActivity.gerarChavePressaoValvula(this, def.id, "alivio2_dia1"), "");
                    String p2Dia2 = prefs.getString(ChecklistActivity.gerarChavePressaoValvula(this, def.id, "alivio2_dia2"), "");

                    canvas.drawText("Válvula de alívio 1:", x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Valor dia 1 (kgf/cm²): " + p1Dia1, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Valor dia 2 (kgf/cm²): " + p1Dia2, x, y, textPaint); y += lineHeight;

                    y += lineHeight;
                    canvas.drawText("Válvula de alívio 2:", x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Valor dia 1 (kgf/cm²): " + p2Dia1, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Valor dia 2 (kgf/cm²): " + p2Dia2, x, y, textPaint); y += lineHeight;
                }

                // Dados especiais de Vácuo e quebra de vácuo (C1 e C2) no PDF do modelo
                if ("checklist_ucabr_vacuo_quebra".equals(def.id)
                        || "checklist_irbr_vacuo_quebra".equals(def.id)
                        || "checklist_edbrse_vacuo_quebra".equals(def.id)
                        || "checklist_esbrag_vacuo_quebra".equals(def.id)
                        || "checklist_cabr_vacuo_quebra".equals(def.id)) {

                    // Se não houver espaço suficiente, começa nova página para este bloco
                    if (y > 600) {
                        document.finishPage(page);
                        pageNumber++;
                        pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();
                        x = 40;
                        y = 50;
                        canvas.drawText(modelName + " - " + def.nome + " (Vácuo e quebra de vácuo)", x, y, titlePaint);
                        y += lineHeight * 2;
                    } else {
                        y += lineHeight * 2;
                        canvas.drawText("Dados de vácuo e quebra de vácuo", x, y, titlePaint);
                        y += lineHeight;
                    }

                    // Vácuo C1
                    String vac1DataInicio = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac1_data_inicio"), "");
                    String vac1DataFim = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac1_data_fim"), "");
                    String vac1HoraInicio = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac1_hora_inicio"), "");
                    String vac1HoraFim = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac1_hora_fim"), "");
                    String vac1Valor = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac1_valor"), "");
                    String vac1Resp = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac1_resp"), "");
                    String vac1NumBomba = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac1_num_bomba"), "");
                    String vac1NumVacuometro = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac1_num_vacuometro"), "");
                    String vac1DataCalVacuometro = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac1_data_cal_vacuometro"), "");

                    canvas.drawText("Vácuo C1:", x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Data início/fim: " + vac1DataInicio + " / " + vac1DataFim, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Hora início/fim: " + vac1HoraInicio + " / " + vac1HoraFim, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Vácuo (valor): " + vac1Valor, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Responsável: " + vac1Resp, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Nº da bomba: " + vac1NumBomba, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Nº do vacuômetro/manifold: " + vac1NumVacuometro, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Data calibração vacuômetro/manifold: " + vac1DataCalVacuometro, x, y, textPaint); y += lineHeight;

                    // Vácuo C2
                    String vac2DataInicio = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac2_data_inicio"), "");
                    String vac2DataFim = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac2_data_fim"), "");
                    String vac2HoraInicio = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac2_hora_inicio"), "");
                    String vac2HoraFim = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac2_hora_fim"), "");
                    String vac2Valor = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac2_valor"), "");
                    String vac2Resp = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac2_resp"), "");
                    String vac2NumBomba = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac2_num_bomba"), "");
                    String vac2NumVacuometro = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac2_num_vacuometro"), "");
                    String vac2DataCalVacuometro = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "vac2_data_cal_vacuometro"), "");

                    y += lineHeight;
                    canvas.drawText("Vácuo C2:", x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Data início/fim: " + vac2DataInicio + " / " + vac2DataFim, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Hora início/fim: " + vac2HoraInicio + " / " + vac2HoraFim, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Vácuo (valor): " + vac2Valor, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Responsável: " + vac2Resp, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Nº da bomba: " + vac2NumBomba, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Nº do vacuômetro/manifold: " + vac2NumVacuometro, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Data calibração vacuômetro/manifold: " + vac2DataCalVacuometro, x, y, textPaint); y += lineHeight;

                    // Quebra do vácuo C1
                    String quebraComData = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra_com_data"), "");
                    String quebraQtdRefri = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra_qtd_refri"), "");
                    String quebraComResp = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra_com_resp"), "");
                    String quebraSemData = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra_sem_data"), "");
                    String quebraCargaN2 = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra_carga_n2"), "");
                    String quebraSemResp = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra_sem_resp"), "");
                    String quebraDetData = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra_det_data"), "");
                    String quebraDetResp = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra_det_resp"), "");
                    String quebraNumBalanca = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra_num_balanca"), "");
                    String quebraDataCalBalanca = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra_data_cal_balanca"), "");
                    String quebraNumManometro = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra_num_manometro"), "");
                    String quebraDataCalManometro = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra_data_cal_manometro"), "");

                    y += lineHeight * 2;
                    canvas.drawText("Quebra do vácuo C1:", x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Com válvula - Data / Qtd refri / Resp: " + quebraComData + " / " + quebraQtdRefri + " / " + quebraComResp, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Sem válvula - Data / Carga N2 / Resp: " + quebraSemData + " / " + quebraCargaN2 + " / " + quebraSemResp, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Detector - Data / Resp: " + quebraDetData + " / " + quebraDetResp, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Instrumentos - Balança (Nº / Data): " + quebraNumBalanca + " / " + quebraDataCalBalanca, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Instrumentos - Manômetro (Nº / Data): " + quebraNumManometro + " / " + quebraDataCalManometro, x, y, textPaint); y += lineHeight;

                    // Quebra do vácuo C2
                    String q2ComData = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra2_com_data"), "");
                    String q2QtdRefri = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra2_qtd_refri"), "");
                    String q2ComResp = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra2_com_resp"), "");
                    String q2SemData = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra2_sem_data"), "");
                    String q2CargaN2 = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra2_carga_n2"), "");
                    String q2SemResp = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra2_sem_resp"), "");
                    String q2DetData = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra2_det_data"), "");
                    String q2DetResp = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra2_det_resp"), "");
                    String q2NumBalanca = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra2_num_balanca"), "");
                    String q2DataCalBalanca = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra2_data_cal_balanca"), "");
                    String q2NumManometro = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra2_num_manometro"), "");
                    String q2DataCalManometro = prefs.getString(ChecklistActivity.gerarChaveVacuo(this, def.id, "quebra2_data_cal_manometro"), "");

                    y += lineHeight;
                    canvas.drawText("Quebra do vácuo C2:", x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Com válvula - Data / Qtd refri / Resp: " + q2ComData + " / " + q2QtdRefri + " / " + q2ComResp, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Sem válvula - Data / Carga N2 / Resp: " + q2SemData + " / " + q2CargaN2 + " / " + q2SemResp, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Detector - Data / Resp: " + q2DetData + " / " + q2DetResp, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Instrumentos - Balança (Nº / Data): " + q2NumBalanca + " / " + q2DataCalBalanca, x, y, textPaint); y += lineHeight;
                    canvas.drawText("  Instrumentos - Manômetro (Nº / Data): " + q2NumManometro + " / " + q2DataCalManometro, x, y, textPaint); y += lineHeight;
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
        } else if ("cabr".equals(modelKey)) {
            prefix = "checklist_cabr_";
        } else if ("manutencao".equals(modelKey)) {
            prefix = "checklist_manutencao";
        } else {
            prefix = "";
        }

        List<ChecklistDefinition> lista = new ArrayList<>();

        JSONArray checklistsArray = ChecklistRepository.getChecklistsArrayForModel(this, modelKey);
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

    public static class ChecklistDefinition {
        String id;
        String nome;
        List<ChecklistItemStatus> itens;

        ChecklistDefinition(String id, String nome, List<ChecklistItemStatus> itens) {
            this.id = id;
            this.nome = nome;
            this.itens = itens;
        }
    }

    public static class ChecklistItemStatus {
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

