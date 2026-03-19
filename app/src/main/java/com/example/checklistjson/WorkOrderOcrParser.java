package com.example.checklistjson;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser simples para extrair OP, modelos e quantidades (UN) a partir de OCR do Word.
 * Mantém regras bem conservadoras para evitar criar máquinas erradas.
 */
public final class WorkOrderOcrParser {

    private WorkOrderOcrParser() {}

    public static final class Parsed {
        @Nullable public final String op;
        @NonNull public final List<ModelUnits> modelUnits;
        @Nullable public final String clienteObra;
        @Nullable public final String fluido;
        @Nullable public final String tensao;

        Parsed(@Nullable String op,
               @NonNull List<ModelUnits> modelUnits,
               @Nullable String clienteObra,
               @Nullable String fluido,
               @Nullable String tensao) {
            this.op = op;
            this.modelUnits = modelUnits;
            this.clienteObra = clienteObra;
            this.fluido = fluido;
            this.tensao = tensao;
        }
    }

    public static final class ModelUnits {
        @NonNull public final String modelKey; // irbr, ucabr, edbrse, esbrag, cabr
        public final int units;

        ModelUnits(@NonNull String modelKey, int units) {
            this.modelKey = modelKey;
            this.units = units;
        }
    }

    @NonNull
    public static Parsed parse(@Nullable String ocrText) {
        String text = ocrText == null ? "" : ocrText;

        String op = extractOp(text);
        List<ModelUnits> models = extractModelUnits(text);
        String clienteObra = extractClienteObra(text);
        String fluido = extractFluido(text);
        String tensao = extractTensao(text);
        return new Parsed(op, models, clienteObra, fluido, tensao);
    }

    @Nullable
    private static String extractOp(@NonNull String text) {
        Matcher m = Pattern.compile("(?i)\\bOP\\s*[:\\-]?\\s*(\\d{3,8})\\b").matcher(text);
        if (m.find()) return m.group(1);
        return null;
    }

    @NonNull
    private static List<ModelUnits> extractModelUnits(@NonNull String text) {
        // Ignora QCR (quadro remoto) por regra do usuário.
        // Captura linhas do tipo "UCABR017... - 3 UN - DATA: ..."
        Pattern pLine = Pattern.compile(
                "(?im)^\\s*([A-Z]{3,8})[A-Z0-9]*\\s*[–\\-]\\s*(\\d+)\\s*UN\\b"
        );

        Matcher m = pLine.matcher(text);
        List<ModelUnits> out = new ArrayList<>();

        // Se houver repetição do mesmo modelo, soma as unidades (conservador e útil).
        // Ex.: 1 UN em 3 linhas → total 3
        java.util.Map<String, Integer> sum = new java.util.HashMap<>();
        while (m.find()) {
            String prefix = safeUpper(m.group(1));
            int units = safeInt(m.group(2));
            if (units <= 0) continue;

            String modelKey = mapPrefixToModelKey(prefix);
            if (modelKey == null) continue;

            sum.put(modelKey, (sum.containsKey(modelKey) ? sum.get(modelKey) : 0) + units);
        }

        // Mantém ordem estável por prioridade de modelo (só para UX previsível no diálogo)
        String[] order = new String[]{"cabr", "irbr", "ucabr", "edbrse", "esbrag"};
        Set<String> used = new HashSet<>();
        for (String k : order) {
            Integer u = sum.get(k);
            if (u != null && u > 0) {
                out.add(new ModelUnits(k, u));
                used.add(k);
            }
        }
        for (java.util.Map.Entry<String, Integer> e : sum.entrySet()) {
            if (used.contains(e.getKey())) continue;
            if (e.getValue() != null && e.getValue() > 0) out.add(new ModelUnits(e.getKey(), e.getValue()));
        }

        return out;
    }

    @Nullable
    private static String extractFluido(@NonNull String text) {
        // Procura padrões do tipo R410A, R407C, R 407C, etc.
        Matcher m = Pattern.compile("(?i)\\bR\\s*([0-9]{3})\\s*([A-Z])?\\b").matcher(text);
        if (m.find()) {
            String num = m.group(1);
            String sufixo = m.group(2);
            return "R" + num + (sufixo == null ? "" : sufixo.toUpperCase(Locale.ROOT));
        }
        return null;
    }

    @Nullable
    private static String extractTensao(@NonNull String text) {
        // Procura tensões comuns em documentos: 110V, 127V, 220V, 380V, 440V, 480V
        Matcher m = Pattern.compile("(?i)\\b(110|127|208|220|230|240|380|400|415|440|460|480)\\s*V\\b").matcher(text);
        if (m.find()) {
            return m.group(1) + "V";
        }
        return null;
    }

