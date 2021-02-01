package aes;

import javafx.scene.control.Alert;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.Scanner;

public class DataTypeConverter {

    public String encodeUTF8(String str) {
        try {
            str = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return str;
    }

    public String decodeUTF8(String str) {
        String decode_str;
        try {
            str = str.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            decode_str = URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();

            return str;
        }

        return decode_str;
    }

    public String hexStringToString(String hexStr) {

        Scanner scanner = new Scanner(hexStr);
        StringBuilder stringBuilder = new StringBuilder();

        while (scanner.hasNext()) {
            stringBuilder.append((char) Integer.parseInt(scanner.next(), 16));
        }

        String result = stringBuilder.toString();
        result = decodeUTF8(result);
        return result;
    }

    public LinkedList<short[]> stringToHex_Text(String str) {
        LinkedList<short[]> charList = new LinkedList<short[]>();
        str = encodeUTF8(str);

        char[] chars = str.toCharArray();
        short[] result = new short[16];

        for (int i = 0; i < chars.length; i++) {
            result[i % 16] = (short) chars[i];

            if ((i % 16 == 15) || (i == chars.length - 1)) {
                charList.add(result);
                result = new short[16];
            }
        }

        return charList;
    }

    public LinkedList<short[]> hexStringToHex_Text(String str) {
        LinkedList<short[]> charList = new LinkedList<short[]>();
        short[] result = new short[16];

        str = str.replaceAll(" ", "");

        int count = 0;
        for (int i = 0; i < str.length(); i += 2){
            try {
                result[count++] = Short.parseShort(str.substring(i, i + 2), 16);
            }catch (NumberFormatException e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Input type is hex");
                alert.setContentText("Enter 16 hex values");
                alert.show();

                return null;
            }

            if (count == 16 || i+2 >= str.length()) {
                count = 0;
                charList.add(result);
                result = new short[16];
            }
        }

        return charList;
    }

    public short[] stringToHex_Key(String str) {
        str = encodeUTF8(str);

        char[] chars = str.toCharArray();

        short[] result = new short[16];
        for (int i = 0; i < chars.length; i++) {
            result[i] = (short) chars[i];
        }

        return result;
    }

    public short[] hexStringToHex_Key(String str) {
        short[] result = new short[16];

        str = str.replaceAll(" ", "");
        try {
            BigInteger bigInteger = new BigInteger(str,16);

            for (int i = 0; i < 16; i++){
                result[i] = bigInteger.shiftRight(120 - (i * 8))
                        .and(BigInteger.valueOf(0xFF))
                        .shortValue();
            }
        }catch (NumberFormatException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Input type is hex");
            alert.setContentText("Enter 16 hex values");
            alert.show();

            return null;
        }

        return result;
    }

    public BigInteger shortsToBigInt(short[] shorts) {
        BigInteger bigInteger = (BigInteger.valueOf(shorts[0]).shiftLeft(24))
                .or(BigInteger.valueOf(shorts[1]).shiftLeft(16))
                .or(BigInteger.valueOf(shorts[2]).shiftLeft(8))
                .or(BigInteger.valueOf(shorts[3]));

        return bigInteger;
    }

    public short[] bigIntToShorts(BigInteger bigInteger) {
        short[] shorts = new short[4];

        for (int i = 0; i < 4; i++) {
            shorts[i] = ((bigInteger
                    .shiftRight(24 - (i * 8)))
                    .and(BigInteger.valueOf(0x00FF)))
                    .shortValue();
        }

        return shorts;
    }
}
