package com.example.checklistjson;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class QrUtils {

    private QrUtils() {}

    public static Map<String, String> parseQr(String qrText) {
        Map<String, String> map = new HashMap<>();
        if (qrText == null) return map;

        // 1) Formato recomendado: CHAVE=VALOR;CHAVE=VALOR...
        if (qrText.contains("=")) {
            String[] partes = qrText.split(";");
            for (String p : partes) {
                String[] kv = p.split("=", 2);
                if (kv.length == 2) {
                    String chave = kv[0].trim().toUpperCase();
                    String valor = kv[1].trim();
                    if (!chave.isEmpty()) {
                        map.put(chave, valor);
                    }
                }
            }
        }

        // 2) Formatos “livres”
        if (!map.containsKey("DATA_CAL")) {
            Matcher mData = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})").matcher(qrText);
            if (mData.find()) {
                map.put("DATA_CAL", mData.group(1));
            }
        }

        if (!map.containsKey("NUM")) {
            // Casos comuns no seu padrão:
            // "N° Instr. medição: 01" / "Nº Instr medicao: 01" / "N° Instrumento: 01"
            Matcher mNumInstr = Pattern.compile(
                    "(?i)\\bN\\s*[º°o]?\\s*(?:instr\\.?\\s*(?:med[ií]c[aã]o)?|instrumento)\\s*[:=]\\s*([A-Za-z0-9._-]+)"
            ).matcher(qrText);
            if (mNumInstr.find()) {
                map.put("NUM", mNumInstr.group(1));
            } else {
                // Fallback genérico: "Nº: MN1234" / "N° 01" / "No=01"
                Matcher mNum = Pattern.compile("(?i)\\bN\\s*[º°o]?\\s*[:=]?\\s*([A-Za-z0-9._-]+)").matcher(qrText);
                if (mNum.find()) {
                    String candidato = mNum.group(1);
                    // Se o candidato for só "Instr" (ou parecido), tenta achar o número após os dois-pontos
                    if (candidato != null && candidato.replace(".", "").trim().equalsIgnoreCase("INSTR")) {
                        Matcher mAposDoisPontos = Pattern.compile("[:=]\\s*([0-9]{1,6})\\b").matcher(qrText);
                        if (mAposDoisPontos.find()) {
                            map.put("NUM", mAposDoisPontos.group(1));
                        } else {
                            map.put("NUM", candidato);
                        }
                    } else {
                        // Se capturou uma palavra (ex.: "torquimetro") e existe um número após ":"/"=",
                        // preferimos o número.
                        Matcher mAposDoisPontos = Pattern.compile("[:=]\\s*([0-9]{1,10})\\b").matcher(qrText);
                        if (mAposDoisPontos.find()) {
                            map.put("NUM", mAposDoisPontos.group(1));
                        } else {
                            map.put("NUM", candidato);
                        }
                    }
                }
            }
        }

        if (!map.containsKey("TIPO")) {
            Matcher mTipo = Pattern.compile("(?i)\\bTIPO\\s*[:=]\\s*([A-Za-z0-9_ -]+)").matcher(qrText);
            if (mTipo.find()) {
                map.put("TIPO", mTipo.group(1).trim().toUpperCase());
            }
        }

        if (!map.containsKey("DESCRICAO")) {
            Matcher mDesc = Pattern.compile("(?i)\\bDESCRICAO\\s*[:=]\\s*(.+)$").matcher(qrText);
            if (mDesc.find()) {
                map.put("DESCRICAO", mDesc.group(1).trim());
            }
        }

        // Instrumentos do vácuo: BOMBA e VACUOMETRO/MANIFOLD
        if (!map.containsKey("BOMBA")) {
            // Ex.: "N° Bomba: 123" / "Bomba vácuo = 123"
            // Aceita também "N°Bomba vácuo: 03"
            Matcher mBomba = Pattern.compile(
                    "(?i)\\bN\\s*[º°o]?\\s*BOMBA\\b[^\\n\\r]{0,40}?[:=]\\s*([A-Za-z0-9._-]+)"
            ).matcher(qrText);
            if (mBomba.find()) {
                map.put("BOMBA", mBomba.group(1));
            } else {
                Matcher mBomba2 = Pattern.compile("(?i)\\bBOMBA\\b[^A-Za-z0-9]{0,20}[:=]?\\s*([A-Za-z0-9._-]+)").matcher(qrText);
                if (mBomba2.find()) {
                    map.put("BOMBA", mBomba2.group(1));
                }
            }
        }

        if (!map.containsKey("VACUOMETRO")) {
            // Ex.: "N° Vacuômetro / Manifold: VM01" / "Vacuometro: VM01" / "Manifold: MF01"
            // Aceita grafias comuns/erradas como "Vacuôemetro"
            Matcher mVac = Pattern.compile(
                    "(?i)\\bN\\s*[º°o]?\\s*(?:VACU[ÔO]METRO|VACUOMETRO|VACU[ÔO]EMETRO|VACUOEMETRO)\\b[^\\n\\r]{0,60}?[:=]\\s*([A-Za-z0-9._-]+)"
            ).matcher(qrText);
            if (mVac.find()) {
                map.put("VACUOMETRO", mVac.group(1));
            } else {
                Matcher mManifold = Pattern.compile("(?i)\\bMANIFOLD\\b[^\\n\\r]{0,40}?[:=]\\s*([A-Za-z0-9._-]+)").matcher(qrText);
                if (mManifold.find()) {
                    map.put("VACUOMETRO", mManifold.group(1));
                } else {
                    Matcher mVac2 = Pattern.compile("(?i)\\b(VACU[ÔO]METRO|VACUOMETRO|MANIFOLD)\\b[^A-Za-z0-9]{0,20}[:=]?\\s*([A-Za-z0-9._-]+)").matcher(qrText);
                    if (mVac2.find()) {
                        map.put("VACUOMETRO", mVac2.group(2));
                    }
                }
            }
        }

        // Instrumento: BALANCA
        if (!map.containsKey("BALANCA")) {
            Matcher mBal = Pattern.compile(
                    "(?i)\\bN\\s*[º°o]?\\s*(?:BALAN[CÇ]A)\\b[^\\n\\r]{0,40}?[:=]\\s*([A-Za-z0-9._-]+)"
            ).matcher(qrText);
            if (mBal.find()) {
                map.put("BALANCA", mBal.group(1));
            } else {
                Matcher mBal2 = Pattern.compile("(?i)\\bBALAN[CÇ]A\\b[^\\n\\r]{0,40}?[:=]\\s*([A-Za-z0-9._-]+)").matcher(qrText);
                if (mBal2.find()) {
                    map.put("BALANCA", mBal2.group(1));
                }
            }
        }

        // Instrumento: MANOMETRO
        if (!map.containsKey("MANOMETRO")) {
            Matcher mMan = Pattern.compile(
                    "(?i)\\bN\\s*[º°o]?\\s*(?:MAN[ÔO]METRO|MANOMETRO)\\b[^\\n\\r]{0,40}?[:=]\\s*([A-Za-z0-9._-]+)"
            ).matcher(qrText);
            if (mMan.find()) {
                map.put("MANOMETRO", mMan.group(1));
            } else {
                Matcher mMan2 = Pattern.compile("(?i)\\b(MAN[ÔO]METRO|MANOMETRO)\\b[^\\n\\r]{0,40}?[:=]\\s*([A-Za-z0-9._-]+)").matcher(qrText);
                if (mMan2.find()) {
                    map.put("MANOMETRO", mMan2.group(2));
                }
            }
        }

        // Instrumento: TORQUIMETRO
        if (!map.containsKey("TORQUIMETRO")) {
            Matcher mTq = Pattern.compile(
                    "(?i)\\bN\\s*[º°o]?\\s*(?:TORQU[IÍ]METRO|TORQUIMETRO)\\b[^\\n\\r]{0,40}?[:=]\\s*([A-Za-z0-9._-]+)"
            ).matcher(qrText);
            if (mTq.find()) {
                map.put("TORQUIMETRO", mTq.group(1));
            } else {
                Matcher mTq2 = Pattern.compile("(?i)\\b(TORQU[IÍ]METRO|TORQUIMETRO)\\b[^\\n\\r]{0,40}?[:=]\\s*([A-Za-z0-9._-]+)").matcher(qrText);
                if (mTq2.find()) {
                    map.put("TORQUIMETRO", mTq2.group(2));
                }
            }
        }

        return map;
    }
}

