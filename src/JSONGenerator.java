
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;

import components.binarytree.BinaryTree;
import components.binarytree.BinaryTree1;

/**
 * A class to generate a JSON file given a mathematical expression.
 */
/**
 * @author paolo
 *
 */
/**
 * @author paolo
 *
 */
/**
 * @author paolo
 *
 */
public final class JSONGenerator {

    /**
     *
     */
    private static JSONExpressionTokenizer tokenizer;

    /**
     * Constructs an evaluator.
     */
    private JSONGenerator() {
    }

    /**
     * Evaluates the expression.
     *
     * @return the value of the expression.
     */
    private static BinaryTree<String> expressionTree() throws ParseException {
        BinaryTree<String> expTree = termTree();
        BinaryTree<String> subexpTree1 = new BinaryTree1<String>();
        boolean done = false;
        while (!done) {
            String next = tokenizer.peekToken();
            // next could be null, that's why this funny syntax is used
            if ("+".equals(next) || "-".equals(next)) {
                tokenizer.nextToken();
                BinaryTree<String> subexpTree2 = termTree();
                subexpTree1.transferFrom(expTree);
                expTree.assemble(next, subexpTree1, subexpTree2);
            } else {
                done = true;
            }
        }
        return expTree;
    }

    /**
     * Evaluates the next term found in the expression.
     *
     * @return the value of the term
     */
    private static BinaryTree<String> termTree() throws ParseException {
        BinaryTree<String> expTree = powerTree();
        BinaryTree<String> subexpTree1 = new BinaryTree1<String>();
        boolean done = false;
        while (!done) {
            String next = tokenizer.peekToken();
            // next could be null, that's why this funny syntax is used
            if ("*".equals(next) || "/".equals(next) || "%".equals(next)) {
                tokenizer.nextToken();
                BinaryTree<String> subexpTree2 = powerTree();
                subexpTree1.transferFrom(expTree);
                expTree.assemble(next, subexpTree1, subexpTree2);
            } else {
                done = true;
            }
        }
        return expTree;
    }

    /**
     * Evaluates the next power/root found in the expression.
     *
     * @return the value of the power/root
     */
    private static BinaryTree<String> powerTree() throws ParseException {
        BinaryTree<String> expTree = factorTree();
        BinaryTree<String> subexpTree1 = new BinaryTree1<String>();
        boolean done = false;
        while (!done) {
            String next = tokenizer.peekToken();
            // next could be null, that's why this funny syntax is used
            if ("^".equals(next) || "v".equals(next)) {
                tokenizer.nextToken();
                BinaryTree<String> subexpTree2 = factorTree();
                subexpTree1.transferFrom(expTree);
                expTree.assemble(next, subexpTree1, subexpTree2);
            } else {
                done = true;
            }
        }
        return expTree;
    }

    /**
     * Evaluates the next factor found in the expression.
     *
     * @return the value of the factor
     */
    private static BinaryTree<String> factorTree() throws ParseException {
        BinaryTree<String> expTree = new BinaryTree1<String>();
        String next = tokenizer.peekToken();
        // next could be null, that's why this funny syntax is used
        if ("(".equals(next)) {
            tokenizer.nextToken(); // Discard "("
            expTree.transferFrom(expressionTree());
            next = tokenizer.peekToken();
            if (")".equals(next)) {
                tokenizer.nextToken(); // Discard ")"
            } else if (next == null) {
                throw new ParseException("Missing ')'", 0);
            } else {
                throw new ParseException("Unexpected token: " + next, 0);
            }
        } else if (next == null) {
            throw new ParseException("Incomplete expression", 0);
        } else { // should be a digit sequence (a number)
            if (next.matches("0|[1-9]\\d*")) {
                expTree.assemble(tokenizer.nextToken(), expTree.newInstance(),
                        expTree.newInstance());
            } else {
                throw new ParseException("Unexpected token: " + next, 0);
            }
        }
        return expTree;
    }

    /**
     * @param level
     * @param out
     */
    private static void printSpaces(int level, PrintWriter out) {
        for (int i = 0; i < level; i++) {
            out.print("  ");
        }
    }

    /**
     * Outputs the JSON to a file while reading the expression tree
     *
     * @param expTree
     * @param out
     * @param level
     */
    private static void outputTree(BinaryTree<String> expTree, PrintWriter out,
            int level) {
        BinaryTree<String> left = expTree.newInstance();
        BinaryTree<String> right = expTree.newInstance();
        String root = expTree.disassemble(left, right);
        boolean leaf = false;

        System.out.println(root);

        switch (root.charAt(0)) {
            case '+':
                printNotLeaf(level, out, "plus");
                break;
            case '-':
                printNotLeaf(level, out, "minus");
                break;
            case '*':
                printNotLeaf(level, out, "times");
                break;
            case '/':
                printNotLeaf(level, out, "divide");
                break;
            case '%':
                printNotLeaf(level, out, "mod");
                break;
            case '^':
                printNotLeaf(level, out, "power");
                break;
            default:
                leaf = true;
                out.print(root);
                break;
        }

        if (!leaf) {
            printSpaces(level, out);
            out.print("\"children\": [");
            outputTree(left, out, level + 1);
            out.print(", ");
            outputTree(right, out, level + 1);
            out.println("]");
            printSpaces(level, out);
            out.println("}");
        }
        expTree.assemble(root, left, right);
    }

    /**
     * Helper method to print an opening expression given a the operator
     *
     * @param level
     * @param out
     * @param operator
     */
    public static void printNotLeaf(int level, PrintWriter out,
            String operator) {
        printSpaces(level, out);
        out.println("\n{");
        printSpaces(level, out);
        out.println("\"operator\": \"" + operator + "\",");

    }

    /**
     * @param exp
     * @return whether exp can be parsed or not
     */
    public static boolean canBeParsed(String exp) {
        try {
            JSONGenerator.tokenizer = new JSONExpressionTokenizer(exp);
            expressionTree();
            String next = tokenizer.peekToken();
            if (next != null) {
                // System.err.println("Extra token after expression: " + next  + ": 4");
                return false;
            }
        } catch (ParseException e) {
            // System.err.println(e.getMessage() + ": " + e.getErrorOffset());
            return false;
        }
        return true;
    }

    /**
     * @param exp
     * @param file
     * @throws IOException
     * @throws ParseException
     */
    public static void save(String exp, String file)
            throws IOException, ParseException {
        PrintWriter out = new PrintWriter(
                new BufferedWriter(new FileWriter(file)));

        JSONGenerator.tokenizer = new JSONExpressionTokenizer(exp);
        BinaryTree<String> expTree = expressionTree();
        outputTree(expTree, out, 0);

        out.close();
    }

}
