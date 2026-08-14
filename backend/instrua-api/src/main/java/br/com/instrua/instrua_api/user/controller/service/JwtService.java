package br.com.instrua.instrua_api.user.controller.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, claims -> (String) claims.get("sub"));
    }

    public <T> T extractClaim(String token, Function<Map<String, Object>, T> claimsResolver) {
        Map<String, Object> claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username);
    }

    public String generateToken(Map<String, Object> extraClaims, String username) {
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new HashMap<>(extraClaims);
        long issuedAtSeconds = System.currentTimeMillis() / 1000;
        payload.put("sub", username);
        payload.put("iat", issuedAtSeconds);
        payload.put("exp", issuedAtSeconds + (jwtExpiration / 1000));

        String encodedHeader = encodeBase64Url(serializeJson(header).getBytes(StandardCharsets.UTF_8));
        String encodedPayload = encodeBase64Url(serializeJson(payload).getBytes(StandardCharsets.UTF_8));
        String unsignedToken = encodedHeader + "." + encodedPayload;
        String signature = encodeBase64Url(hmacSha256(unsignedToken));
        return unsignedToken + "." + signature;
    }

    public boolean isTokenValid(String token, String username) {
        try {
            String tokenUsername = extractUsername(token);
            return username.equals(tokenUsername) && !isTokenExpired(token);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    private Date extractExpiration(String token) {
        Object claim = extractClaim(token, claims -> claims.get("exp"));
        long expSeconds = claim instanceof Number ? ((Number) claim).longValue() : Long.parseLong(claim.toString());
        return new Date(expSeconds * 1000);
    }

    private Map<String, Object> extractAllClaims(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("JWT token must consist of 3 parts");
        }

        byte[] signatureBytes = decodeBase64Url(parts[2]);
        String unsignedToken = parts[0] + "." + parts[1];
        byte[] expectedSignature = hmacSha256(unsignedToken);
        if (!java.security.MessageDigest.isEqual(expectedSignature, signatureBytes)) {
            throw new IllegalArgumentException("Invalid JWT signature");
        }

        String payloadJson = new String(decodeBase64Url(parts[1]), StandardCharsets.UTF_8);
        return parseJsonToMap(payloadJson);
    }

    private byte[] hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(getSigningKey());
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("Unable to compute HMAC SHA-256", ex);
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    private String encodeBase64Url(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private byte[] decodeBase64Url(String encoded) {
        return Base64.getUrlDecoder().decode(encoded);
    }

    private String serializeJson(Map<String, Object> map) {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(escapeJsonString(entry.getKey())).append('"');
            builder.append(':');
            serializeJsonValue(entry.getValue(), builder);
        }
        builder.append('}');
        return builder.toString();
    }

    private void serializeJsonValue(Object value, StringBuilder builder) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof String) {
            builder.append('"').append(escapeJsonString((String) value)).append('"');
        } else if (value instanceof Number || value instanceof Boolean) {
            builder.append(value.toString());
        } else if (value instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapValue = (Map<String, Object>) value;
            builder.append(serializeJson(mapValue));
        } else if (value instanceof List) {
            builder.append('[');
            boolean first = true;
            for (Object item : (List<?>) value) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                serializeJsonValue(item, builder);
            }
            builder.append(']');
        } else {
            builder.append('"').append(escapeJsonString(value.toString())).append('"');
        }
    }

    private Map<String, Object> parseJsonToMap(String json) {
        int[] idx = {0};
        skipWhitespace(json, idx);
        if (idx[0] >= json.length() || json.charAt(idx[0]) != '{') {
            throw new IllegalArgumentException("Invalid JSON object");
        }
        idx[0]++;

        Map<String, Object> result = new HashMap<>();
        skipWhitespace(json, idx);
        if (idx[0] < json.length() && json.charAt(idx[0]) == '}') {
            idx[0]++;
            return result;
        }

        while (true) {
            skipWhitespace(json, idx);
            String key = parseJsonString(json, idx);
            skipWhitespace(json, idx);
            if (idx[0] >= json.length() || json.charAt(idx[0]) != ':') {
                throw new IllegalArgumentException("Expected ':' after key");
            }
            idx[0]++;
            skipWhitespace(json, idx);
            Object value = parseJsonValue(json, idx);
            result.put(key, value);
            skipWhitespace(json, idx);
            if (idx[0] >= json.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON object");
            }
            char next = json.charAt(idx[0]);
            if (next == ',') {
                idx[0]++;
                continue;
            }
            if (next == '}') {
                idx[0]++;
                break;
            }
            throw new IllegalArgumentException("Expected ',' or '}' in JSON object");
        }
        return result;
    }

    private Object parseJsonValue(String json, int[] idx) {
        skipWhitespace(json, idx);
        if (idx[0] >= json.length()) {
            throw new IllegalArgumentException("Unexpected end of JSON value");
        }
        char current = json.charAt(idx[0]);
        if (current == '"') {
            return parseJsonString(json, idx);
        }
        if (current == '{') {
            return parseJsonObject(json, idx);
        }
        if (current == '[') {
            return parseJsonArray(json, idx);
        }
        if (current == 't' && json.startsWith("true", idx[0])) {
            idx[0] += 4;
            return Boolean.TRUE;
        }
        if (current == 'f' && json.startsWith("false", idx[0])) {
            idx[0] += 5;
            return Boolean.FALSE;
        }
        if (current == 'n' && json.startsWith("null", idx[0])) {
            idx[0] += 4;
            return null;
        }
        return parseJsonNumber(json, idx);
    }

    private String parseJsonString(String json, int[] idx) {
        if (json.charAt(idx[0]) != '"') {
            throw new IllegalArgumentException("Invalid JSON string");
        }
        idx[0]++;
        StringBuilder builder = new StringBuilder();
        while (idx[0] < json.length()) {
            char current = json.charAt(idx[0]++);
            if (current == '"') {
                return builder.toString();
            }
            if (current == '\\') {
                if (idx[0] >= json.length()) {
                    throw new IllegalArgumentException("Invalid escape sequence in JSON string");
                }
                char escape = json.charAt(idx[0]++);
                switch (escape) {
                    case '"': builder.append('"'); break;
                    case '\\': builder.append('\\'); break;
                    case '/': builder.append('/'); break;
                    case 'b': builder.append('\b'); break;
                    case 'f': builder.append('\f'); break;
                    case 'n': builder.append('\n'); break;
                    case 'r': builder.append('\r'); break;
                    case 't': builder.append('\t'); break;
                    case 'u':
                        if (idx[0] + 4 > json.length()) {
                            throw new IllegalArgumentException("Invalid unicode escape sequence");
                        }
                        String hex = json.substring(idx[0], idx[0] + 4);
                        idx[0] += 4;
                        builder.append((char) Integer.parseInt(hex, 16));
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported escape sequence: \\" + escape);
                }
            } else {
                builder.append(current);
            }
        }
        throw new IllegalArgumentException("Unterminated JSON string");
    }

    private Map<String, Object> parseJsonObject(String json, int[] idx) {
        if (json.charAt(idx[0]) != '{') {
            throw new IllegalArgumentException("Invalid JSON object");
        }
        idx[0]++;
        Map<String, Object> map = new HashMap<>();
        skipWhitespace(json, idx);
        if (idx[0] < json.length() && json.charAt(idx[0]) == '}') {
            idx[0]++;
            return map;
        }
        while (true) {
            skipWhitespace(json, idx);
            String key = parseJsonString(json, idx);
            skipWhitespace(json, idx);
            if (idx[0] >= json.length() || json.charAt(idx[0]) != ':') {
                throw new IllegalArgumentException("Expected ':' after key");
            }
            idx[0]++;
            skipWhitespace(json, idx);
            Object value = parseJsonValue(json, idx);
            map.put(key, value);
            skipWhitespace(json, idx);
            if (idx[0] >= json.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON object");
            }
            char next = json.charAt(idx[0]);
            if (next == ',') {
                idx[0]++;
                continue;
            }
            if (next == '}') {
                idx[0]++;
                break;
            }
            throw new IllegalArgumentException("Expected ',' or '}' in JSON object");
        }
        return map;
    }

    private List<Object> parseJsonArray(String json, int[] idx) {
        if (json.charAt(idx[0]) != '[') {
            throw new IllegalArgumentException("Invalid JSON array");
        }
        idx[0]++;
        List<Object> list = new ArrayList<>();
        skipWhitespace(json, idx);
        if (idx[0] < json.length() && json.charAt(idx[0]) == ']') {
            idx[0]++;
            return list;
        }
        while (true) {
            skipWhitespace(json, idx);
            list.add(parseJsonValue(json, idx));
            skipWhitespace(json, idx);
            if (idx[0] >= json.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON array");
            }
            char next = json.charAt(idx[0]);
            if (next == ',') {
                idx[0]++;
                continue;
            }
            if (next == ']') {
                idx[0]++;
                break;
            }
            throw new IllegalArgumentException("Expected ',' or ']' in JSON array");
        }
        return list;
    }

    private Object parseJsonNumber(String json, int[] idx) {
        int start = idx[0];
        if (json.charAt(idx[0]) == '-') {
            idx[0]++;
        }
        while (idx[0] < json.length() && Character.isDigit(json.charAt(idx[0]))) {
            idx[0]++;
        }
        boolean isDouble = false;
        if (idx[0] < json.length() && json.charAt(idx[0]) == '.') {
            isDouble = true;
            idx[0]++;
            while (idx[0] < json.length() && Character.isDigit(json.charAt(idx[0]))) {
                idx[0]++;
            }
        }
        if (idx[0] < json.length() && (json.charAt(idx[0]) == 'e' || json.charAt(idx[0]) == 'E')) {
            isDouble = true;
            idx[0]++;
            if (idx[0] < json.length() && (json.charAt(idx[0]) == '+' || json.charAt(idx[0]) == '-')) {
                idx[0]++;
            }
            while (idx[0] < json.length() && Character.isDigit(json.charAt(idx[0]))) {
                idx[0]++;
            }
        }
        String number = json.substring(start, idx[0]);
        try {
            if (isDouble) {
                return Double.parseDouble(number);
            }
            return Long.parseLong(number);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid JSON number: " + number, ex);
        }
    }

    private void skipWhitespace(String json, int[] idx) {
        while (idx[0] < json.length() && Character.isWhitespace(json.charAt(idx[0]))) {
            idx[0]++;
        }
    }

    private String escapeJsonString(String value) {
        StringBuilder builder = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"': builder.append("\\\""); break;
                case '\\': builder.append("\\\\"); break;
                case '\b': builder.append("\\b"); break;
                case '\f': builder.append("\\f"); break;
                case '\n': builder.append("\\n"); break;
                case '\r': builder.append("\\r"); break;
                case '\t': builder.append("\\t"); break;
                default:
                    if (c < 0x20 || c > 0x7E) {
                        builder.append(String.format("\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
            }
        }
        return builder.toString();
    }
}
