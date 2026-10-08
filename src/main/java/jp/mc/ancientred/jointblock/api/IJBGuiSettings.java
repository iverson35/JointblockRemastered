package jp.mc.ancientred.jointblock.api;

import java.text.ChoiceFormat;
import java.text.Format;

public interface IJBGuiSettings {

    TriButtonState[] defaultButtonStates = new TriButtonState[]{
        new TriButtonState("-10", "x10", "+10", -10.0, 10.0),
        new TriButtonState("-1", "x1", "+1", -1.0, 1.0),
        new TriButtonState("MIN", "<->", "MAX", -2.147483648E9, 2.147483647E9)
    };

    void setDescriptionText(String text, int index);

    void setParamEntryState(int index, ChoiceFormat choiceFormat, Format format, int min, int max);

    void setParamEntryState(int index, ChoiceFormat choiceFormat, String text, int min, int max);

    void setParamEntryState(int index, String text, Format format, int min, int max);

    void setParamEntryState(int index, String text, String text2, int min, int max);

    void setParamButtonToggleAry(int index, TriButtonState[] states, int selected);

    class TriButtonState {
        public String left;
        public String center;
        public String right;
        public double amountLeft;
        public double amountRight;

        public TriButtonState(String left, String center, String right, double amountLeft, double amountRight) {
            this.left = left;
            this.center = center;
            this.right = right;
            this.amountLeft = amountLeft;
            this.amountRight = amountRight;
        }

        public TriButtonState(String left, String right, double amountLeft, double amountRight) {
            this.left = left;
            this.center = null;
            this.right = right;
            this.amountLeft = amountLeft;
            this.amountRight = amountRight;
        }
    }
}
