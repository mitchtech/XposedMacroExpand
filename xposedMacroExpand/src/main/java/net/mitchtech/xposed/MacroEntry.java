
package net.mitchtech.xposed;

public class MacroEntry {
    public String actual;
    public String replacement;
    public boolean enabled;

    public MacroEntry(String actual, String replacement) {
        super();
        this.actual = actual;
        this.replacement = replacement;
    }

    public String toString() {
        return actual + " --> " + replacement;
    }
    
    public String toAhkRow() {
        return "::" + actual + "::" + replacement + "\n";
    }

    public String isEnabled() { return (enabled) ? "Enabled" : "Disabled" ; }
}