    @Nullable
    private static String extractClienteObra(@NonNull String text) {
        // Só preenche se houver um rótulo claro (evita pegar descrições longas por engano).
        // Exemplos aceitos:
        // "Cliente/Obra: ABC - Planta X"
        // "Cliente: ABC" (pode haver "Obra: X" em outra linha)
        // "Obra: X"
        String cliente = null;
        String obra = null;

        Matcher mClienteObra = Pattern.compile("(?im)^[\\t ]*(CLIENTE\\s*/\\s*OBRA)\\s*[:\\-]\\s*([^\\n\\r]+)", Pattern.MULTILINE).matcher(text);
        if (mClienteObra.find()) {
            String v = safeLineValue(mClienteObra.group(2));
            return v.isEmpty() ? null : v;
        }

        // Aceita também "CLIENTE MPE ENGENHARIA" (sem ":"), que é comum no OCR.
        Matcher mCliente = Pattern.compile("(?i)\\bCLIENTE\\b\\s*[:\\-]?\\s*([^\\n\\r]+)").matcher(text);
        if (mCliente.find()) {
            cliente = safeLineValue(stripLeadingSeparators(mCliente.group(1)));
            if (isWordRibbonNoise(cliente)) {
                cliente = null;
            }
        }

        Matcher mObra = Pattern.compile("(?i)\\bOBRA\\b\\s*[:\\-]?\\s*([^\\n\\r]+)").matcher(text);
        if (mObra.find()) {
            obra = safeLineValue(stripLeadingSeparators(mObra.group(1)));
            if (isWordRibbonNoise(obra)) {
                obra = null;
            }
        }

        if (cliente != null && !cliente.isEmpty() && obra != null && !obra.isEmpty()) {
            return cliente + " - " + obra;
        }
        if (cliente != null && !cliente.isEmpty()) return cliente;
        if (obra != null && !obra.isEmpty()) return obra;
        // Sem fallback genérico: estava capturando texto da faixa do Word ("Revisão Exibir Ajuda").
        return null;
    }

    @NonNull
    private static String safeLineValue(@Nullable String s) {
        if (s == null) return "";
        String v = s.trim();
        // corta em caso de OCR juntar muita coisa em uma linha (mantém compacto)
        if (v.length() > 80) v = v.substring(0, 80).trim();
        // remove múltiplos espaços
        v = v.replaceAll("\\s{2,}", " ");
        return v;
    }

    private static boolean pareceNomeCliente(@Nullable String s) {
        if (s == null) return false;
        String v = s.trim();
        if (v.length() < 4) return false; // descarta coisas muito curtas tipo "F3"
        if (!v.contains(" ")) return false; // normalmente cliente/obra tem pelo menos um espaço
        int letras = 0;
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if (Character.isLetter(c)) letras++;
        }
        return letras >= 3;
    }

    @NonNull
    private static String stripLeadingSeparators(@Nullable String s) {
        if (s == null) return "";
        return s.replaceFirst("^[\\s:;\\-]+", "").trim();
    }

    private static boolean isWordRibbonNoise(@Nullable String s) {
        if (s == null) return false;
        String v = s.trim().toUpperCase(Locale.ROOT);
        if (v.isEmpty()) return false;
        // Palavras comuns do menu do Word em pt-BR
        return v.contains("REVIS") || v.contains("EXIB") || v.contains("AJUD")
                || v.contains("ARQUIV") || v.contains("INSER") || v.contains("LAYOUT")
                || v.contains("REFER") || v.contains("CORRESP")
                || v.contains("PÁGIN") || v.contains("PAGIN");
    }

    private static int indexOfRegex(@NonNull String regex, @NonNull String text) {
        try {
            Matcher m = Pattern.compile(regex).matcher(text);
            if (m.find()) return m.start();
        } catch (Exception ignored) {
        }
        return -1;
    }

    @Nullable
    private static String mapPrefixToModelKey(@NonNull String prefix) {
        // Normaliza alguns prefixos possíveis no Word
        if (prefix.startsWith("QCR")) return null; // ignorar
        if (prefix.startsWith("CABR")) return "cabr";
        if (prefix.startsWith("IRBR")) return "irbr";
        if (prefix.startsWith("UCABR")) return "ucabr";
        if (prefix.startsWith("EDBRSE") || prefix.startsWith("EUBRSE")) return "edbrse";
        if (prefix.startsWith("ESBR")) return "esbrag";
        return null;
    }

    @NonNull
    private static String safeUpper(@Nullable String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }

    private static int safeInt(@Nullable String s) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}

