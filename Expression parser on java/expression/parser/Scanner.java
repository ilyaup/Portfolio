package expression.parser;

import java.io.*;

public class Scanner {
    final private Reader in;
    private char[] buffer = new char[1024];
    private int read = 0;
    private int locale = 0;
    private int num = -3;
    private int linesCount = 1;
    private int wordsCount = 0;
    private boolean isEnd = false;
    private boolean hasNum = false;
    private boolean hasWord = false;
    private boolean hasLine = false;
    private boolean newLine = false;
    private String separator = System.lineSeparator();
    private int n = separator.length();
    private StringBuilder sb;

    Scanner(String file, String encoding) throws IOException {
        in = new InputStreamReader(new FileInputStream(file), encoding);
    }

    Scanner(InputStream input) throws IOException {
        in = new InputStreamReader(input);
    }

    Scanner(String string) {
        in = new StringReader(string);
    }

    private boolean isLegal(char a) {
        return Character.isLetter(a) || Character.getType(a) == Character.DASH_PUNCTUATION || a == '\'';
    }

    public int nextInt() throws IOException {
        if(hasNextInt()) {
            hasNum = false;
            return Integer.parseUnsignedInt(sb.toString(), 16);
        } else {
            return -1;
        }
    }

    public boolean hasNextInt() throws FileNotFoundException, IOException {
        if (hasNum) {
            return true;
        }
        sb = new StringBuilder();
        boolean inNum = false;
        boolean isBreak = false;
        if (isEnd) {
            return false;
        }
        while (true) {
            for (; locale < read; locale++) {
                if (buffer[locale] == ' ') {
                    if (inNum) {
                        locale++;
                        isBreak = true;
                        break;
                    }
                } else if (isHexDigit(buffer[locale]) || buffer[locale] == '-'){
                    sb.append(buffer[locale]);
                    inNum = true;
                }
            }
            if(isBreak) {
                break;
            }
            read = in.read(buffer);
            locale = 0;
            if (read == - 1) {
                isEnd = true;
                break;
            }
        }
        if (sb.length() > 0) {
            hasNum = true;
        }
        return hasNum;
    }

    public void close() throws IOException {
        in.close();
    }

    public String nextLine() throws IOException {
        if(hasNextLine()) {
            hasLine = false;
            return sb.toString();
        } else {
            return null;
        }
    }

    public boolean hasNextLine() throws FileNotFoundException, IOException {
        if (hasLine) {
            return true;
        }
        sb = new StringBuilder();
        boolean inLine = false;
        boolean isBreak = false;
        if (isEnd) {
            return false;
        }
        while (true) {
            for (; locale < read; locale++) {
                if (buffer[locale] != separator.charAt(n - 1)) {
                    sb.append(buffer[locale]);
                } else if (buffer[locale] == separator.charAt(n - 1)) {
                    locale++;
                    isBreak = true;
                    break;
                }
            }
            if(isBreak) {
                hasLine = true;
                break;
            }
            read = in.read(buffer);
            locale = 0;
            if (read == - 1) {
                isEnd = true;
                break;
            }
        }
        if (sb.length() > 0) {
            hasLine = true;
        }
        return hasLine;
    }

    public String nextWord() throws IOException {
        if(hasNextWord()) {
            hasWord = false;
            wordsCount++;
            return sb.toString();
        } else {
            return null;
        }
    }

    public boolean hasNextWord() throws FileNotFoundException, IOException {
        if (hasWord) {
            return true;
        }
        sb = new StringBuilder();
        boolean inWord = false;
        boolean isBreak = false;
        if (isEnd) {
            return false;
        }
        if (newLine) {
            linesCount++;
            wordsCount = 0;
            newLine = false;
        }
        while (true) {
            for (; locale < read; locale++) {
                if (!isLegal(buffer[locale]) || buffer[locale] == separator.charAt(n - 1)) {
                    if (buffer[locale] == separator.charAt(n - 1)) {
                        if (inWord) {
                            newLine = true;
                        }
                        else {
                            linesCount++;
                            wordsCount = 0;
                        }
                    }
                    if (inWord) {
                        locale++;
                        isBreak = true;
                        break;
                    }
                } else if (isLegal(buffer[locale])) {
                    sb.append(buffer[locale]);
                    inWord = true;
                }
            }
            if(isBreak) {
                break;
            }
            read = in.read(buffer);
            locale = 0;
            if (read == - 1) {
                isEnd = true;
                break;
            }
        }
        if (sb.length() > 0) {
            hasWord = true;
        }
        return hasWord;
    }

    public int linePos() {
        return linesCount;
    }

    public int wordPos() {
        return wordsCount;
    }

    public boolean isHexDigit(char a) {
        return '0' <= a && a <= '9' || 'a' <= a && a <= 'f' || 'A' <= a && a <= 'F';
    }

}