package com.example.checklistjson;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ChecklistActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_PHOTO = 1001;
    private static final int REQUEST_CODE_TAKE_PHOTO = 1002;

    private RecyclerView recyclerView;
    private ChecklistAdapter adapter;
    private final List<ChecklistItem> itens = new ArrayList<>();
    private String checklistId;
    private ArrayList<String> listaIds;
    private ArrayList<String> listaNomes;
    private int indiceAtual = -1;
    private EditText etResponsavel;
    private EditText etData;
    private EditText etItChecklist;
    private EditText etOpChecklist;
    private EditText etObservacao;
    private LinearLayout layoutFotos;
    // Campos específicos para UCABR - Compressores (valores numéricos e responsáveis/data)
    private LinearLayout layoutCompressoresUcabr;
    private EditText etC1Tensao;
    private EditText etC1Fluido;
    private EditText etC1Oleo;
    private EditText etC2Tensao;
    private EditText etC2Fluido;
    private EditText etC2Oleo;
    private EditText etC1Data;
    private EditText etC1Responsavel;
    private EditText etC2Data;
    private EditText etC2Responsavel;
    // Campos específicos para UCABR - Montagem frigorífica (pressões válvulas de alívio)
    private LinearLayout layoutPressaoValvulasUcabr;
    private EditText etPressaoAlivio1;
    private EditText etPressaoAlivio1Dia2;
    private EditText etPressaoAlivio2;
    private EditText etPressaoAlivio2Dia2;
    // Campos específicos para UCABR - Teste de estanqueidade (C1 e C2)
    private LinearLayout layoutEstanqueidadeUcabr;
    private EditText etEstC1Data, etEstC1Resp, etEstC1HoraInicio, etEstC1HoraFim, etEstC1PsiInicio, etEstC1PsiFim;
    private EditText etEstC2Data, etEstC2Resp, etEstC2HoraInicio, etEstC2HoraFim, etEstC2PsiInicio, etEstC2PsiFim;
    private EditText etEstObs, etEstNumManometroC1, etEstNumManometroC2, etEstDataCalibracaoC1, etEstDataCalibracaoC2;
    // Campos específicos para UCABR - Vácuo e quebra do vácuo (C1 e C2)
    private LinearLayout layoutVacuoQuebraUcabr;
    private EditText etVacuoC1DataInicio, etVacuoC1DataFim, etVacuoC1Resp, etVacuoC1HoraInicio, etVacuoC1HoraFim, etVacuoC1Valor;
    private EditText etVacuoNumBomba, etVacuoNumVacuometro, etVacuoDataCalibracaoVacuometro;
    private EditText etQuebraComValvulaData, etQuebraQtdRefrigerante, etQuebraComValvulaResp;
    private EditText etQuebraSemValvulaData, etQuebraCargaNitrogenio, etQuebraSemValvulaResp;
    private EditText etQuebraDetectorData, etQuebraDetectorResp;
    private EditText etQuebraNumBalanca, etQuebraDataCalibracaoBalanca;
    private EditText etQuebraNumManometro, etQuebraDataCalibracaoManometro;
    // Quebra do vácuo C2
    private EditText etQuebra2ComValvulaData, etQuebra2QtdRefrigerante, etQuebra2ComValvulaResp;
    private EditText etQuebra2SemValvulaData, etQuebra2CargaNitrogenio, etQuebra2SemValvulaResp;
    private EditText etQuebra2DetectorData, etQuebra2DetectorResp;
    private EditText etQuebra2NumBalanca, etQuebra2DataCalibracaoBalanca;
    private EditText etQuebra2NumManometro, etQuebra2DataCalibracaoManometro;
    // Segundo teste de vácuo (C2)
    private EditText etVacuoC2DataInicio, etVacuoC2DataFim, etVacuoC2Resp, etVacuoC2HoraInicio, etVacuoC2HoraFim, etVacuoC2Valor;
    private EditText etVacuoNumBombaC2, etVacuoNumVacuometroC2, etVacuoDataCalibracaoVacuometroC2;
    // Campos específicos para IRBR - Montagem elétrica (instrumentos de medição)
    private LinearLayout layoutEletricaInstrumentos;
    private EditText etEletricaInstr1Num, etEletricaInstr1Data;
    private EditText etEletricaInstr2Num, etEletricaInstr2Data;
    private EditText etEletricaInstr3Num, etEletricaInstr3Data;
    private static final int QR_TARGET_NONE = 0;
    private static final int QR_TARGET_ELETRICA_1 = 1;
    private static final int QR_TARGET_ELETRICA_2 = 2;
    private static final int QR_TARGET_ELETRICA_3 = 3;
    private static final int QR_TARGET_EST_C1 = 4;
    private static final int QR_TARGET_EST_C2 = 5;
    private static final int QR_TARGET_VACUO_INSTR_C1 = 6;
    private static final int QR_TARGET_VACUO_INSTR_C2 = 7;
    private static final int QR_TARGET_QUEBRA_BALANCA_C1 = 8;
    private static final int QR_TARGET_QUEBRA_BALANCA_C2 = 9;
    private static final int QR_TARGET_QUEBRA_MANOMETRO_C1 = 11;
    private static final int QR_TARGET_QUEBRA_MANOMETRO_C2 = 12;
    private static final int QR_TARGET_IRBR_PRE_TORQUE = 10;
    private int qrTarget = QR_TARGET_NONE;
    private ActivityResultLauncher<Intent> qrScanLauncher;
    // IRBR - Pré-montagem: Torquímetro
    private LinearLayout layoutIrbrPreTorquimetro;
    private EditText etIrbrPreTorqueNum, etIrbrPreTorqueDataCal, etIrbrPreTorqueData, etIrbrPreTorqueResp;
    private final List<String> listaFotos = new ArrayList<>();
    private Uri fotoTempUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        qrScanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) return;
                    String raw = result.getData().getStringExtra(QrScanTestActivity.EXTRA_RESULT_RAW);
                    if (raw == null) raw = "";
                    aplicarQrPorTarget(qrTarget, raw);
                }
        );

        TextView tvTitulo = findViewById(R.id.tvChecklistTitulo);
        TextView tvResponsavelLabel = findViewById(R.id.tvResponsavelLabel);
        TextView tvDataLabel = findViewById(R.id.tvDataLabel);
        recyclerView = findViewById(R.id.recyclerViewChecklist);
        etResponsavel = findViewById(R.id.etResponsavel);
        etData = findViewById(R.id.etData);
        etItChecklist = findViewById(R.id.etItChecklist);
        etOpChecklist = findViewById(R.id.etOpChecklist);
        etObservacao = findViewById(R.id.etObservacao);
        layoutFotos = findViewById(R.id.layoutFotos);
        Button btnSalvarCabecalho = findViewById(R.id.btnSalvarCabecalho);
        Button btnAdicionarItem = findViewById(R.id.btnAdicionarItem);
        Button btnSalvarObservacao = findViewById(R.id.btnSalvarObservacao);
        Button btnAnterior = findViewById(R.id.btnAnteriorChecklist);
        Button btnProximo = findViewById(R.id.btnProximoChecklist);
        Button btnExportarModeloChecklist = findViewById(R.id.btnExportarModeloChecklist);
        Button btnAdicionarFoto = findViewById(R.id.btnAdicionarFoto);
        // Inicializa campos de compressores (UCABR)
        layoutCompressoresUcabr = findViewById(R.id.layoutCompressoresUcabr);
        etC1Tensao = findViewById(R.id.etC1Tensao);
        etC1Fluido = findViewById(R.id.etC1Fluido);
        etC1Oleo = findViewById(R.id.etC1Oleo);
        etC1Data = findViewById(R.id.etC1Data);
        etC1Responsavel = findViewById(R.id.etC1Responsavel);
        etC2Tensao = findViewById(R.id.etC2Tensao);
        etC2Fluido = findViewById(R.id.etC2Fluido);
        etC2Oleo = findViewById(R.id.etC2Oleo);
        etC2Data = findViewById(R.id.etC2Data);
        etC2Responsavel = findViewById(R.id.etC2Responsavel);
        Button btnSalvarCompressores = findViewById(R.id.btnSalvarCompressores);
        // Inicializa campos de pressão das válvulas de alívio (UCABR - Montagem frigorífica)
        layoutPressaoValvulasUcabr = findViewById(R.id.layoutPressaoValvulasUcabr);
        etPressaoAlivio1 = findViewById(R.id.etPressaoAlivio1);
        etPressaoAlivio1Dia2 = findViewById(R.id.etPressaoAlivio1Dia2);
        etPressaoAlivio2 = findViewById(R.id.etPressaoAlivio2);
        etPressaoAlivio2Dia2 = findViewById(R.id.etPressaoAlivio2Dia2);
        Button btnSalvarPressaoValvulas = findViewById(R.id.btnSalvarPressaoValvulas);
        // Inicializa campos do teste de estanqueidade (UCABR)
        layoutEstanqueidadeUcabr = findViewById(R.id.layoutEstanqueidadeUcabr);
        etEstC1Data = findViewById(R.id.etEstC1Data);
        etEstC1Resp = findViewById(R.id.etEstC1Resp);
        etEstC1HoraInicio = findViewById(R.id.etEstC1HoraInicio);
        etEstC1HoraFim = findViewById(R.id.etEstC1HoraFim);
        etEstC1PsiInicio = findViewById(R.id.etEstC1PsiInicio);
        etEstC1PsiFim = findViewById(R.id.etEstC1PsiFim);
        etEstC2Data = findViewById(R.id.etEstC2Data);
        etEstC2Resp = findViewById(R.id.etEstC2Resp);
        etEstC2HoraInicio = findViewById(R.id.etEstC2HoraInicio);
        etEstC2HoraFim = findViewById(R.id.etEstC2HoraFim);
        etEstC2PsiInicio = findViewById(R.id.etEstC2PsiInicio);
        etEstC2PsiFim = findViewById(R.id.etEstC2PsiFim);
        etEstObs = findViewById(R.id.etEstObs);
        etEstNumManometroC1 = findViewById(R.id.etEstNumManometroC1);
        etEstNumManometroC2 = findViewById(R.id.etEstNumManometroC2);
        etEstDataCalibracaoC1 = findViewById(R.id.etEstDataCalibracaoC1);
        etEstDataCalibracaoC2 = findViewById(R.id.etEstDataCalibracaoC2);
        Button btnSalvarEstanqueidade = findViewById(R.id.btnSalvarEstanqueidade);
        Button btnEstQrC1 = findViewById(R.id.btnEstQrC1);
        Button btnEstQrC2 = findViewById(R.id.btnEstQrC2);
        // Inicializa campos de vácuo e quebra de vácuo (UCABR)
        layoutVacuoQuebraUcabr = findViewById(R.id.layoutVacuoQuebraUcabr);
        etVacuoC1DataInicio = findViewById(R.id.etVacuoC1DataInicio);
        etVacuoC1DataFim = findViewById(R.id.etVacuoC1DataFim);
        etVacuoC1Resp = findViewById(R.id.etVacuoC1Resp);
        etVacuoC1HoraInicio = findViewById(R.id.etVacuoC1HoraInicio);
        etVacuoC1HoraFim = findViewById(R.id.etVacuoC1HoraFim);
        etVacuoC1Valor = findViewById(R.id.etVacuoC1Valor);
        etVacuoNumBomba = findViewById(R.id.etVacuoNumBomba);
        etVacuoNumVacuometro = findViewById(R.id.etVacuoNumVacuometro);
        etVacuoDataCalibracaoVacuometro = findViewById(R.id.etVacuoDataCalibracaoVacuometro);
        etQuebraComValvulaData = findViewById(R.id.etQuebraComValvulaData);
        etQuebraQtdRefrigerante = findViewById(R.id.etQuebraQtdRefrigerante);
        etQuebraComValvulaResp = findViewById(R.id.etQuebraComValvulaResp);
        etQuebraSemValvulaData = findViewById(R.id.etQuebraSemValvulaData);
        etQuebraCargaNitrogenio = findViewById(R.id.etQuebraCargaNitrogenio);
        etQuebraSemValvulaResp = findViewById(R.id.etQuebraSemValvulaResp);
        etQuebraDetectorData = findViewById(R.id.etQuebraDetectorData);
        etQuebraDetectorResp = findViewById(R.id.etQuebraDetectorResp);
        etQuebraNumBalanca = findViewById(R.id.etQuebraNumBalanca);
        etQuebraDataCalibracaoBalanca = findViewById(R.id.etQuebraDataCalibracaoBalanca);
        etQuebraNumManometro = findViewById(R.id.etQuebraNumManometro);
        etQuebraDataCalibracaoManometro = findViewById(R.id.etQuebraDataCalibracaoManometro);
        // Quebra do vácuo C2
        etQuebra2ComValvulaData = findViewById(R.id.etQuebra2ComValvulaData);
        etQuebra2QtdRefrigerante = findViewById(R.id.etQuebra2QtdRefrigerante);
        etQuebra2ComValvulaResp = findViewById(R.id.etQuebra2ComValvulaResp);
        etQuebra2SemValvulaData = findViewById(R.id.etQuebra2SemValvulaData);
        etQuebra2CargaNitrogenio = findViewById(R.id.etQuebra2CargaNitrogenio);
        etQuebra2SemValvulaResp = findViewById(R.id.etQuebra2SemValvulaResp);
        etQuebra2DetectorData = findViewById(R.id.etQuebra2DetectorData);
        etQuebra2DetectorResp = findViewById(R.id.etQuebra2DetectorResp);
        etQuebra2NumBalanca = findViewById(R.id.etQuebra2NumBalanca);
        etQuebra2DataCalibracaoBalanca = findViewById(R.id.etQuebra2DataCalibracaoBalanca);
        etQuebra2NumManometro = findViewById(R.id.etQuebra2NumManometro);
        etQuebra2DataCalibracaoManometro = findViewById(R.id.etQuebra2DataCalibracaoManometro);
        Button btnQuebraBalancaQr = findViewById(R.id.btnQuebraBalancaQr);
        Button btnQuebra2BalancaQr = findViewById(R.id.btnQuebra2BalancaQr);
        Button btnQuebraManometroQr = findViewById(R.id.btnQuebraManometroQr);
        Button btnQuebra2ManometroQr = findViewById(R.id.btnQuebra2ManometroQr);
        Button btnSalvarVacuoQuebra = findViewById(R.id.btnSalvarVacuoQuebra);
        etVacuoC2DataInicio = findViewById(R.id.etVacuoC2DataInicio);
        etVacuoC2DataFim = findViewById(R.id.etVacuoC2DataFim);
        etVacuoC2Resp = findViewById(R.id.etVacuoC2Resp);
        etVacuoC2HoraInicio = findViewById(R.id.etVacuoC2HoraInicio);
        etVacuoC2HoraFim = findViewById(R.id.etVacuoC2HoraFim);
        etVacuoC2Valor = findViewById(R.id.etVacuoC2Valor);
        etVacuoNumBombaC2 = findViewById(R.id.etVacuoNumBombaC2);
        etVacuoNumVacuometroC2 = findViewById(R.id.etVacuoNumVacuometroC2);
        etVacuoDataCalibracaoVacuometroC2 = findViewById(R.id.etVacuoDataCalibracaoVacuometroC2);
        Button btnVacuoInstrQrC1 = findViewById(R.id.btnVacuoInstrQrC1);
        Button btnVacuoInstrQrC2 = findViewById(R.id.btnVacuoInstrQrC2);

        // Inicializa campos de instrumentos (IRBR - Montagem elétrica)
        layoutEletricaInstrumentos = findViewById(R.id.layoutEletricaInstrumentos);
        etEletricaInstr1Num = findViewById(R.id.etEletricaInstr1Num);
        etEletricaInstr1Data = findViewById(R.id.etEletricaInstr1Data);
        etEletricaInstr2Num = findViewById(R.id.etEletricaInstr2Num);
        etEletricaInstr2Data = findViewById(R.id.etEletricaInstr2Data);
        etEletricaInstr3Num = findViewById(R.id.etEletricaInstr3Num);
        etEletricaInstr3Data = findViewById(R.id.etEletricaInstr3Data);
        Button btnSalvarEletricaInstrumentos = findViewById(R.id.btnSalvarEletricaInstrumentos);
        Button btnEletricaInstr1Qr = findViewById(R.id.btnEletricaInstr1Qr);
        Button btnEletricaInstr2Qr = findViewById(R.id.btnEletricaInstr2Qr);
        Button btnEletricaInstr3Qr = findViewById(R.id.btnEletricaInstr3Qr);

        // IRBR - Pré-montagem: Torquímetro
        layoutIrbrPreTorquimetro = findViewById(R.id.layoutIrbrPreTorquimetro);
        etIrbrPreTorqueNum = findViewById(R.id.etIrbrPreTorqueNum);
        etIrbrPreTorqueDataCal = findViewById(R.id.etIrbrPreTorqueDataCal);
        etIrbrPreTorqueData = findViewById(R.id.etIrbrPreTorqueData);
        etIrbrPreTorqueResp = findViewById(R.id.etIrbrPreTorqueResp);
        Button btnIrbrPreTorqueQr = findViewById(R.id.btnIrbrPreTorqueQr);
        Button btnSalvarIrbrPreTorquimetro = findViewById(R.id.btnSalvarIrbrPreTorquimetro);

        checklistId = getIntent().getStringExtra("checklist_id");
        String checklistNome = getIntent().getStringExtra("checklist_nome");

        listaIds = getIntent().getStringArrayListExtra("lista_ids");
        listaNomes = getIntent().getStringArrayListExtra("lista_nomes");
        indiceAtual = getIntent().getIntExtra("indice_atual", -1);

        if (checklistNome != null) {
            // adiciona OP atual no título, se existir
            String titulo = checklistNome;
            SharedPreferences prefsTitulo = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
            String headerKeyTitulo = getHeaderKeyForChecklist(checklistId);
            String opChecklistTitulo = prefsTitulo.getString(gerarChaveOpEquip(this, checklistId), "");
            String opModeloTitulo = prefsTitulo.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKeyTitulo, "op"), "");
            String opAtual = !opChecklistTitulo.isEmpty() ? opChecklistTitulo : opModeloTitulo;
            if (opAtual != null && !opAtual.isEmpty()) {
                titulo = checklistNome + " - OP " + opAtual;
            }
            tvTitulo.setText(titulo);
        }

        // Esconde campos de Responsável e Data do topo (já existem em outras telas/partes do fluxo),
        // exceto nos checklists de liberação final, onde eles são a informação principal.
        if (!"checklist_ucabr_liberacao_final".equals(checklistId)
                && !"checklist_irbr_liberacao_final".equals(checklistId)
                && !"checklist_edbrse_liberacao_final".equals(checklistId)
                && !"checklist_esbrag_liberacao_final".equals(checklistId)
                && !"checklist_cabr_liberacao_final".equals(checklistId)) {
            if (tvResponsavelLabel != null) tvResponsavelLabel.setVisibility(View.GONE);
            if (etResponsavel != null) etResponsavel.setVisibility(View.GONE);
            if (tvDataLabel != null) tvDataLabel.setVisibility(View.GONE);
            if (etData != null) etData.setVisibility(View.GONE);
        } else {
            // No checklist de liberação final, renomeia o rótulo para "Data da liberação"
            if (tvDataLabel != null) {
                tvDataLabel.setText("Data da liberação");
            }
        }

        // Se for o checklist específico de compressores (UCABR, IRBR ou EDBRSE/EUBRSE), exibe seção própria e esconde IT/OP/cabeçalho geral
        if ("checklist_ucabr_compressores".equals(checklistId) ||
                "checklist_irbr_compressores".equals(checklistId) ||
                "checklist_edbrse_compressores".equals(checklistId) ||
                "checklist_cabr_compressores".equals(checklistId)) {
            if (layoutCompressoresUcabr != null) {
                layoutCompressoresUcabr.setVisibility(View.VISIBLE);
                carregarCompressoresUcabr();
            }
            // esconder IT e OP deste checklist e responsável/data gerais e botão de salvar cabeçalho
            TextView tvItChecklistLabel = findViewById(R.id.tvItChecklistLabel);
            TextView tvOpChecklistLabel = findViewById(R.id.tvOpChecklistLabel);
            Button btnSalvarCabecalhoLocal = findViewById(R.id.btnSalvarCabecalho);
            if (tvItChecklistLabel != null) tvItChecklistLabel.setVisibility(View.GONE);
            if (etItChecklist != null) etItChecklist.setVisibility(View.GONE);
            if (tvOpChecklistLabel != null) tvOpChecklistLabel.setVisibility(View.GONE);
            if (etOpChecklist != null) etOpChecklist.setVisibility(View.GONE);
            if (btnSalvarCabecalhoLocal != null) btnSalvarCabecalhoLocal.setVisibility(View.GONE);
        } else {
            if (layoutCompressoresUcabr != null) {
                layoutCompressoresUcabr.setVisibility(View.GONE);
            }
        }

        // Se for o checklist de teste de estanqueidade / teste hidrostático (UCABR, IRBR, EDBRSE/EUBRSE ou ESBRAG/ESBRHAG), esconder apenas IT e OP
        if ("checklist_ucabr_teste_estanqueidade".equals(checklistId) ||
                "checklist_irbr_teste_estanqueidade".equals(checklistId) ||
                "checklist_edbrse_teste_estanqueidade".equals(checklistId) ||
                "checklist_esbrag_teste_hidrostatico".equals(checklistId) ||
                "checklist_cabr_teste_estanqueidade".equals(checklistId)) {
            TextView tvItChecklistLabel2 = findViewById(R.id.tvItChecklistLabel);
            TextView tvOpChecklistLabel2 = findViewById(R.id.tvOpChecklistLabel);
            if (tvItChecklistLabel2 != null) tvItChecklistLabel2.setVisibility(View.GONE);
            if (etItChecklist != null) etItChecklist.setVisibility(View.GONE);
            if (tvOpChecklistLabel2 != null) tvOpChecklistLabel2.setVisibility(View.GONE);
            if (etOpChecklist != null) etOpChecklist.setVisibility(View.GONE);
        }

        // Se for checklist de liberação final (UCABR, IRBR, EDBRSE/EUBRSE ou ESBRAG/ESBRHAG), esconder IT/OP e todas as seções especiais
        if ("checklist_ucabr_liberacao_final".equals(checklistId)
                || "checklist_irbr_liberacao_final".equals(checklistId)
                || "checklist_edbrse_liberacao_final".equals(checklistId)
                || "checklist_esbrag_liberacao_final".equals(checklistId)
                || "checklist_cabr_liberacao_final".equals(checklistId)) {
            TextView tvItChecklistLabel3 = findViewById(R.id.tvItChecklistLabel);
            TextView tvOpChecklistLabel3 = findViewById(R.id.tvOpChecklistLabel);
            Button btnRestaurar = findViewById(R.id.btnRestaurarItensPadrao);
            Button btnAdicionarItemTopo = findViewById(R.id.btnAdicionarItem);
            if (tvItChecklistLabel3 != null) tvItChecklistLabel3.setVisibility(View.GONE);
            if (etItChecklist != null) etItChecklist.setVisibility(View.GONE);
            if (tvOpChecklistLabel3 != null) tvOpChecklistLabel3.setVisibility(View.GONE);
            if (etOpChecklist != null) etOpChecklist.setVisibility(View.GONE);
            if (layoutCompressoresUcabr != null) layoutCompressoresUcabr.setVisibility(View.GONE);
            if (layoutPressaoValvulasUcabr != null) layoutPressaoValvulasUcabr.setVisibility(View.GONE);
            if (layoutEstanqueidadeUcabr != null) layoutEstanqueidadeUcabr.setVisibility(View.GONE);
            if (layoutVacuoQuebraUcabr != null) layoutVacuoQuebraUcabr.setVisibility(View.GONE);
            if (btnRestaurar != null) btnRestaurar.setVisibility(View.GONE);
            if (btnAdicionarItemTopo != null) btnAdicionarItemTopo.setVisibility(View.GONE);
        }

        // Se for o checklist de Montagem frigorífica (UCABR/IRBR/EDBRSE/CABR), exibe seção de pressão das válvulas de alívio
        if ("checklist_ucabr_montagem_frigorifica".equals(checklistId) ||
                "checklist_irbr_montagem_frigorifica".equals(checklistId) ||
                "checklist_edbrse_montagem_frigorifica".equals(checklistId) ||
                "checklist_cabr_montagem_frigorifica".equals(checklistId)) {
            if (layoutPressaoValvulasUcabr != null) {
                layoutPressaoValvulasUcabr.setVisibility(View.VISIBLE);
                carregarPressaoValvulasUcabr();

                TextView tvTituloPressao = findViewById(R.id.tvPressaoValvulasTitulo);
                if (tvTituloPressao != null) {
                    tvTituloPressao.setText("Pressão válvulas de alívio");
                }

                // No EDBRSE/EUBRSE, o formulário pede apenas 1 valor por válvula (sem dia 2)
                if ("checklist_edbrse_montagem_frigorifica".equals(checklistId)) {
                    if (etPressaoAlivio1Dia2 != null) etPressaoAlivio1Dia2.setVisibility(View.GONE);
                    if (etPressaoAlivio2Dia2 != null) etPressaoAlivio2Dia2.setVisibility(View.GONE);
                    if (etPressaoAlivio1 != null) etPressaoAlivio1.setHint("Valor (kgf/cm²)");
                    if (etPressaoAlivio2 != null) etPressaoAlivio2.setHint("Valor (kgf/cm²)");
                } else {
                    if (etPressaoAlivio1Dia2 != null) etPressaoAlivio1Dia2.setVisibility(View.VISIBLE);
                    if (etPressaoAlivio2Dia2 != null) etPressaoAlivio2Dia2.setVisibility(View.VISIBLE);
                    if (etPressaoAlivio1 != null) etPressaoAlivio1.setHint("Valor dia 1 (kgf/cm²)");
                    if (etPressaoAlivio2 != null) etPressaoAlivio2.setHint("Valor dia 1 (kgf/cm²)");
                }
            }
        } else if (layoutPressaoValvulasUcabr != null) {
            layoutPressaoValvulasUcabr.setVisibility(View.GONE);
        }

        // Se for o checklist de Teste de estanqueidade / Teste hidrostático (UCABR, IRBR, EDBRSE/EUBRSE ou ESBRAG/ESBRHAG), exibe seção específica
        if ("checklist_ucabr_teste_estanqueidade".equals(checklistId) ||
                "checklist_irbr_teste_estanqueidade".equals(checklistId) ||
                "checklist_edbrse_teste_estanqueidade".equals(checklistId) ||
                "checklist_esbrag_teste_hidrostatico".equals(checklistId) ||
                "checklist_cabr_teste_estanqueidade".equals(checklistId)) {
            if (layoutEstanqueidadeUcabr != null) {
                layoutEstanqueidadeUcabr.setVisibility(View.VISIBLE);
                carregarEstanqueidadeUcabr();
            }

            // QR para instrumentos C1/C2 (preenche nº e data calibração)
            if (btnEstQrC1 != null) btnEstQrC1.setOnClickListener(v -> iniciarLeituraQr(QR_TARGET_EST_C1));
            if (btnEstQrC2 != null) btnEstQrC2.setOnClickListener(v -> iniciarLeituraQr(QR_TARGET_EST_C2));
        } else if (layoutEstanqueidadeUcabr != null) {
            layoutEstanqueidadeUcabr.setVisibility(View.GONE);
        }

        // Se for o checklist de Vácuo e quebra do vácuo (UCABR, IRBR ou EDBRSE/EUBRSE), exibe seção específica
        if ("checklist_ucabr_vacuo_quebra".equals(checklistId) ||
                "checklist_irbr_vacuo_quebra".equals(checklistId) ||
                "checklist_edbrse_vacuo_quebra".equals(checklistId) ||
                "checklist_cabr_vacuo_quebra".equals(checklistId)) {
            if (layoutVacuoQuebraUcabr != null) {
                layoutVacuoQuebraUcabr.setVisibility(View.VISIBLE);
                carregarVacuoQuebraUcabr();
            }

            if (btnVacuoInstrQrC1 != null) btnVacuoInstrQrC1.setOnClickListener(v -> iniciarLeituraQr(QR_TARGET_VACUO_INSTR_C1));
            if (btnVacuoInstrQrC2 != null) btnVacuoInstrQrC2.setOnClickListener(v -> iniciarLeituraQr(QR_TARGET_VACUO_INSTR_C2));
            if (btnQuebraBalancaQr != null) btnQuebraBalancaQr.setOnClickListener(v -> iniciarLeituraQr(QR_TARGET_QUEBRA_BALANCA_C1));
            if (btnQuebra2BalancaQr != null) btnQuebra2BalancaQr.setOnClickListener(v -> iniciarLeituraQr(QR_TARGET_QUEBRA_BALANCA_C2));
            if (btnQuebraManometroQr != null) btnQuebraManometroQr.setOnClickListener(v -> iniciarLeituraQr(QR_TARGET_QUEBRA_MANOMETRO_C1));
            if (btnQuebra2ManometroQr != null) btnQuebra2ManometroQr.setOnClickListener(v -> iniciarLeituraQr(QR_TARGET_QUEBRA_MANOMETRO_C2));
        } else if (layoutVacuoQuebraUcabr != null) {
            layoutVacuoQuebraUcabr.setVisibility(View.GONE);
        }

        // Se for o checklist de Montagem elétrica (IRBR/UCABR/EDBRSE), exibe seção de instrumentos no final
        if ("checklist_irbr_montagem_eletrica".equals(checklistId)
                || "checklist_ucabr_montagem_eletrica".equals(checklistId)
                || "checklist_edbrse_montagem_eletrica".equals(checklistId)) {
            if (layoutEletricaInstrumentos != null) {
                layoutEletricaInstrumentos.setVisibility(View.VISIBLE);
                carregarEletricaInstrumentos();
            }
            configurarDatePicker(etEletricaInstr1Data);
            configurarDatePicker(etEletricaInstr2Data);
            configurarDatePicker(etEletricaInstr3Data);

            if (btnEletricaInstr1Qr != null) btnEletricaInstr1Qr.setOnClickListener(v -> iniciarLeituraQr(QR_TARGET_ELETRICA_1));
            if (btnEletricaInstr2Qr != null) btnEletricaInstr2Qr.setOnClickListener(v -> iniciarLeituraQr(QR_TARGET_ELETRICA_2));
            if (btnEletricaInstr3Qr != null) btnEletricaInstr3Qr.setOnClickListener(v -> iniciarLeituraQr(QR_TARGET_ELETRICA_3));
        } else if (layoutEletricaInstrumentos != null) {
            layoutEletricaInstrumentos.setVisibility(View.GONE);
        }

        // Se for o checklist de Pré-montagem (IRBR/UCABR/EDBRSE/CABR), exibe bloco do torquímetro
        if ("checklist_irbr_pre_montagem".equals(checklistId)
                || "checklist_ucabr_pre_montagem".equals(checklistId)
                || "checklist_edbrse_pre_montagem".equals(checklistId)
                || "checklist_cabr_pre_montagem".equals(checklistId)) {
            if (layoutIrbrPreTorquimetro != null) {
                layoutIrbrPreTorquimetro.setVisibility(View.VISIBLE);
                carregarIrbrPreTorquimetro();
            }
            configurarDatePicker(etIrbrPreTorqueDataCal);
            configurarDatePicker(etIrbrPreTorqueData);
            if (btnIrbrPreTorqueQr != null) btnIrbrPreTorqueQr.setOnClickListener(v -> iniciarLeituraQr(QR_TARGET_IRBR_PRE_TORQUE));
        } else if (layoutIrbrPreTorquimetro != null) {
            layoutIrbrPreTorquimetro.setVisibility(View.GONE);
        }

        // Configura navegação apenas quando viemos de um menu com lista (ex.: IRBR)
        if (listaIds == null || listaIds.isEmpty()) {
            btnAnterior.setVisibility(View.GONE);
            btnProximo.setVisibility(View.GONE);
        } else {
            // Se índice não veio, tenta descobrir pelo ID atual
            if (indiceAtual < 0) {
                indiceAtual = listaIds.indexOf(checklistId);
            }

            if (indiceAtual <= 0) {
                btnAnterior.setEnabled(false);
            }
            if (indiceAtual >= listaIds.size() - 1) {
                btnProximo.setEnabled(false);
            }
        }

        // Carrega itens e cabeçalho (responsável/data/IT/OP)
        carregarItensDoChecklist();
        carregarCabecalho();
        carregarItChecklist();
        carregarOpChecklist();
        carregarObservacao();
        carregarFotos();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Permite que o RecyclerView role independentemente dentro do ScrollView,
        // garantindo visualização de todos os itens mesmo em listas longas.
        recyclerView.setNestedScrollingEnabled(true);
        adapter = new ChecklistAdapter(itens, checklistId, this, new ChecklistAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position, ChecklistItem item) {
                confirmarRemocaoItem(position, item);
            }
        });
        recyclerView.setAdapter(adapter);

        // Como o cabeçalho principal (Responsável/Data) não é mais usado nesta tela,
        // o botão de salvar cabeçalho fica oculto para evitar confusão.
        if (btnSalvarCabecalho != null) {
            btnSalvarCabecalho.setVisibility(View.GONE);
        }

        btnAdicionarItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Nas telas de liberação final não é permitido adicionar itens
                if ("checklist_ucabr_liberacao_final".equals(checklistId)
                        || "checklist_irbr_liberacao_final".equals(checklistId)
                        || "checklist_edbrse_liberacao_final".equals(checklistId)
                        || "checklist_esbrag_liberacao_final".equals(checklistId)) return;
                mostrarDialogAdicionarItem();
            }
        });

        btnSalvarObservacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarObservacao();
            }
        });

        Button btnRestaurar = findViewById(R.id.btnRestaurarItensPadrao);
        btnRestaurar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Nas telas de liberação final não há itens padrão para restaurar
                if ("checklist_ucabr_liberacao_final".equals(checklistId)
                        || "checklist_irbr_liberacao_final".equals(checklistId)
                        || "checklist_edbrse_liberacao_final".equals(checklistId)
                        || "checklist_esbrag_liberacao_final".equals(checklistId)) return;
                new AlertDialog.Builder(ChecklistActivity.this)
                        .setTitle("Restaurar itens padrão")
                        .setMessage("Isso vai restaurar todos os itens originais deste checklist e remover itens personalizados. Deseja continuar?")
                        .setPositiveButton("Sim", (dialog, which) -> restaurarChecklistPadrao())
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });

        if (!"checklist_ucabr_compressores".equals(checklistId) &&
                !"checklist_irbr_compressores".equals(checklistId) &&
                !"checklist_edbrse_compressores".equals(checklistId)) {
            etData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mostrarDatePicker(etData);
                }
            });
        }

        btnAnterior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navegarParaChecklist(-1);
            }
        });

        btnProximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navegarParaChecklist(1);
            }
        });

        btnAdicionarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escolherFoto();
            }
        });

        if (btnSalvarCompressores != null) {
            btnSalvarCompressores.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    salvarCompressoresUcabr();
                }
            });
        }

        if (btnSalvarPressaoValvulas != null) {
            btnSalvarPressaoValvulas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    salvarPressaoValvulasUcabr();
                }
            });
        }

        if (btnSalvarEstanqueidade != null) {
            btnSalvarEstanqueidade.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    salvarEstanqueidadeUcabr();
                }
            });
        }

        if (btnSalvarVacuoQuebra != null) {
            btnSalvarVacuoQuebra.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    salvarVacuoQuebraUcabr();
                }
            });
        }

        if (btnSalvarEletricaInstrumentos != null) {
            btnSalvarEletricaInstrumentos.setOnClickListener(v -> salvarEletricaInstrumentos());
        }

        if (btnSalvarIrbrPreTorquimetro != null) {
            btnSalvarIrbrPreTorquimetro.setOnClickListener(v -> salvarIrbrPreTorquimetro());
        }

        // Botão de exportar modelo inteiro (visível apenas em alguns casos)
        String modelKey = getHeaderKeyForChecklist(checklistId);
        String modelName = null;
        if ("irbr".equals(modelKey) && "checklist_irbr_liberacao_final".equals(checklistId)) {
            modelName = "Modelo IRBR";
        } else if ("ucabr".equals(modelKey) && "checklist_ucabr_liberacao_final".equals(checklistId)) {
            modelName = "Modelo UCABR";
        } else if ("edbrse".equals(modelKey) && "checklist_edbrse_liberacao_final".equals(checklistId)) {
            modelName = "Modelo EDBRSE/EUBRSE";
        } else if ("esbrag".equals(modelKey) && "checklist_esbrag_liberacao_final".equals(checklistId)) {
            modelName = "Modelo ESBRAG/ESBRHAG";
        } else if ("cabr".equals(modelKey) && "checklist_cabr_liberacao_final".equals(checklistId)) {
            modelName = "Modelo CABR";
        } else if ("manutencao".equals(modelKey) && "checklist_manutencao".equals(checklistId)) {
            modelName = "Modelo Manutenção (Exemplo)";
        }

        if (modelName != null) {
            btnExportarModeloChecklist.setVisibility(View.VISIBLE);
            String finalModelName = modelName;
            btnExportarModeloChecklist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ChecklistActivity.this, ModelExportActivity.class);
                    intent.putExtra("model_key", modelKey);
                    intent.putExtra("model_name", finalModelName);
                    startActivity(intent);
                }
            });
        } else {
            btnExportarModeloChecklist.setVisibility(View.GONE);
        }
    }

    private void carregarItensDoChecklist() {
        try {
            JSONObject checklistSelecionado = ChecklistRepository.getChecklistById(this, checklistId);
            if (checklistSelecionado == null) return;

            // Nas liberações finais (UCABR, IRBR, EDBRSE/EUBRSE, ESBRAG/ESBRHAG e CABR), não exibimos itens SIM/NÃO, apenas cabeçalho e observação.
            if ("checklist_ucabr_liberacao_final".equals(checklistId)
                    || "checklist_irbr_liberacao_final".equals(checklistId)
                    || "checklist_edbrse_liberacao_final".equals(checklistId)
                    || "checklist_esbrag_liberacao_final".equals(checklistId)
                    || "checklist_cabr_liberacao_final".equals(checklistId)) {
                return;
            }

            JSONArray itensArray = checklistSelecionado.getJSONArray("itens");
            SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);

            List<String> idsRemovidos = carregarIdsRemovidos();
            // Garantia: para os checklists de Montagem frigorífica,
            // sempre mostrar todos os itens ignorando qualquer remoção antiga salva em SharedPreferences.
            if ("checklist_ucabr_montagem_frigorifica".equals(checklistId) ||
                    "checklist_irbr_montagem_frigorifica".equals(checklistId) ||
                    "checklist_edbrse_montagem_frigorifica".equals(checklistId) ||
                    "checklist_cabr_montagem_frigorifica".equals(checklistId)) {
                idsRemovidos.clear();
            }

            for (int i = 0; i < itensArray.length(); i++) {
                JSONObject itemObj = itensArray.getJSONObject(i);
                String idItem = itemObj.getString("id");
                String titulo = itemObj.getString("titulo");

                if (idsRemovidos.contains(idItem)) {
                    continue;
                }

                String chaveStatus = gerarChaveStatusEquip(this, checklistId, idItem);
                String status = prefs.getString(chaveStatus, "");

                // Compatibilidade com estado antigo (boolean SIM)
                if (status.isEmpty()) {
                    String chaveAntiga = gerarChavePref(checklistId, idItem);
                    boolean antigoChecked = prefs.getBoolean(chaveAntiga, false);
                    if (antigoChecked) {
                        status = "SIM";
                    }
                }

                itens.add(new ChecklistItem(idItem, titulo, status));
            }

            // Itens customizados adicionados pelo usuário
            List<ChecklistItem> itensCustom = carregarItensCustomizados();
            for (ChecklistItem customItem : itensCustom) {
                String chaveStatus = gerarChaveStatusEquip(this, checklistId, customItem.getId());
                String status = prefs.getString(chaveStatus, "");
                customItem.setStatus(status);
                itens.add(customItem);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void carregarCabecalho() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        String chaveResp = gerarChaveRespEquip(this, checklistId);
        String chaveData = gerarChaveDataEquip(this, checklistId);

        String responsavel = prefs.getString(chaveResp, "");
        String data = prefs.getString(chaveData, "");

        etResponsavel.setText(responsavel);
        etData.setText(data);
    }

    private void salvarCabecalho() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String chaveResp = gerarChaveRespEquip(this, checklistId);
        String chaveData = gerarChaveDataEquip(this, checklistId);

        String responsavel = etResponsavel.getText().toString();
        String data = etData.getText().toString();

        editor.putString(chaveResp, responsavel);
        editor.putString(chaveData, data);
        editor.apply();

        // também salva IT e OP específicos deste checklist
        salvarItChecklist();
        salvarOpChecklist();
    }

    private void carregarItChecklist() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        String chaveIt = gerarChaveItEquip(this, checklistId);
        String itChecklist = prefs.getString(chaveIt, "");
        etItChecklist.setText(itChecklist);
    }

    private void salvarItChecklist() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String chaveIt = gerarChaveItEquip(this, checklistId);
        String itChecklist = etItChecklist.getText().toString();
        editor.putString(chaveIt, itChecklist);
        editor.apply();
    }

    private void carregarOpChecklist() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        String chaveOp = gerarChaveOp(checklistId);
        String opChecklist = prefs.getString(chaveOp, "");
        etOpChecklist.setText(opChecklist);
    }

    private void salvarOpChecklist() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String chaveOp = gerarChaveOp(checklistId);
        String opChecklist = etOpChecklist.getText().toString();
        editor.putString(chaveOp, opChecklist);
        editor.apply();
    }

    private void carregarObservacao() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        String chaveObs = gerarChaveObsEquip(this, checklistId);
        String obs = prefs.getString(chaveObs, "");
        etObservacao.setText(obs);
    }

    private void salvarObservacao() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String chaveObs = gerarChaveObsEquip(this, checklistId);
        String obs = etObservacao.getText().toString();
        editor.putString(chaveObs, obs);
        editor.apply();
    }

    // -----------------------------
    // Campos numéricos UCABR - Compressores
    // -----------------------------

    private void carregarCompressoresUcabr() {
        if (layoutCompressoresUcabr == null) return;

        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);

        String c1Tensao = prefs.getString(gerarChaveComp(this, checklistId, "c1_tensao"), "");
        String c1Fluido = prefs.getString(gerarChaveComp(this, checklistId, "c1_fluido"), "");
        String c1Oleo = prefs.getString(gerarChaveComp(this, checklistId, "c1_oleo"), "");
        String c1Data = prefs.getString(gerarChaveComp(this, checklistId, "c1_data"), "");
        String c1Resp = prefs.getString(gerarChaveComp(this, checklistId, "c1_resp"), "");
        String c2Tensao = prefs.getString(gerarChaveComp(this, checklistId, "c2_tensao"), "");
        String c2Fluido = prefs.getString(gerarChaveComp(this, checklistId, "c2_fluido"), "");
        String c2Oleo = prefs.getString(gerarChaveComp(this, checklistId, "c2_oleo"), "");
        String c2Data = prefs.getString(gerarChaveComp(this, checklistId, "c2_data"), "");
        String c2Resp = prefs.getString(gerarChaveComp(this, checklistId, "c2_resp"), "");

        etC1Tensao.setText(c1Tensao);
        etC1Fluido.setText(c1Fluido);
        etC1Oleo.setText(c1Oleo);
        etC1Data.setText(c1Data);
        etC1Responsavel.setText(c1Resp);
        etC2Tensao.setText(c2Tensao);
        etC2Fluido.setText(c2Fluido);
        etC2Oleo.setText(c2Oleo);
        etC2Data.setText(c2Data);
        etC2Responsavel.setText(c2Resp);

        // DatePicker para datas específicas dos compressores
        if (etC1Data != null) {
            etC1Data.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mostrarDatePicker(etC1Data);
                }
            });
        }
        if (etC2Data != null) {
            etC2Data.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mostrarDatePicker(etC2Data);
                }
            });
        }
    }

    private void salvarCompressoresUcabr() {
        if (layoutCompressoresUcabr == null) return;

        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(gerarChaveComp(this, checklistId, "c1_tensao"), etC1Tensao.getText().toString());
        editor.putString(gerarChaveComp(this, checklistId, "c1_fluido"), etC1Fluido.getText().toString());
        editor.putString(gerarChaveComp(this, checklistId, "c1_oleo"), etC1Oleo.getText().toString());
        editor.putString(gerarChaveComp(this, checklistId, "c1_data"), etC1Data.getText().toString());
        editor.putString(gerarChaveComp(this, checklistId, "c1_resp"), etC1Responsavel.getText().toString());
        editor.putString(gerarChaveComp(this, checklistId, "c2_tensao"), etC2Tensao.getText().toString());
        editor.putString(gerarChaveComp(this, checklistId, "c2_fluido"), etC2Fluido.getText().toString());
        editor.putString(gerarChaveComp(this, checklistId, "c2_oleo"), etC2Oleo.getText().toString());
        editor.putString(gerarChaveComp(this, checklistId, "c2_data"), etC2Data.getText().toString());
        editor.putString(gerarChaveComp(this, checklistId, "c2_resp"), etC2Responsavel.getText().toString());

        editor.apply();
        Toast.makeText(this, "Dados dos compressores salvos", Toast.LENGTH_SHORT).show();
    }

    // -----------------------------
    // Campos numéricos UCABR - Montagem frigorífica (pressões válvulas de alívio)
    // -----------------------------

    private void carregarPressaoValvulasUcabr() {
        if (layoutPressaoValvulasUcabr == null) return;

        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);

        String p1Dia1 = prefs.getString(gerarChavePressaoValvula(this, checklistId, "alivio1_dia1"), "");
        String p1Dia2 = prefs.getString(gerarChavePressaoValvula(this, checklistId, "alivio1_dia2"), "");
        String p2Dia1 = prefs.getString(gerarChavePressaoValvula(this, checklistId, "alivio2_dia1"), "");
        String p2Dia2 = prefs.getString(gerarChavePressaoValvula(this, checklistId, "alivio2_dia2"), "");

        etPressaoAlivio1.setText(p1Dia1);
        etPressaoAlivio1Dia2.setText(p1Dia2);
        etPressaoAlivio2.setText(p2Dia1);
        etPressaoAlivio2Dia2.setText(p2Dia2);
    }

    private void salvarPressaoValvulasUcabr() {
        if (layoutPressaoValvulasUcabr == null) return;

        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(gerarChavePressaoValvula(this, checklistId, "alivio1_dia1"), etPressaoAlivio1.getText().toString());
        editor.putString(gerarChavePressaoValvula(this, checklistId, "alivio1_dia2"), etPressaoAlivio1Dia2.getText().toString());
        editor.putString(gerarChavePressaoValvula(this, checklistId, "alivio2_dia1"), etPressaoAlivio2.getText().toString());
        editor.putString(gerarChavePressaoValvula(this, checklistId, "alivio2_dia2"), etPressaoAlivio2Dia2.getText().toString());

        editor.apply();
        Toast.makeText(this, "Pressões das válvulas salvas", Toast.LENGTH_SHORT).show();
    }

    // -----------------------------
    // Campos UCABR - Teste de estanqueidade (C1 e C2)
    // -----------------------------

    private void carregarEstanqueidadeUcabr() {
        if (layoutEstanqueidadeUcabr == null) return;

        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);

        etEstC1Data.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "c1_data"), ""));
        etEstC1Resp.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "c1_resp"), ""));
        etEstC1HoraInicio.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "c1_hora_inicio"), ""));
        etEstC1HoraFim.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "c1_hora_fim"), ""));
        etEstC1PsiInicio.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "c1_psi_inicio"), ""));
        etEstC1PsiFim.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "c1_psi_fim"), ""));

        etEstC2Data.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "c2_data"), ""));
        etEstC2Resp.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "c2_resp"), ""));
        etEstC2HoraInicio.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "c2_hora_inicio"), ""));
        etEstC2HoraFim.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "c2_hora_fim"), ""));
        etEstC2PsiInicio.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "c2_psi_inicio"), ""));
        etEstC2PsiFim.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "c2_psi_fim"), ""));

        etEstObs.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "obs"), ""));
        etEstNumManometroC1.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "num_manometro_c1"), ""));
        etEstNumManometroC2.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "num_manometro_c2"), ""));
        etEstDataCalibracaoC1.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "data_calibracao_c1"), ""));
        etEstDataCalibracaoC2.setText(prefs.getString(gerarChaveEstanq(this, checklistId, "data_calibracao_c2"), ""));

        // DatePicker para datas específicas do teste
        etEstC1Data.setOnClickListener(v -> mostrarDatePicker(etEstC1Data));
        etEstC2Data.setOnClickListener(v -> mostrarDatePicker(etEstC2Data));
        etEstDataCalibracaoC1.setOnClickListener(v -> mostrarDatePicker(etEstDataCalibracaoC1));
        etEstDataCalibracaoC2.setOnClickListener(v -> mostrarDatePicker(etEstDataCalibracaoC2));
    }

    private void salvarEstanqueidadeUcabr() {
        if (layoutEstanqueidadeUcabr == null) return;

        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(gerarChaveEstanq(this, checklistId, "c1_data"), etEstC1Data.getText().toString());
        editor.putString(gerarChaveEstanq(this, checklistId, "c1_resp"), etEstC1Resp.getText().toString());
        editor.putString(gerarChaveEstanq(this, checklistId, "c1_hora_inicio"), etEstC1HoraInicio.getText().toString());
        editor.putString(gerarChaveEstanq(this, checklistId, "c1_hora_fim"), etEstC1HoraFim.getText().toString());
        editor.putString(gerarChaveEstanq(this, checklistId, "c1_psi_inicio"), etEstC1PsiInicio.getText().toString());
        editor.putString(gerarChaveEstanq(this, checklistId, "c1_psi_fim"), etEstC1PsiFim.getText().toString());

        editor.putString(gerarChaveEstanq(this, checklistId, "c2_data"), etEstC2Data.getText().toString());
        editor.putString(gerarChaveEstanq(this, checklistId, "c2_resp"), etEstC2Resp.getText().toString());
        editor.putString(gerarChaveEstanq(this, checklistId, "c2_hora_inicio"), etEstC2HoraInicio.getText().toString());
        editor.putString(gerarChaveEstanq(this, checklistId, "c2_hora_fim"), etEstC2HoraFim.getText().toString());
        editor.putString(gerarChaveEstanq(this, checklistId, "c2_psi_inicio"), etEstC2PsiInicio.getText().toString());
        editor.putString(gerarChaveEstanq(this, checklistId, "c2_psi_fim"), etEstC2PsiFim.getText().toString());

        editor.putString(gerarChaveEstanq(this, checklistId, "obs"), etEstObs.getText().toString());
        editor.putString(gerarChaveEstanq(this, checklistId, "num_manometro_c1"), etEstNumManometroC1.getText().toString());
        editor.putString(gerarChaveEstanq(this, checklistId, "num_manometro_c2"), etEstNumManometroC2.getText().toString());
        editor.putString(gerarChaveEstanq(this, checklistId, "data_calibracao_c1"), etEstDataCalibracaoC1.getText().toString());
        editor.putString(gerarChaveEstanq(this, checklistId, "data_calibracao_c2"), etEstDataCalibracaoC2.getText().toString());

        editor.apply();
        Toast.makeText(this, "Dados do teste de estanqueidade salvos", Toast.LENGTH_SHORT).show();
    }

    // -----------------------------
    // Campos UCABR - Vácuo e quebra do vácuo (C1)
    // -----------------------------

    private void carregarVacuoQuebraUcabr() {
        if (layoutVacuoQuebraUcabr == null) return;

        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);

        etVacuoC1DataInicio.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac1_data_inicio"), ""));
        etVacuoC1DataFim.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac1_data_fim"), ""));
        etVacuoC1Resp.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac1_resp"), ""));
        etVacuoC1HoraInicio.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac1_hora_inicio"), ""));
        etVacuoC1HoraFim.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac1_hora_fim"), ""));
        etVacuoC1Valor.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac1_valor"), ""));
        etVacuoNumBomba.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac1_num_bomba"), ""));
        etVacuoNumVacuometro.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac1_num_vacuometro"), ""));
        etVacuoDataCalibracaoVacuometro.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac1_data_cal_vacuometro"), ""));

        etVacuoC2DataInicio.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac2_data_inicio"), ""));
        etVacuoC2DataFim.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac2_data_fim"), ""));
        etVacuoC2Resp.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac2_resp"), ""));
        etVacuoC2HoraInicio.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac2_hora_inicio"), ""));
        etVacuoC2HoraFim.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac2_hora_fim"), ""));
        etVacuoC2Valor.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac2_valor"), ""));
        etVacuoNumBombaC2.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac2_num_bomba"), ""));
        etVacuoNumVacuometroC2.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac2_num_vacuometro"), ""));
        etVacuoDataCalibracaoVacuometroC2.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "vac2_data_cal_vacuometro"), ""));

        etQuebraComValvulaData.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra_com_data"), ""));
        etQuebraQtdRefrigerante.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra_qtd_refri"), ""));
        etQuebraComValvulaResp.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra_com_resp"), ""));

        etQuebraSemValvulaData.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra_sem_data"), ""));
        etQuebraCargaNitrogenio.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra_carga_n2"), ""));
        etQuebraSemValvulaResp.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra_sem_resp"), ""));

        etQuebraDetectorData.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra_det_data"), ""));
        etQuebraDetectorResp.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra_det_resp"), ""));

        etQuebraNumBalanca.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra_num_balanca"), ""));
        etQuebraDataCalibracaoBalanca.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra_data_cal_balanca"), ""));
        etQuebraNumManometro.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra_num_manometro"), ""));
        etQuebraDataCalibracaoManometro.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra_data_cal_manometro"), ""));

        // Quebra do vácuo C2
        etQuebra2ComValvulaData.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra2_com_data"), ""));
        etQuebra2QtdRefrigerante.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra2_qtd_refri"), ""));
        etQuebra2ComValvulaResp.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra2_com_resp"), ""));

        etQuebra2SemValvulaData.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra2_sem_data"), ""));
        etQuebra2CargaNitrogenio.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra2_carga_n2"), ""));
        etQuebra2SemValvulaResp.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra2_sem_resp"), ""));

        etQuebra2DetectorData.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra2_det_data"), ""));
        etQuebra2DetectorResp.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra2_det_resp"), ""));

        etQuebra2NumBalanca.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra2_num_balanca"), ""));
        etQuebra2DataCalibracaoBalanca.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra2_data_cal_balanca"), ""));
        etQuebra2NumManometro.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra2_num_manometro"), ""));
        etQuebra2DataCalibracaoManometro.setText(prefs.getString(gerarChaveVacuo(this, checklistId, "quebra2_data_cal_manometro"), ""));

        // DatePicker para datas
        etVacuoC1DataInicio.setOnClickListener(v -> mostrarDatePicker(etVacuoC1DataInicio));
        etVacuoC1DataFim.setOnClickListener(v -> mostrarDatePicker(etVacuoC1DataFim));
        etVacuoDataCalibracaoVacuometro.setOnClickListener(v -> mostrarDatePicker(etVacuoDataCalibracaoVacuometro));
        etVacuoC2DataInicio.setOnClickListener(v -> mostrarDatePicker(etVacuoC2DataInicio));
        etVacuoC2DataFim.setOnClickListener(v -> mostrarDatePicker(etVacuoC2DataFim));
        etVacuoDataCalibracaoVacuometroC2.setOnClickListener(v -> mostrarDatePicker(etVacuoDataCalibracaoVacuometroC2));
        etQuebraComValvulaData.setOnClickListener(v -> mostrarDatePicker(etQuebraComValvulaData));
        etQuebraSemValvulaData.setOnClickListener(v -> mostrarDatePicker(etQuebraSemValvulaData));
        etQuebraDetectorData.setOnClickListener(v -> mostrarDatePicker(etQuebraDetectorData));
        etQuebraDataCalibracaoBalanca.setOnClickListener(v -> mostrarDatePicker(etQuebraDataCalibracaoBalanca));
        etQuebraDataCalibracaoManometro.setOnClickListener(v -> mostrarDatePicker(etQuebraDataCalibracaoManometro));
        etQuebra2ComValvulaData.setOnClickListener(v -> mostrarDatePicker(etQuebra2ComValvulaData));
        etQuebra2SemValvulaData.setOnClickListener(v -> mostrarDatePicker(etQuebra2SemValvulaData));
        etQuebra2DetectorData.setOnClickListener(v -> mostrarDatePicker(etQuebra2DetectorData));
        etQuebra2DataCalibracaoBalanca.setOnClickListener(v -> mostrarDatePicker(etQuebra2DataCalibracaoBalanca));
        etQuebra2DataCalibracaoManometro.setOnClickListener(v -> mostrarDatePicker(etQuebra2DataCalibracaoManometro));
    }

    private void salvarVacuoQuebraUcabr() {
        if (layoutVacuoQuebraUcabr == null) return;

        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(gerarChaveVacuo(this, checklistId, "vac1_data_inicio"), etVacuoC1DataInicio.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "vac1_data_fim"), etVacuoC1DataFim.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "vac1_resp"), etVacuoC1Resp.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "vac1_hora_inicio"), etVacuoC1HoraInicio.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "vac1_hora_fim"), etVacuoC1HoraFim.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "vac1_valor"), etVacuoC1Valor.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "vac1_num_bomba"), etVacuoNumBomba.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "vac1_num_vacuometro"), etVacuoNumVacuometro.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "vac1_data_cal_vacuometro"), etVacuoDataCalibracaoVacuometro.getText().toString());

        editor.putString(gerarChaveVacuo(this, checklistId, "vac2_data_inicio"), etVacuoC2DataInicio.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "vac2_data_fim"), etVacuoC2DataFim.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "vac2_resp"), etVacuoC2Resp.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "vac2_hora_inicio"), etVacuoC2HoraInicio.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "vac2_hora_fim"), etVacuoC2HoraFim.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "vac2_valor"), etVacuoC2Valor.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "vac2_num_bomba"), etVacuoNumBombaC2.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "vac2_num_vacuometro"), etVacuoNumVacuometroC2.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "vac2_data_cal_vacuometro"), etVacuoDataCalibracaoVacuometroC2.getText().toString());

        editor.putString(gerarChaveVacuo(this, checklistId, "quebra_com_data"), etQuebraComValvulaData.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra_qtd_refri"), etQuebraQtdRefrigerante.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra_com_resp"), etQuebraComValvulaResp.getText().toString());

        editor.putString(gerarChaveVacuo(this, checklistId, "quebra_sem_data"), etQuebraSemValvulaData.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra_carga_n2"), etQuebraCargaNitrogenio.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra_sem_resp"), etQuebraSemValvulaResp.getText().toString());

        editor.putString(gerarChaveVacuo(this, checklistId, "quebra_det_data"), etQuebraDetectorData.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra_det_resp"), etQuebraDetectorResp.getText().toString());

        editor.putString(gerarChaveVacuo(this, checklistId, "quebra_num_balanca"), etQuebraNumBalanca.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra_data_cal_balanca"), etQuebraDataCalibracaoBalanca.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra_num_manometro"), etQuebraNumManometro.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra_data_cal_manometro"), etQuebraDataCalibracaoManometro.getText().toString());

        // Quebra do vácuo C2
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra2_com_data"), etQuebra2ComValvulaData.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra2_qtd_refri"), etQuebra2QtdRefrigerante.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra2_com_resp"), etQuebra2ComValvulaResp.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra2_sem_data"), etQuebra2SemValvulaData.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra2_carga_n2"), etQuebra2CargaNitrogenio.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra2_sem_resp"), etQuebra2SemValvulaResp.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra2_det_data"), etQuebra2DetectorData.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra2_det_resp"), etQuebra2DetectorResp.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra2_num_balanca"), etQuebra2NumBalanca.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra2_data_cal_balanca"), etQuebra2DataCalibracaoBalanca.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra2_num_manometro"), etQuebra2NumManometro.getText().toString());
        editor.putString(gerarChaveVacuo(this, checklistId, "quebra2_data_cal_manometro"), etQuebra2DataCalibracaoManometro.getText().toString());

        editor.apply();
        Toast.makeText(this, "Dados de vácuo e quebra salvos", Toast.LENGTH_SHORT).show();
    }

    private void carregarEletricaInstrumentos() {
        if (layoutEletricaInstrumentos == null) return;
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        if (etEletricaInstr1Num != null) etEletricaInstr1Num.setText(prefs.getString(gerarChaveEletrica(this, checklistId, "instr1_num"), ""));
        if (etEletricaInstr1Data != null) etEletricaInstr1Data.setText(prefs.getString(gerarChaveEletrica(this, checklistId, "instr1_data"), ""));
        if (etEletricaInstr2Num != null) etEletricaInstr2Num.setText(prefs.getString(gerarChaveEletrica(this, checklistId, "instr2_num"), ""));
        if (etEletricaInstr2Data != null) etEletricaInstr2Data.setText(prefs.getString(gerarChaveEletrica(this, checklistId, "instr2_data"), ""));
        if (etEletricaInstr3Num != null) etEletricaInstr3Num.setText(prefs.getString(gerarChaveEletrica(this, checklistId, "instr3_num"), ""));
        if (etEletricaInstr3Data != null) etEletricaInstr3Data.setText(prefs.getString(gerarChaveEletrica(this, checklistId, "instr3_data"), ""));
    }

    private void salvarEletricaInstrumentos() {
        if (layoutEletricaInstrumentos == null) return;
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(gerarChaveEletrica(this, checklistId, "instr1_num"), texto(etEletricaInstr1Num));
        editor.putString(gerarChaveEletrica(this, checklistId, "instr1_data"), texto(etEletricaInstr1Data));
        editor.putString(gerarChaveEletrica(this, checklistId, "instr2_num"), texto(etEletricaInstr2Num));
        editor.putString(gerarChaveEletrica(this, checklistId, "instr2_data"), texto(etEletricaInstr2Data));
        editor.putString(gerarChaveEletrica(this, checklistId, "instr3_num"), texto(etEletricaInstr3Num));
        editor.putString(gerarChaveEletrica(this, checklistId, "instr3_data"), texto(etEletricaInstr3Data));
        editor.apply();
        Toast.makeText(this, "Instrumentos salvos", Toast.LENGTH_SHORT).show();
    }

    private static String texto(EditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void configurarDatePicker(EditText editText) {
        if (editText == null) return;
        editText.setOnClickListener(v -> abrirDatePicker(editText));
    }

    private void abrirDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        int m = month + 1;
                        String data = String.format("%02d/%02d/%04d", dayOfMonth, m, year);
                        target.setText(data);
                    }
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void iniciarLeituraQr(int target) {
        qrTarget = target;
        Intent intent = new Intent(this, QrScanTestActivity.class);
        intent.putExtra(QrScanTestActivity.EXTRA_RETURN_RESULT, true);
        qrScanLauncher.launch(intent);
    }

    private void aplicarQrPorTarget(int target, String raw) {
        if (target == QR_TARGET_NONE) return;
        String textoRaw = raw != null ? raw.trim() : "";
        if (textoRaw.isEmpty()) {
            Toast.makeText(this, "Nenhum texto lido no QR", Toast.LENGTH_SHORT).show();
            return;
        }

        String num = "";
        String dataCal = "";
        try {
            java.util.Map<String, String> dados = QrUtils.parseQr(textoRaw);
            num = dados.getOrDefault("NUM", "");
            dataCal = dados.getOrDefault("DATA_CAL", "");
        } catch (Exception ignored) {
        }

        if (target == QR_TARGET_ELETRICA_1 || target == QR_TARGET_ELETRICA_2 || target == QR_TARGET_ELETRICA_3) {
            EditText etNum;
            EditText etData;
            if (target == QR_TARGET_ELETRICA_1) {
                etNum = etEletricaInstr1Num;
                etData = etEletricaInstr1Data;
            } else if (target == QR_TARGET_ELETRICA_2) {
                etNum = etEletricaInstr2Num;
                etData = etEletricaInstr2Data;
            } else {
                etNum = etEletricaInstr3Num;
                etData = etEletricaInstr3Data;
            }

            if (etNum != null) etNum.setText(!num.isEmpty() ? num : textoRaw);
            if (etData != null && !dataCal.isEmpty()) etData.setText(dataCal);
            return;
        }

        if (target == QR_TARGET_EST_C1) {
            if (etEstNumManometroC1 != null) etEstNumManometroC1.setText(!num.isEmpty() ? num : textoRaw);
            if (etEstDataCalibracaoC1 != null && !dataCal.isEmpty()) etEstDataCalibracaoC1.setText(dataCal);
            return;
        }

        if (target == QR_TARGET_EST_C2) {
            if (etEstNumManometroC2 != null) etEstNumManometroC2.setText(!num.isEmpty() ? num : textoRaw);
            if (etEstDataCalibracaoC2 != null && !dataCal.isEmpty()) etEstDataCalibracaoC2.setText(dataCal);
            return;
        }

        if (target == QR_TARGET_VACUO_INSTR_C1) {
            String bomba = "";
            String vacuometro = "";
            try {
                java.util.Map<String, String> dados = QrUtils.parseQr(textoRaw);
                bomba = dados.getOrDefault("BOMBA", "");
                vacuometro = dados.getOrDefault("VACUOMETRO", "");
            } catch (Exception ignored) {
            }
            if (etVacuoNumBomba != null && !bomba.isEmpty()) etVacuoNumBomba.setText(bomba);
            if (etVacuoNumVacuometro != null && !vacuometro.isEmpty()) etVacuoNumVacuometro.setText(vacuometro);
            if (etVacuoDataCalibracaoVacuometro != null && !dataCal.isEmpty()) etVacuoDataCalibracaoVacuometro.setText(dataCal);
            return;
        }

        if (target == QR_TARGET_VACUO_INSTR_C2) {
            String bomba = "";
            String vacuometro = "";
            try {
                java.util.Map<String, String> dados = QrUtils.parseQr(textoRaw);
                bomba = dados.getOrDefault("BOMBA", "");
                vacuometro = dados.getOrDefault("VACUOMETRO", "");
            } catch (Exception ignored) {
            }
            if (etVacuoNumBombaC2 != null && !bomba.isEmpty()) etVacuoNumBombaC2.setText(bomba);
            if (etVacuoNumVacuometroC2 != null && !vacuometro.isEmpty()) etVacuoNumVacuometroC2.setText(vacuometro);
            if (etVacuoDataCalibracaoVacuometroC2 != null && !dataCal.isEmpty()) etVacuoDataCalibracaoVacuometroC2.setText(dataCal);
            return;
        }

        if (target == QR_TARGET_QUEBRA_BALANCA_C1) {
            String balanca = "";
            try {
                java.util.Map<String, String> dados = QrUtils.parseQr(textoRaw);
                balanca = dados.getOrDefault("BALANCA", "");
                if (balanca.isEmpty()) balanca = dados.getOrDefault("NUM", "");
            } catch (Exception ignored) {
            }
            if (etQuebraNumBalanca != null) etQuebraNumBalanca.setText(!balanca.isEmpty() ? balanca : textoRaw);
            if (etQuebraDataCalibracaoBalanca != null && !dataCal.isEmpty()) etQuebraDataCalibracaoBalanca.setText(dataCal);
            return;
        }

        if (target == QR_TARGET_QUEBRA_BALANCA_C2) {
            String balanca = "";
            try {
                java.util.Map<String, String> dados = QrUtils.parseQr(textoRaw);
                balanca = dados.getOrDefault("BALANCA", "");
                if (balanca.isEmpty()) balanca = dados.getOrDefault("NUM", "");
            } catch (Exception ignored) {
            }
            if (etQuebra2NumBalanca != null) etQuebra2NumBalanca.setText(!balanca.isEmpty() ? balanca : textoRaw);
            if (etQuebra2DataCalibracaoBalanca != null && !dataCal.isEmpty()) etQuebra2DataCalibracaoBalanca.setText(dataCal);
            return;
        }

        if (target == QR_TARGET_QUEBRA_MANOMETRO_C1) {
            String manometro = "";
            try {
                java.util.Map<String, String> dados = QrUtils.parseQr(textoRaw);
                manometro = dados.getOrDefault("MANOMETRO", "");
                if (manometro.isEmpty()) manometro = dados.getOrDefault("NUM", "");
            } catch (Exception ignored) {
            }
            if (etQuebraNumManometro != null) etQuebraNumManometro.setText(!manometro.isEmpty() ? manometro : textoRaw);
            if (etQuebraDataCalibracaoManometro != null && !dataCal.isEmpty()) etQuebraDataCalibracaoManometro.setText(dataCal);
            return;
        }

        if (target == QR_TARGET_QUEBRA_MANOMETRO_C2) {
            String manometro = "";
            try {
                java.util.Map<String, String> dados = QrUtils.parseQr(textoRaw);
                manometro = dados.getOrDefault("MANOMETRO", "");
                if (manometro.isEmpty()) manometro = dados.getOrDefault("NUM", "");
            } catch (Exception ignored) {
            }
            if (etQuebra2NumManometro != null) etQuebra2NumManometro.setText(!manometro.isEmpty() ? manometro : textoRaw);
            if (etQuebra2DataCalibracaoManometro != null && !dataCal.isEmpty()) etQuebra2DataCalibracaoManometro.setText(dataCal);
            return;
        }

        if (target == QR_TARGET_IRBR_PRE_TORQUE) {
            String torque = "";
            try {
                java.util.Map<String, String> dados = QrUtils.parseQr(textoRaw);
                torque = dados.getOrDefault("TORQUIMETRO", "");
            } catch (Exception ignored) {
            }
            if (torque.isEmpty()) torque = !num.isEmpty() ? num : textoRaw;
            if (etIrbrPreTorqueNum != null) etIrbrPreTorqueNum.setText(torque);
            if (etIrbrPreTorqueDataCal != null && !dataCal.isEmpty()) etIrbrPreTorqueDataCal.setText(dataCal);
        }
    }

    private void carregarIrbrPreTorquimetro() {
        if (layoutIrbrPreTorquimetro == null) return;
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        if (etIrbrPreTorqueNum != null) etIrbrPreTorqueNum.setText(prefs.getString(gerarChavePre(this, checklistId, "torque_num"), ""));
        if (etIrbrPreTorqueDataCal != null) etIrbrPreTorqueDataCal.setText(prefs.getString(gerarChavePre(this, checklistId, "torque_data_cal"), ""));
        if (etIrbrPreTorqueData != null) etIrbrPreTorqueData.setText(prefs.getString(gerarChavePre(this, checklistId, "torque_data"), ""));
        if (etIrbrPreTorqueResp != null) etIrbrPreTorqueResp.setText(prefs.getString(gerarChavePre(this, checklistId, "torque_resp"), ""));
    }

    private void salvarIrbrPreTorquimetro() {
        if (layoutIrbrPreTorquimetro == null) return;
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(gerarChavePre(this, checklistId, "torque_num"), texto(etIrbrPreTorqueNum));
        editor.putString(gerarChavePre(this, checklistId, "torque_data_cal"), texto(etIrbrPreTorqueDataCal));
        editor.putString(gerarChavePre(this, checklistId, "torque_data"), texto(etIrbrPreTorqueData));
        editor.putString(gerarChavePre(this, checklistId, "torque_resp"), texto(etIrbrPreTorqueResp));
        editor.apply();
        Toast.makeText(this, "Torquímetro salvo", Toast.LENGTH_SHORT).show();
    }

    private void carregarFotos() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        String chaveFotos = gerarChaveFotosEquip(this, checklistId);
        String valor = prefs.getString(chaveFotos, "");
        listaFotos.clear();
        layoutFotos.removeAllViews();
        if (valor != null && !valor.isEmpty()) {
            String[] partes = valor.split(";");
            for (String s : partes) {
                if (!s.isEmpty()) {
                    listaFotos.add(s);
                }
            }
        }
        atualizarListaFotos();
    }

    private void salvarFotos() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String chaveFotos = gerarChaveFotosEquip(this, checklistId);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < listaFotos.size(); i++) {
            if (i > 0) sb.append(";");
            sb.append(listaFotos.get(i));
        }
        editor.putString(chaveFotos, sb.toString());
        editor.apply();
    }

    private void atualizarListaFotos() {
        layoutFotos.removeAllViews();
        for (int i = 0; i < listaFotos.size(); i++) {
            String uriString = listaFotos.get(i);
            TextView tv = new TextView(this);
            tv.setText("Foto " + (i + 1));
            tv.setPadding(0, 4, 0, 4);
            layoutFotos.addView(tv);
        }
    }

    private void escolherFoto() {
        String[] opcoes = new String[]{"Tirar foto", "Escolher da galeria"};
        new AlertDialog.Builder(this)
                .setTitle("Adicionar foto")
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        tirarFoto();
                    } else if (which == 1) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*");
                        startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void tirarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, "Nenhum app de câmera disponível", Toast.LENGTH_SHORT).show();
            return;
        }

        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null) {
            dir = getFilesDir();
        }
        String fileName = "foto_" + checklistId + "_" + System.currentTimeMillis() + ".jpg";
        File fotoFile = new File(dir, fileName);
        fotoTempUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", fotoFile);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoTempUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
    }

    private void mostrarDatePicker(final EditText campo) {
        final Calendar calendario = Calendar.getInstance();

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
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void mostrarDialogAdicionarItem() {
        final EditText input = new EditText(this);
        input.setHint("Descrição do novo item");

        new AlertDialog.Builder(this)
                .setTitle("Adicionar item")
                .setView(input)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String titulo = input.getText().toString().trim();
                    if (!titulo.isEmpty()) {
                        String novoId = "user_" + System.currentTimeMillis();
                        ChecklistItem novoItem = new ChecklistItem(novoId, titulo, "");
                        itens.add(novoItem);
                        salvarItensCustomizados();
                        adapter.notifyItemInserted(itens.size() - 1);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_PHOTO && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try {
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);
                } catch (SecurityException ignored) {
                }

                listaFotos.add(uri.toString());
                salvarFotos();
                atualizarListaFotos();
            }
        } else if (requestCode == REQUEST_CODE_TAKE_PHOTO && resultCode == RESULT_OK && fotoTempUri != null) {
            listaFotos.add(fotoTempUri.toString());
            salvarFotos();
            atualizarListaFotos();
        }
    }

    private void confirmarRemocaoItem(int position, ChecklistItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Remover item")
                .setMessage("Deseja remover o item selecionado?")
                .setPositiveButton("Remover", (dialog, which) -> {
                    marcarItemComoRemovidoOuCustom(position, item);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void marcarItemComoRemovidoOuCustom(int position, ChecklistItem item) {
        if (item.getId().startsWith("user_")) {
            removerItemCustom(item.getId());
        } else {
            List<String> idsRemovidos = carregarIdsRemovidos();
            if (!idsRemovidos.contains(item.getId())) {
                idsRemovidos.add(item.getId());
                salvarIdsRemovidos(idsRemovidos);
            }
        }

        // Opcional: limpar estado salvo do item removido
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String chaveStatus = gerarChaveStatus(checklistId, item.getId());
        editor.remove(chaveStatus);
        editor.apply();

        itens.remove(position);
        adapter.notifyItemRemoved(position);
    }

    private List<String> carregarIdsRemovidos() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        String chave = "checklist_" + checklistId + "_removed_ids";
        String valor = prefs.getString(chave, "");
        List<String> ids = new ArrayList<>();
        if (!valor.isEmpty()) {
            ids.addAll(Arrays.asList(valor.split(",")));
        }
        return ids;
    }

    private void salvarIdsRemovidos(List<String> ids) {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String chave = "checklist_" + checklistId + "_removed_ids";
        String valor = String.join(",", ids);
        editor.putString(chave, valor);
        editor.apply();
    }

    private List<ChecklistItem> carregarItensCustomizados() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        String chave = "checklist_" + checklistId + "_custom_items";
        String valor = prefs.getString(chave, "");
        List<ChecklistItem> lista = new ArrayList<>();
        if (!valor.isEmpty()) {
            String[] partes = valor.split(";;");
            for (String parte : partes) {
                String[] campos = parte.split("\\|", 2);
                if (campos.length == 2) {
                    String id = campos[0];
                    String titulo = campos[1];
                    lista.add(new ChecklistItem(id, titulo, ""));
                }
            }
        }
        return lista;
    }

    private void salvarItensCustomizados() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String chave = "checklist_" + checklistId + "_custom_items";

        StringBuilder sb = new StringBuilder();
        for (ChecklistItem item : itens) {
            if (item.getId().startsWith("user_")) {
                if (!sb.toString().isEmpty()) {
                    sb.append(";;");
                }
                sb.append(item.getId())
                        .append("|")
                        .append(item.getTitulo());
            }
        }

        editor.putString(chave, sb.toString());
        editor.apply();
    }

    private void removerItemCustom(String itemId) {
        List<ChecklistItem> custom = carregarItensCustomizados();
        List<ChecklistItem> atualizada = new ArrayList<>();
        for (ChecklistItem c : custom) {
            if (!c.getId().equals(itemId)) {
                atualizada.add(c);
            }
        }

        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String chave = "checklist_" + checklistId + "_custom_items";

        StringBuilder sb = new StringBuilder();
        for (ChecklistItem item : atualizada) {
            if (!sb.toString().isEmpty()) {
                sb.append(";;");
            }
            sb.append(item.getId())
                    .append("|")
                    .append(item.getTitulo());
        }

        editor.putString(chave, sb.toString());
        editor.apply();
    }

    private void restaurarChecklistPadrao() {
        try {
            SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // Limpa lista de itens removidos e itens customizados
            String chaveRemoved = "checklist_" + checklistId + "_removed_ids";
            String chaveCustom = "checklist_" + checklistId + "_custom_items";
            editor.remove(chaveRemoved);
            editor.remove(chaveCustom);

            // Remove status/flags de todos os itens definidos no JSON para este checklist
            JSONObject checklistSelecionado = ChecklistRepository.getChecklistById(this, checklistId);
            if (checklistSelecionado != null) {
                JSONArray itensArray = checklistSelecionado.getJSONArray("itens");
                for (int i = 0; i < itensArray.length(); i++) {
                    JSONObject itemObj = itensArray.getJSONObject(i);
                    String idItem = itemObj.getString("id");
                    editor.remove(gerarChaveStatus(checklistId, idItem));
                    // compatibilidade com chave antiga booleana
                    editor.remove(gerarChavePref(checklistId, idItem));
                }
            }

            editor.apply();

            // Recarrega itens na tela
            itens.clear();
            carregarItensDoChecklist();
            adapter.notifyDataSetChanged();

            Toast.makeText(this, "Itens padrão restaurados para este checklist", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao restaurar itens padrão", Toast.LENGTH_LONG).show();
        }
    }

    private void navegarParaChecklist(int delta) {
        if (listaIds == null || listaIds.isEmpty() || indiceAtual < 0) return;

        // Salva cabeçalho e observação antes de sair
        salvarCabecalho();
        salvarObservacao();

        int novoIndice = indiceAtual + delta;
        if (novoIndice < 0 || novoIndice >= listaIds.size()) return;

        String novoId = listaIds.get(novoIndice);
        String novoNome = listaNomes != null && listaNomes.size() == listaIds.size()
                ? listaNomes.get(novoIndice)
                : novoId;

        Intent intent = new Intent(this, ChecklistActivity.class);
        intent.putExtra("checklist_id", novoId);
        intent.putExtra("checklist_nome", novoNome);
        intent.putStringArrayListExtra("lista_ids", listaIds);
        if (listaNomes != null) {
            intent.putStringArrayListExtra("lista_nomes", listaNomes);
        }
        intent.putExtra("indice_atual", novoIndice);
        startActivity(intent);
        finish();
    }

    private String getHeaderKeyForChecklist(String checklistId) {
        String modelKey;
        if (checklistId.startsWith("checklist_irbr_")) {
            modelKey = "irbr";
        } else if (checklistId.startsWith("checklist_ucabr_")) {
            modelKey = "ucabr";
        } else if (checklistId.startsWith("checklist_edbrse_")) {
            modelKey = "edbrse";
        } else if (checklistId.startsWith("checklist_esbrag_")) {
            modelKey = "esbrag";
        } else if (checklistId.startsWith("checklist_cabr_")) {
            modelKey = "cabr";
        } else if ("checklist_manutencao".equals(checklistId)) {
            modelKey = "manutencao";
        } else {
            modelKey = "generico";
        }

        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        String equipKey = ChecklistHeaderActivity.getCurrentEquipKey(prefs, modelKey);
        return equipKey != null ? equipKey : modelKey;
    }

    private void exportarChecklistParaPdf() {
        try {
            SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);

            String headerKey = getHeaderKeyForChecklist(checklistId);

            String clienteObra = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "cliente_obra"), "");
            String modelo = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "modelo"), "");
            String sn = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "sn"), "");
            String fluido = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "fluido"), "");
            String tensao = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "tensao"), "");
            String opChecklist = prefs.getString(gerarChaveOpEquip(this, checklistId), "");
            String opModelo = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "op"), "");
            String op = !opChecklist.isEmpty() ? opChecklist : opModelo;
            String tag = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "tag"), "");
            String elaboradoPor = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "elaborado_por"), "");
            String dataElaboracao = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "data_elaboracao"), "");
            String aprovadoPor = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "aprovado_por"), "");
            String dataAprovacao = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "data_aprovacao"), "");
            // IT específico do checklist tem prioridade; se vazio, usa IT do modelo
            String itChecklist = prefs.getString(gerarChaveItEquip(this, checklistId), "");
            String itModelo = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "it"), "");
            String it = !itChecklist.isEmpty() ? itChecklist : itModelo;

            String responsavel = etResponsavel.getText().toString();
            String data = etData.getText().toString();
            String obsGeral = etObservacao.getText().toString();

            PdfDocument document = new PdfDocument();

            // Página A4 em pontos (aprox 595 x 842)
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            Paint titlePaint = new Paint();
            titlePaint.setTextSize(16);
            titlePaint.setFakeBoldText(true);

            Paint textPaint = new Paint();
            textPaint.setTextSize(10);

            int x = 40;
            int y = 50;
            int lineHeight = 14;

            canvas.drawText("Checklist: " + (getIntent().getStringExtra("checklist_nome")), x, y, titlePaint);
            y += lineHeight * 2;

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
            canvas.drawText("Data da aprovação: " + dataAprovacao, x, y, textPaint); y += lineHeight;
            canvas.drawText("IT: " + it, x, y, textPaint); y += lineHeight * 2;

            canvas.drawText("Responsável: " + responsavel, x, y, textPaint); y += lineHeight;
            canvas.drawText("Data: " + data, x, y, textPaint); y += lineHeight * 2;

            canvas.drawText("Itens:", x, y, titlePaint); y += lineHeight;

            for (ChecklistItem item : itens) {
                String prefixo;
                if (item.isSim()) {
                    prefixo = "[SIM] ";
                } else if (item.isNa()) {
                    prefixo = "[N/A] ";
                } else {
                    prefixo = "[   ] ";
                }

                String linha = prefixo + item.getTitulo();

                // Quebra de linha simples se for muito grande
                if (linha.length() > 90) {
                    String parte1 = linha.substring(0, 90);
                    String parte2 = linha.substring(90);
                    canvas.drawText(parte1, x, y, textPaint); y += lineHeight;
                    canvas.drawText("    " + parte2, x, y, textPaint); y += lineHeight;
                } else {
                    canvas.drawText(linha, x, y, textPaint); y += lineHeight;
                }

                if (y > 800) {
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                }
            }

            // Seção extra: torquímetro (Pré-montagem - IRBR/UCABR/EDBRSE/CABR)
            if ("checklist_irbr_pre_montagem".equals(checklistId)
                    || "checklist_ucabr_pre_montagem".equals(checklistId)
                    || "checklist_edbrse_pre_montagem".equals(checklistId)
                    || "checklist_cabr_pre_montagem".equals(checklistId)) {
                String torqueNum = prefs.getString(gerarChavePre(this, checklistId, "torque_num"), "");
                String torqueDataCal = prefs.getString(gerarChavePre(this, checklistId, "torque_data_cal"), "");
                String torqueData = prefs.getString(gerarChavePre(this, checklistId, "torque_data"), "");
                String torqueResp = prefs.getString(gerarChavePre(this, checklistId, "torque_resp"), "");

                y += lineHeight;
                if (y > 800) {
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                }

                canvas.drawText("Torquímetro:", x, y, titlePaint); y += lineHeight;
                canvas.drawText("Nº instr.: " + torqueNum, x, y, textPaint); y += lineHeight;
                canvas.drawText("Data da calibração: " + torqueDataCal, x, y, textPaint); y += lineHeight;
                canvas.drawText("Data: " + torqueData, x, y, textPaint); y += lineHeight;
                canvas.drawText("Responsável: " + torqueResp, x, y, textPaint); y += lineHeight;
            }

            // Seção extra: pressão válvulas de alívio (Montagem frigorífica)
            if ("checklist_ucabr_montagem_frigorifica".equals(checklistId)
                    || "checklist_irbr_montagem_frigorifica".equals(checklistId)
                    || "checklist_edbrse_montagem_frigorifica".equals(checklistId)
                    || "checklist_cabr_montagem_frigorifica".equals(checklistId)) {

                String p1Dia1 = prefs.getString(gerarChavePressaoValvula(this, checklistId, "alivio1_dia1"), "");
                String p1Dia2 = prefs.getString(gerarChavePressaoValvula(this, checklistId, "alivio1_dia2"), "");
                String p2Dia1 = prefs.getString(gerarChavePressaoValvula(this, checklistId, "alivio2_dia1"), "");
                String p2Dia2 = prefs.getString(gerarChavePressaoValvula(this, checklistId, "alivio2_dia2"), "");

                y += lineHeight;
                if (y > 800) {
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                }

                canvas.drawText("Pressão válvulas de alívio:", x, y, titlePaint); y += lineHeight;
                if ("checklist_edbrse_montagem_frigorifica".equals(checklistId)) {
                    canvas.drawText("Válvula alívio 1 (kgf/cm²): " + p1Dia1, x, y, textPaint); y += lineHeight;
                    canvas.drawText("Válvula alívio 2 (kgf/cm²): " + p2Dia1, x, y, textPaint); y += lineHeight;
                } else {
                    canvas.drawText("Válvula alívio 1 - dia 1/2 (kgf/cm²): " + p1Dia1 + " / " + p1Dia2, x, y, textPaint); y += lineHeight;
                    canvas.drawText("Válvula alívio 2 - dia 1/2 (kgf/cm²): " + p2Dia1 + " / " + p2Dia2, x, y, textPaint); y += lineHeight;
                }
            }

            // Seção extra: instrumentos (Montagem elétrica - IRBR/UCABR)
            if ("checklist_irbr_montagem_eletrica".equals(checklistId)
                    || "checklist_ucabr_montagem_eletrica".equals(checklistId)
                    || "checklist_edbrse_montagem_eletrica".equals(checklistId)) {
                String instr1Num = prefs.getString(gerarChaveEletrica(this, checklistId, "instr1_num"), "");
                String instr1Data = prefs.getString(gerarChaveEletrica(this, checklistId, "instr1_data"), "");
                String instr2Num = prefs.getString(gerarChaveEletrica(this, checklistId, "instr2_num"), "");
                String instr2Data = prefs.getString(gerarChaveEletrica(this, checklistId, "instr2_data"), "");
                String instr3Num = prefs.getString(gerarChaveEletrica(this, checklistId, "instr3_num"), "");
                String instr3Data = prefs.getString(gerarChaveEletrica(this, checklistId, "instr3_data"), "");

                y += lineHeight;
                if (y > 800) {
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                }

                canvas.drawText("Instrumentos:", x, y, titlePaint);
                y += lineHeight;
                canvas.drawText("Nº Instr. Medição 1: " + instr1Num + "    Data calibração: " + instr1Data, x, y, textPaint);
                y += lineHeight;
                canvas.drawText("Nº Instr. Medição 2: " + instr2Num + "    Data calibração: " + instr2Data, x, y, textPaint);
                y += lineHeight;
                canvas.drawText("Nº Instr. Medição 3: " + instr3Num + "    Data calibração: " + instr3Data, x, y, textPaint);
                y += lineHeight;
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

            File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (dir == null) {
                dir = getFilesDir();
            }

            String fileName = "checklist_" + checklistId + "_" + System.currentTimeMillis() + ".pdf";
            File file = new File(dir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();

            Toast.makeText(this, "PDF salvo em: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Opcional: abrir diálogo de compartilhamento
            try {
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Compartilhar checklist em PDF"));
            } catch (Exception e) {
                // Se o FileProvider não estiver configurado, pelo menos o arquivo já foi salvo
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao gerar PDF", Toast.LENGTH_LONG).show();
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

    // chaves antigas (compatibilidade)
    public static String gerarChavePref(String checklistId, String itemId) {
        return "checklist_" + checklistId + "_item_" + itemId;
    }

    public static String gerarChaveStatus(String checklistId, String itemId) {
        return "checklist_" + checklistId + "_item_" + itemId + "_status";
    }

    public static String gerarChaveIt(String checklistId) {
        return "checklist_" + checklistId + "_it";
    }

    public static String gerarChaveOp(String checklistId) {
        return "checklist_" + checklistId + "_op";
    }

    public static String gerarChaveResp(String checklistId) {
        return "checklist_" + checklistId + "_responsavel";
    }

    public static String gerarChaveData(String checklistId) {
        return "checklist_" + checklistId + "_data";
    }

    public static String gerarChaveObs(String checklistId) {
        return "checklist_" + checklistId + "_obs";
    }

    // novas chaves por máquina (equipamento)
    private static String getModelKeyForChecklist(String checklistId) {
        if (checklistId.startsWith("checklist_irbr_")) {
            return "irbr";
        } else if (checklistId.startsWith("checklist_ucabr_")) {
            return "ucabr";
        } else if (checklistId.startsWith("checklist_edbrse_")) {
            return "edbrse";
        } else if (checklistId.startsWith("checklist_esbrag_")) {
            return "esbrag";
        } else if ("checklist_manutencao".equals(checklistId)) {
            return "manutencao";
        }
        return "generico";
    }

    private static String getEquipPrefix(android.content.Context context, String checklistId) {
        String modelKey = getModelKeyForChecklist(checklistId);
        SharedPreferences prefs = context.getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        String equipKey = ChecklistHeaderActivity.getCurrentEquipKey(prefs, modelKey);
        if (equipKey != null) {
            return "equip_" + equipKey + "_checklist_" + checklistId + "_";
        } else {
            return "checklist_" + checklistId + "_";
        }
    }

    public static String gerarChaveStatusEquip(android.content.Context context, String checklistId, String itemId) {
        return getEquipPrefix(context, checklistId) + "item_" + itemId + "_status";
    }

    public static String gerarChaveItEquip(android.content.Context context, String checklistId) {
        return getEquipPrefix(context, checklistId) + "it";
    }

    public static String gerarChaveOpEquip(android.content.Context context, String checklistId) {
        return getEquipPrefix(context, checklistId) + "op";
    }

    public static String gerarChaveRespEquip(android.content.Context context, String checklistId) {
        return getEquipPrefix(context, checklistId) + "responsavel";
    }

    public static String gerarChaveDataEquip(android.content.Context context, String checklistId) {
        return getEquipPrefix(context, checklistId) + "data";
    }

    public static String gerarChaveObsEquip(android.content.Context context, String checklistId) {
        return getEquipPrefix(context, checklistId) + "obs";
    }

    public static String gerarChaveFotosEquip(android.content.Context context, String checklistId) {
        return getEquipPrefix(context, checklistId) + "fotos";
    }

    // Chaves específicas para campos dos compressores (UCABR), também por equipamento
    public static String gerarChaveComp(android.content.Context context, String checklistId, String campo) {
        return getEquipPrefix(context, checklistId) + "comp_" + campo;
    }

    // Chaves específicas para pressões das válvulas de alívio (UCABR - Montagem frigorífica), por equipamento
    public static String gerarChavePressaoValvula(android.content.Context context, String checklistId, String campo) {
        return getEquipPrefix(context, checklistId) + "pressao_" + campo;
    }

    // Chaves específicas para teste de estanqueidade (UCABR), por equipamento
    public static String gerarChaveEstanq(android.content.Context context, String checklistId, String campo) {
        return getEquipPrefix(context, checklistId) + "estanq_" + campo;
    }

    // Chaves específicas para vácuo e quebra do vácuo (UCABR), por equipamento
    public static String gerarChaveVacuo(android.content.Context context, String checklistId, String campo) {
        return getEquipPrefix(context, checklistId) + "vacuo_" + campo;
    }

    // Chaves específicas para instrumentos (Montagem elétrica - IRBR), por equipamento
    public static String gerarChaveEletrica(android.content.Context context, String checklistId, String campo) {
        return getEquipPrefix(context, checklistId) + "eletrica_" + campo;
    }

    // Chaves específicas (IRBR - Pré-montagem), por equipamento
    public static String gerarChavePre(android.content.Context context, String checklistId, String campo) {
        return getEquipPrefix(context, checklistId) + "pre_" + campo;
    }
}

