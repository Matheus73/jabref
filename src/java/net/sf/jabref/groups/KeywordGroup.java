package net.sf.jabref.groups;

import java.util.*;
import java.util.regex.*;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import net.sf.jabref.*;
import net.sf.jabref.undo.*;

/**
 * @author zieren
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class KeywordGroup extends AbstractGroup implements SearchRule {
    public static final String ID = "KeywordGroup:";
    private String m_name;
    private String m_searchField;
    private String m_searchExpression;
    private Pattern m_pattern;
    private boolean m_expressionMatchesItself = false;

    /**
     * Creates a KeywordGroup with the specified properties.
     */
    public KeywordGroup(String name, String searchField, String searchExpression)
            throws IllegalArgumentException, PatternSyntaxException {
        m_name = name;
        m_searchField = searchField;
        m_searchExpression = searchExpression;
        compilePattern();
    }

    private void compilePattern() throws IllegalArgumentException,
            PatternSyntaxException {
        m_pattern = Pattern.compile(m_searchExpression,
                Pattern.CASE_INSENSITIVE);
        // this is required to decide whether entries can be added
        // to this group by adding m_searchExpression to the m_searchField
        // (it's quite a hack, but the only solution would be to disable
        // add/remove completely for keyword groups)
        m_expressionMatchesItself = m_pattern.matcher(m_searchExpression)
                .matches();
    }

    /**
     * Parses s and recreates the KeywordGroup from it.
     * 
     * @param s
     *            The String representation obtained from
     *            KeywordGroup.toString()
     */
    public static AbstractGroup fromString(String s) throws Exception {
        if (!s.startsWith(ID))
            throw new Exception(
                    "Internal error: KeywordGroup cannot be created from \""
                            + s + "\"");
        QuotedStringTokenizer tok = new QuotedStringTokenizer(s.substring(ID
                .length()), SEPARATOR, QUOTE_CHAR);
        String name = tok.nextToken();
        String field = tok.nextToken();
        String expression = tok.nextToken();
        return new KeywordGroup(Util.unquote(name, QUOTE_CHAR), Util.unquote(
                field, QUOTE_CHAR), Util.unquote(expression, QUOTE_CHAR));
    }

    /**
     * @see net.sf.jabref.groups.AbstractGroup#getSearchRule()
     */
    public SearchRule getSearchRule() {
        return this;
    }

    /**
     * Returns a String representation of this object that can be used to
     * reconstruct it.
     */
    public String toString() {
        return ID + Util.quote(m_name, SEPARATOR, QUOTE_CHAR) + SEPARATOR
                + Util.quote(m_searchField, SEPARATOR, QUOTE_CHAR) + SEPARATOR
                + Util.quote(m_searchExpression, SEPARATOR, QUOTE_CHAR)
                + SEPARATOR;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String m_name) {
        this.m_name = m_name;
    }

    public String getSearchField() {
        return m_searchField;
    }

    public void setSearchField(String field) {
        m_searchField = field;
    }

    public String getSearchExpression() {
        return m_searchExpression;
    }

    public void setSearchExpression(String expression)
            throws IllegalArgumentException, PatternSyntaxException {
        m_searchExpression = expression;
        compilePattern();
    }

    public boolean supportsAdd() {
        return m_expressionMatchesItself;
    }

    public boolean supportsRemove() {
        return true;
    }

    public void addSelection(BasePanel basePanel) {
        if (!supportsAdd()) {
            // this should never happen
            basePanel.output("The group \"" + getName()
                    + "\" does not support the adding of entries.");
            return;
        }
        for (int i = 0; i < GUIGlobals.ALL_FIELDS.length; ++i) {
            if (m_searchField.equals(GUIGlobals.ALL_FIELDS[i])
                    && !m_searchField.equals("keywords")) {
                if (!showWarningDialog(basePanel))
                    return;
            }
        }

        BibtexEntry[] bes = basePanel.getSelectedEntries();
        if ((bes != null) && (bes.length > 0)) {
            NamedCompound ce = new NamedCompound("add to group");
            boolean hasEdits = false;
            for (int i = 0; i < bes.length; i++) {
                if (applyRule(null, bes[i]) == 0) {
                    String oldContent = (String) bes[i].getField(m_searchField), pre = " ", post = "";
                    String newContent = (oldContent == null ? "" : oldContent
                            + pre)
                            + m_searchExpression + post;
                    bes[i].setField(m_searchField, newContent);

                    // Store undo information.
                    ce.addEdit(new UndoableFieldChange(bes[i], m_searchField,
                            oldContent, newContent));
                    hasEdits = true;
                }
            }
            if (hasEdits) {
                ce.end();
                basePanel.undoManager.addEdit(ce);
                basePanel.refreshTable();
                basePanel.markBaseChanged();
                basePanel.updateEntryEditorIfShowing();
                basePanel.updateViewToSelected();
            }

            basePanel.output("Appended '" + m_searchExpression + "' to the '"
                    + m_searchField + "' field of " + bes.length + " entr"
                    + (bes.length > 1 ? "ies." : "y."));
        }
    }

    /**
     * Displays a warning message about changes to the entries due to adding
     * to/removal from a group.
     * 
     * @return true if the user chose to proceed, false otherwise.
     */
    private boolean showWarningDialog(BasePanel basePanel) {
        String message = "This action will modify the \""
                + m_searchField
                + "\" field "
                + "of your entries.\nThis could cause undesired changes to "
                + "your entries, so it\nis recommended that you change the field "
                + "in your group\ndefinition to \"keywords\" or a non-standard name."
                + "\n\nDo you still want to continue?";
        int choice = JOptionPane.showConfirmDialog(basePanel, message,
                "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        return choice != JOptionPane.NO_OPTION;
    }

    public void removeSelection(BasePanel basePanel) {
        if (!supportsRemove()) {
            // this should never happen
            basePanel.output("The group \"" + getName()
                    + "\" does not support the removal of entries.");
            return;
        }
        for (int i = 0; i < GUIGlobals.ALL_FIELDS.length; i++) {
            if (m_searchField.equals(GUIGlobals.ALL_FIELDS[i])
                    && !m_searchField.equals("keywords")) {
                if (!showWarningDialog(basePanel))
                    return;
            }
        }

        BibtexEntry[] selectedEntries = basePanel.getSelectedEntries();
        if ((selectedEntries != null) && (selectedEntries.length > 0)) {
            NamedCompound ce = new NamedCompound("remove from group");
            boolean hasEdits = false;
            for (int i = 0; i < selectedEntries.length; ++i) {
                if (applyRule(null, selectedEntries[i]) > 0) {
                    String oldContent = (String) selectedEntries[i]
                            .getField(m_searchField);
                    removeMatches(selectedEntries[i]);
                    // Store undo information.
                    ce.addEdit(new UndoableFieldChange(selectedEntries[i],
                            m_searchField, oldContent, selectedEntries[i]
                                    .getField(m_searchField)));
                    hasEdits = true;
                }
            }
            if (hasEdits) {
                ce.end();
                basePanel.undoManager.addEdit(ce);
                basePanel.refreshTable();
                basePanel.markBaseChanged();
                basePanel.updateEntryEditorIfShowing();
                basePanel.updateViewToSelected();
            }

            basePanel.output("Removed '" + m_searchExpression + "' from the '"
                    + m_searchField + "' field of " + selectedEntries.length
                    + " entr" + (selectedEntries.length > 1 ? "ies." : "y."));
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof KeywordGroup))
            return false;
        KeywordGroup other = (KeywordGroup) o;
        return m_name.equals(other.m_name)
                && m_searchField.equals(other.m_searchField)
                && m_searchExpression.equals(other.m_searchExpression);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.jabref.groups.AbstractGroup#contains(java.util.Map,
     *      net.sf.jabref.BibtexEntry)
     */
    public int contains(Map searchOptions, BibtexEntry entry) {
        String content = (String) entry.getField(m_searchField);
        if ((content != null) && (m_pattern.matcher(content).find()))
            return 1;
        return 0;
    }

    /**
     * Removes matches of searchString in the entry's field.
     */
    private void removeMatches(BibtexEntry entry) {
        String content = (String) entry.getField(m_searchField);
        StringBuffer sb = new StringBuffer();
        if (content != null) {
            String[] split = m_pattern.split(content);
            for (int i = 0; i < split.length; ++i)
                sb.append(split[i]);
        }
        entry.setField(m_searchField, (sb.length() > 0 ? sb.toString() : null));
    }

    public int applyRule(Map searchOptions, BibtexEntry entry) {
        return contains(searchOptions, entry);
    }

    public AbstractGroup deepCopy() {
        try {
            return new KeywordGroup(m_name, m_searchField, m_searchExpression);
        } catch (Throwable t) {
            // this should never happen, because the constructor obviously
            // succeeded in creating _this_ instance!
            System.err.println("Internal error: Exception " + t
                    + " in KeywordGroup.deepCopy()");
            return null;
        }
    }
}
