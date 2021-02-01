package aes;

public class PrintAES_Result {
    private String round;
    private String state;
    private String roundKey;

    public PrintAES_Result(String round, String state, String roundKey) {
        this.round = round;
        this.state = state;
        this.roundKey = roundKey;
    }

    public String getRound() {
        return round;
    }


    public String getState() {
        return state;
    }

    public String getRoundKey() {
        return roundKey;
    }

}
