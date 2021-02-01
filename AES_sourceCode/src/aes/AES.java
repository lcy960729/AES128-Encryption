package aes;

import java.math.BigInteger;
import java.util.LinkedList;

public class AES {
    private LinkedList<PrintAES_Result> printAES_results = new LinkedList<>();
    private DataTypeConverter dataTypeConverter = new DataTypeConverter();
    private BigInteger[] wordKey_List;

    StringBuilder state_result = new StringBuilder();

    public LinkedList<PrintAES_Result> getPrintAES_results() {
        return printAES_results;
    }

    public void clearPrintAES_results() {
        printAES_results.clear();
    }

    private void addResult(String round, String state, String roundKey) {
        printAES_results.add(new PrintAES_Result(round, state, roundKey));
        state_result.setLength(0);
    }

    // mode == 0 : encryption
    // mode == 1 : decryption
    public String run_Encryption_Or_Decryption(int mode, int textInputType, String text, int keyInputType, String keyStr) {
        StringBuilder cipherText = new StringBuilder();
        LinkedList<short[]> hex_List = get_Text_shortsList(textInputType, text);
        short[] key = get_Key_shortsList(keyInputType, keyStr);

        generate_key(key);

        for (int i = 0; i < hex_List.size(); i++) {
            state_result.setLength(0);
            state_result.append("plainText_hex\t: " + get_StateText(hex_List.get(i)) + "\n");
            state_result.append("key_hex\t\t: " + get_StateText(key));
            addResult("", state_result.toString(), "");

            short[] cipherState = run_Encryption_Or_Decryption(mode, hex_List.get(i));

            for (short value : cipherState) {
                String temp = Integer.toString(value, 16);

                if (temp.length() == 1) {
                    cipherText.append(0);
                }
                cipherText.append(temp.toUpperCase());
                cipherText.append(' ');
            }
        }

        return cipherText.toString();
    }


    private short[] run_Encryption_Or_Decryption(int mode, short[] state) {
        state = addRoundKey(mode, 0, state);

        for (int i = 1; i <= 10; i++) {
            state = subBytes(mode, state);
            state = shiftRows(mode, state);

            if (i != 10) {
                state = mixColumns(mode, state);
            }

            state = addRoundKey(mode, i, state);

        }
        return state;
    }

    private short[] subBytes(int mode, short[] state) {
        short[] table = (mode == 0) ? subBytes_Table : inv_subBytes_Table;

        for (int j = 0; j < state.length; j++) {
            short row = (short) ((state[j] & 0x00F0) >> 4);
            short col = (short) (state[j] & 0x000F);

            state[j] = table[(row * 16) + col];
        }

        state_result.append("subBytes\t\t: " + get_StateText(state) + "\n");

        return state;
    }

    private short[] shiftRows(int mode, short[] state) {
        short[] temp = {0, 0};

        if (mode == 0) {
            // 1번쨰
            temp[0] = state[1];
            for (int i = 0; i < 3; i++) {
                state[i * 4 + 1] = state[(i + 1) * 4 + 1];
            }
            state[13] = temp[0];

            // 3번쨰
            temp[0] = state[15];
            for (int i = 3; i > 0; i--) {
                state[i * 4 + 3] = state[(i - 1) * 4 + 3];
            }
            state[3] = temp[0];
        } else {
            // 1번쨰
            temp[0] = state[13];
            for (int i = 3; i > 0; i--) {
                state[i * 4 + 1] = state[(i - 1) * 4 + 1];
            }
            state[1] = temp[0];
            // 3번쨰
            temp[0] = state[3];
            for (int i = 0; i < 3; i++) {
                state[i * 4 + 3] = state[(i + 1) * 4 + 3];
            }
            state[15] = temp[0];
        }

        // 2번쨰
        temp[0] = state[2];
        temp[1] = state[6];

        state[2] = state[10];
        state[6] = state[14];
        state[10] = temp[0];
        state[14] = temp[1];


        state_result.append("shiftRows\t\t: " + get_StateText(state) + "\n");

        return state;
    }

