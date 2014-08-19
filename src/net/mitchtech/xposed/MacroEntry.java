
package net.mitchtech.xposed;

public class MacroEntry {
    public String actual;
    public String replacement;

    public MacroEntry() {
        this("actual", "replacement");
    }

    public MacroEntry(String actual, String replacement) {
        super();
        this.actual = actual;
        this.replacement = replacement;
    }

    public String toString() {
        return actual + " : " + replacement;
    }
}
