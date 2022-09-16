package cn.smallpotato.common.model;

/**
 * @author panjb
 */
public interface Element {
    Element POISON_PILL = new Element() {};

    class StringElement implements Element {
        private final String text;

        public StringElement(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}
