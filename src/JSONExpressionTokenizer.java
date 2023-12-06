

/**
 * This class breaks up a string describing an expression into tokens: numbers,
 * parentheses, and operators.
 */
public final class JSONExpressionTokenizer {

    /**
     *
     */
    private String input;
    /**
     *
     */
    private int start; // The start of the current token
    /**
     *
     */
    private int end; // The position after the end of the current token

    /**
     * Constructs a tokenizer.
     *
     * @param anInput
     *            the string to tokenize
     */
    public JSONExpressionTokenizer(String anInput) {
        this.input = anInput;
        this.start = 0;
        this.end = 0;
        this.nextToken(); // Find the first token
    }

    /**
     * Peeks at the next token without consuming it.
     *
     * @return the next token or null if there are no more tokens
     */
    public String peekToken() {
        if (this.start >= this.input.length()) {
            return null;
        } else {
            return this.input.substring(this.start, this.end);
        }
    }

    /**
     * Gets the next token and moves the tokenizer to the following token.
     *
     * @return the next token or null if there are no more tokens
     */
    public String nextToken() {
        String r = this.peekToken();
        this.start = this.end;
        if (this.start >= this.input.length()) {
            return r;
        }
        if (Character.isDigit(this.input.charAt(this.start))) {
            this.end = this.start + 1;
            while (this.end < this.input.length()
                    && Character.isDigit(this.input.charAt(this.end))) {
                this.end++;
            }
        } else {
            this.end = this.start + 1;
        }
        return r;
    }

}