    private short[] mixColumns(int mode, short[] _state) {
        short[] state = new short[16];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                short sum = 0;

                for (int k = 0; k < 4; k++) {
                    short[] table = (mode == 0) ? mixColumns_Table : inv_mixColumns_Table;
                    short mixColumnValue = table[j * 4 + k];
                    short x = _state[k + i * 4];

                    switch (mixColumnValue) {
                        case 0x02:
                            x = xTime(x);
                            break;
                        case 0x03:
                            x = (short) (xTime(x) ^ x);
                            break;
                        case 0x09:
                            x = (short) (xTime(xTime(xTime(x))) ^ x);
                            break;
                        case 0x0B:
                            x = (short) (xTime(xTime(xTime(x))) ^ xTime(x) ^ x);
                            break;
                        case 0x0D:
                            x = (short) (xTime(xTime(xTime(x))) ^ xTime(xTime(x)) ^ x);
                            break;
                        case 0x0E:
                            x = (short) (xTime(xTime(xTime(x))) ^ xTime(xTime(x)) ^ xTime(x));
                            break;
                    }

                    sum ^= x;
                }
                state[i * 4 + j] = sum;
            }
        }
        state_result.append("mixColumns\t: " + get_StateText(state) + "\n");

        return state;
    }

    private short[] addRoundKey(int mode, int round, short[] state) {
        int roundIndex = (mode == 0) ? round : 10 - round;

        short[] roundKey = get_RoundKey(roundIndex);

        if (mode == 1 && round != 0 && round != 10) {
            roundKey = mixColumns(mode, roundKey);
        }

        for (int i = 0; i < 16; i++) {
            state[i] ^= roundKey[i];
        }

        state_result.append("addRoundKey\t: " + get_StateText(state) + "\n");

        if (round == 0) {
            addResult("preRound", state_result.toString(), get_StateText(roundKey));
        } else
            addResult(String.valueOf(round), state_result.toString(), get_StateText(roundKey));

        return state;
    }

    private short[] get_RoundKey(int round) {
        short[] roundKey = new short[16];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) { //word 단위 키 4개로 roundKey가 됨
                short[] word_Key = dataTypeConverter.bigIntToShorts(wordKey_List[round * 4 + i]);
                roundKey[i * 4 + j] = word_Key[j];
            }
        }

        return roundKey;
    }

    private short xTime(short x) {
        x <<= 1;
        x = (short) ((x >= 0x0100) ? (x ^ 0x001b) : x);
        x &= 0xFF;
        return x;
    }

    private void generate_key(short[] key) {
        wordKey_List = new BigInteger[44];

        generate_key_0_3(key);
        generate_key_4_43();
    }

    private void generate_key_0_3(short[] key) {
        for (int wIndex = 0; wIndex < 4; wIndex++) {
            wordKey_List[wIndex] = (
                    BigInteger.valueOf(key[wIndex * 4] << 24)
                            .or(BigInteger.valueOf(key[wIndex * 4 + 1] << 16))
                            .or(BigInteger.valueOf(key[wIndex * 4 + 2] << 8))
                            .or(BigInteger.valueOf(key[wIndex * 4 + 3]))
            );
        }
    }

    private void generate_key_4_43() {
        for (int wIndex = 4; wIndex < 44; wIndex++) {
            if (wIndex % 4 == 0) {
                wordKey_List[wIndex] = generate_T(wIndex).xor(wordKey_List[wIndex - 4]);
            } else {
                wordKey_List[wIndex] = wordKey_List[wIndex - 1].xor(wordKey_List[wIndex - 4]);
            }
        }
    }

    private BigInteger generate_T(int round) {
        short[] word_Key = dataTypeConverter.bigIntToShorts(wordKey_List[round - 1]);

        word_Key = rotWord(word_Key);
        word_Key = subWord(word_Key);

        word_Key[0] ^= rCon_Table[(round / 4 - 1)];

        BigInteger t = dataTypeConverter.shortsToBigInt(word_Key);

        return t;
    }

    //rotate left 1word
    private short[] rotWord(short[] shorts) {
        short temp = shorts[0];

        for (int i = 0; i < 3; i++) {
            shorts[i] = shorts[i + 1];
        }
        shorts[3] = temp;

        return shorts;
    }

    private short[] subWord(short[] shorts) {
        shorts = subBytes(0, shorts);
        return shorts;
    }

    private LinkedList<short[]> get_Text_shortsList(int inputType, String str) {
        LinkedList<short[]> hex_List;

        if (inputType == 0) {
            hex_List = dataTypeConverter.stringToHex_Text(str);
        } else {
            hex_List = dataTypeConverter.hexStringToHex_Text(str);
        }

        return hex_List;
    }

    private short[] get_Key_shortsList(int inputType, String str) {
        short[] key;

        if (inputType == 0) {
            key = dataTypeConverter.stringToHex_Key(str);
        } else {
            key = dataTypeConverter.hexStringToHex_Key(str);
        }

        return key;
    }

    private String get_StateText(short[] plainText) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < plainText.length; i++) {
            String temp = Integer.toString(plainText[i], 16);

            if (temp.length() == 1) {
                stringBuilder.append(0);
            }
            stringBuilder.append(temp.toUpperCase());
            stringBuilder.append(' ');
        }
        return stringBuilder.toString();
    }

    final short[] subBytes_Table =
            {
                    0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76,
                    0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0,
                    0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15,
                    0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75,
                    0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84,
                    0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf,
                    0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8,
                    0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2,
                    0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73,
                    0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb,
                    0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79,
                    0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08,
                    0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a,
                    0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e,
                    0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf,
                    0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16
            };

    final short[] inv_subBytes_Table =
            {
                    0x52, 0x09, 0x6a, 0xd5, 0x30, 0x36, 0xa5, 0x38, 0xbf, 0x40, 0xa3, 0x9e, 0x81, 0xf3, 0xd7, 0xfb,
                    0x7c, 0xe3, 0x39, 0x82, 0x9b, 0x2f, 0xff, 0x87, 0x34, 0x8e, 0x43, 0x44, 0xc4, 0xde, 0xe9, 0xcb,
                    0x54, 0x7b, 0x94, 0x32, 0xa6, 0xc2, 0x23, 0x3d, 0xee, 0x4c, 0x95, 0x0b, 0x42, 0xfa, 0xc3, 0x4e,
                    0x08, 0x2e, 0xa1, 0x66, 0x28, 0xd9, 0x24, 0xb2, 0x76, 0x5b, 0xa2, 0x49, 0x6d, 0x8b, 0xd1, 0x25,
                    0x72, 0xf8, 0xf6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xd4, 0xa4, 0x5c, 0xcc, 0x5d, 0x65, 0xb6, 0x92,
                    0x6c, 0x70, 0x48, 0x50, 0xfd, 0xed, 0xb9, 0xda, 0x5e, 0x15, 0x46, 0x57, 0xa7, 0x8d, 0x9d, 0x84,
                    0x90, 0xd8, 0xab, 0x00, 0x8c, 0xbc, 0xd3, 0x0a, 0xf7, 0xe4, 0x58, 0x05, 0xb8, 0xb3, 0x45, 0x06,
                    0xd0, 0x2c, 0x1e, 0x8f, 0xca, 0x3f, 0x0f, 0x02, 0xc1, 0xaf, 0xbd, 0x03, 0x01, 0x13, 0x8a, 0x6b,
                    0x3a, 0x91, 0x11, 0x41, 0x4f, 0x67, 0xdc, 0xea, 0x97, 0xf2, 0xcf, 0xce, 0xf0, 0xb4, 0xe6, 0x73,
                    0x96, 0xac, 0x74, 0x22, 0xe7, 0xad, 0x35, 0x85, 0xe2, 0xf9, 0x37, 0xe8, 0x1c, 0x75, 0xdf, 0x6e,
                    0x47, 0xf1, 0x1a, 0x71, 0x1d, 0x29, 0xc5, 0x89, 0x6f, 0xb7, 0x62, 0x0e, 0xaa, 0x18, 0xbe, 0x1b,
                    0xfc, 0x56, 0x3e, 0x4b, 0xc6, 0xd2, 0x79, 0x20, 0x9a, 0xdb, 0xc0, 0xfe, 0x78, 0xcd, 0x5a, 0xf4,
                    0x1f, 0xdd, 0xa8, 0x33, 0x88, 0x07, 0xc7, 0x31, 0xb1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xec, 0x5f,
                    0x60, 0x51, 0x7f, 0xa9, 0x19, 0xb5, 0x4a, 0x0d, 0x2d, 0xe5, 0x7a, 0x9f, 0x93, 0xc9, 0x9c, 0xef,
                    0xa0, 0xe0, 0x3b, 0x4d, 0xae, 0x2a, 0xf5, 0xb0, 0xc8, 0xeb, 0xbb, 0x3c, 0x83, 0x53, 0x99, 0x61,
                    0x17, 0x2b, 0x04, 0x7e, 0xba, 0x77, 0xd6, 0x26, 0xe1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0c, 0x7d
            };


    final short[] mixColumns_Table = {
            0x02, 0x03, 0x01, 0x01,
            0x01, 0x02, 0x03, 0x01,
            0x01, 0x01, 0x02, 0x03,
            0x03, 0x01, 0x01, 0x02,
    };

    final short[] inv_mixColumns_Table = {
            0x0E, 0x0B, 0x0D, 0x09,
            0x09, 0x0E, 0x0B, 0x0D,
            0x0D, 0x09, 0x0E, 0x0B,
            0x0B, 0x0D, 0x09, 0x0E,

            0x0E, 0x0B, 0x0D, 0x09,
            0x09, 0x0E, 0x0B, 0x0D,
            0x0D, 0x09, 0x0E, 0x0B,
            0x0B, 0x0D, 0x09, 0x0E,
    };

    final short[] rCon_Table = {
            0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1B, 0x36
    };
}
